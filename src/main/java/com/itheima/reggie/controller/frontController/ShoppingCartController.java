package com.itheima.reggie.controller.frontController;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //获取菜品的id
        Long dishId = shoppingCart.getDishId();
        //添加判断条件 如果购物车getUserId和购物车绑定的id一致
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        //如果这个菜品存在
        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
            //判断菜品是否有口味信息
            if (shoppingCart.getDishFlavor()!=null){
                queryWrapper.eq(ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());
            }
        } else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //查询当前菜品或者套餐是否在购物车中
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne != null) {
            //如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        if (cartServiceOne.getDishId()!=null){
            cartServiceOne.setNumber(null);
            return R.success(cartServiceOne);
        }else {
            return R.success(cartServiceOne);
        }
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        //SQL:delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

    /**
     * 删除购物车数据
     * @return R
     */
    @PostMapping("/sub")
    public R subshopping(@RequestBody ShoppingCart shoppingCart){
        //健壮性判断
        if (shoppingCart==null){
            return R.success("参数异常");
        }
        //获取菜品id
        Long dishId = shoppingCart.getDishId();
        String dishFlavor = shoppingCart.getDishFlavor();
        LambdaUpdateWrapper<ShoppingCart> wrapper=new LambdaUpdateWrapper<>();
        if (dishId!=null){
            wrapper.eq(ShoppingCart::getDishId,dishId);
        }
        //有口味 是菜品
        if (dishFlavor != null){
            wrapper.eq(ShoppingCart::getDishFlavor,dishFlavor);
        }
        if (shoppingCart.getSetmealId() != null){
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(wrapper);
        //判断是套餐还是菜品 //没有口味就是套餐 //判断该套餐 相同口味有几份
        if (one.getNumber()==1){
            //如果一份直接删除
            shoppingCartService.remove(wrapper);
        }
        //如果多份,Number-1 进行更新
        one.setNumber(one.getNumber()-1);
        shoppingCartService.updateById(one);
/*
        if (shoppingCarts.getDishId() != null && shoppingCart.getDishFlavor() != null) {
            shoppingCarts.setNumber(null);
            return R.success(shoppingCarts);
        } else {
            return R.success(shoppingCarts);
        }*/

        return R.success("删除成功");
    }

}