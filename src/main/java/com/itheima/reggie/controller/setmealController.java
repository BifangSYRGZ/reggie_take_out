package com.itheima.reggie.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @neirong:
 * @banben:
 */
@RestController
@RequestMapping("/setmeal")
@Transactional //添加事务
public class setmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishServie setmealDishServie;
    @Autowired
    private categoryService categoryservice;

    //添加套餐数据
    @PostMapping()
    public R add(@RequestBody setmealDto setmealdto) {
        //1.保存套餐到套餐表中
        setmealService.save(setmealdto);
        //2.保存套餐菜品信息到中间表中
        List<SetmealDish> setmealDishes = setmealdto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealdto.getId());
            setmealDishServie.save(setmealDish);
        }
        return R.success("添加成功");
    }

    //分页查询所有套餐信息
    @GetMapping("/page")
    public R pagetaocan(Integer page, Integer pageSize, String name) {
        //分页查询
        IPage<Setmeal> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        //添加模糊查询的条件
        if (name != null) {
            wrapper.like(Setmeal::getName, name);
        }
        //分页查询
        IPage<Setmeal> page1 = setmealService.page(p, wrapper);
        //获得数据
        List<Setmeal> records = page1.getRecords();
        //Setmeal集合数据转成JSON字符串
        String jsonString = JSON.toJSONString(records);
        //JSON字符串转换成setmealDto集合数据
        List<setmealDto> setmealDtos = JSON.parseArray(jsonString, setmealDto.class);
        for (setmealDto setmealDto : setmealDtos) {
            //setmealDto中需要展示套餐分类的名字
            String categoryename = categoryservice.getById(setmealDto.getCategoryId()).getName();
            //菜品的名称设置进去
            setmealDto.setCategoryName(categoryename);
        }
        String s = JSON.toJSONString(page1);
        Page page2 = JSON.parseObject(s, Page.class);
        page2.setRecords(setmealDtos);
        return R.success(page2);

    }

    //批量删除套餐
    @DeleteMapping()
    public R Deletetaocan(Long[] ids) {
        //删除套餐
        setmealService.removeByIds(Arrays.asList(ids));
        //删除套餐中的菜品信息
        LambdaUpdateWrapper<SetmealDish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishServie.remove(wrapper);
        return R.success("删除成功");
    }

    //批量起售停售
    @PostMapping("/status/{id}")
    //请求有两个值,1状态值 id 2 需要修改状态的数据ids
    public R updatestatus(@PathVariable Integer id, Long[] ids) {
        //修改套餐销售状态 根据id
        LambdaUpdateWrapper<Setmeal> wrapper = new LambdaUpdateWrapper<>();
        //设置Status的状态,并根据id来修改
        wrapper.set(Setmeal::getStatus, id).in(Setmeal::getId, ids);
        setmealService.update(wrapper);
        return R.success("操作成功");
    }

    //修改数据
    @GetMapping("/{id}")
    public R updateshuju(@PathVariable Long id) {
        //1.根据id查询套餐信息
        Setmeal service = setmealService.getById(id);
        //2.根据套餐信息,获取中间表dish_id
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SetmealDish::getSetmealId, id);
        //3.查询菜品信息
        List<SetmealDish> dishList = setmealDishServie.list(wrapper);
        //4.菜品信息封装到setmealDto中
        String jsonString = JSON.toJSONString(service);
        setmealDto setmealDto = JSON.parseObject(jsonString, setmealDto.class);
        setmealDto.setSetmealDishes(dishList);
        //5.返回数据 R<SetmealDto>
        return R.success(setmealDto);
    }

    //数据的保存
    @PutMapping()
    public R updaetaocan(@RequestBody setmealDto setmealdto) {
        //请求数据 setmealdto
        //1.更新Setmeal数据
        setmealService.updateById(setmealdto);
        //2.获取Setmeal id 根据id删除中间表数据
        Long id = setmealdto.getId();
        LambdaUpdateWrapper<SetmealDish> warpper = new LambdaUpdateWrapper<>();
        warpper.eq(SetmealDish::getSetmealId, id);
        setmealDishServie.remove(warpper);
        //3.获取setmealdto中的List<SetmealDish> setmealDishes数据
        List<SetmealDish> setmealDishes = setmealdto.getSetmealDishes();
        //4.新增List<SetmealDish> setmealDishes数据和Setmeal id到中间表中
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(id);
            setmealDishServie.save(setmealDish);
        }
        //5.返回数据
        return R.success("修改成功");
    }



    @GetMapping("/list")
    public R selectListshuju(Long categoryId, Integer status) {
        R r=setmealService.selectListshuju(categoryId,status);
        return r;
    }

   /* @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }*/


}
