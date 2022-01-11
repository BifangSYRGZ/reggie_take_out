package com.itheima.reggie.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.categoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @neirong:
 * @banben:
 */
@RestController
@RequestMapping("/dish")
@Transactional //开启事务
public class DishController {

   @Autowired
   private DishService service;
   @Autowired
   private DishFlavorService dishFlavorService;
   @Autowired
   private categoryService categoryservice;
    //添加菜品
    @PostMapping()
    public R dishadd(@RequestBody DishDto dishDto){
        //健壮性判断
        if (dishDto==null){
            return R.error("参数错误");
        }
        Dish dish = new Dish();
        //使用Spring工具类来复制对象  前面是来源对象 后面是目标对象
        BeanUtils.copyProperties(dishDto,dish);
        //存储dish菜品
        service.save(dish);
        //循环保存菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dish.getId());
            dishFlavorService.save(flavor);
        }
        return R.success("添加成功");
    }
    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        service.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryservice.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }
  /*  //分页显示菜品数据
    @GetMapping("/page")
    public R fenyepage(Integer page, Integer pageSize,String name) {

        IPage<Dish> p=new Page<>(page,pageSize);
        LambdaQueryWrapper<Dish> wapper=new LambdaQueryWrapper<>();
        //判断是否需要模糊查询
        if (name!=null){
            wapper.like(Dish::getName,name);
        }
        ArrayList<DishDto> dishDtos = new ArrayList<>();
        IPage<Dish> dish = service.page(p, wapper);
        List<Dish> dishList = dish.getRecords();
        for (Dish dish1 : dishList) {
            Long categoryId = dish1.getCategoryId();
            Category category = categoryservice.getById(categoryId);
            //转换对象
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            dishDto.setCategoryName(category.getName());
            dishDtos.add(dishDto);
        }
        Page<DishDto> page1 = new Page<>();
        page1.setRecords(dishDtos);
        page1.setTotal(dish.getTotal());
        page1.setCurrent(dish.getCurrent());
        page1.setSize(dish.getSize());
        return R.success(page1);

    }*/
    //菜品的修改 -回显
    @GetMapping("/{id}")
    public R dishid(@PathVariable Long id){
        //查询所有dish数据
        Dish dish = service.getById(id);
        //查询口味信息
        LambdaQueryWrapper<DishFlavor> wapper=new LambdaQueryWrapper<>();
        wapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(wapper);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(dishFlavors);
        return R.success(dishDto);
    }
    //菜品的修改 -修改 (先删后增)
    @PutMapping()
    public R adddish(@RequestBody DishDto dishDto){
        //菜品类数据直接修改
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDto,dish);
        service.updateById(dish);
        //先删除菜品数据 在进行新增操作
        LambdaQueryWrapper<DishFlavor> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(wrapper);
        //循环添加菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            //拿到所有的口味对象
            flavor.setDishId(dishDto.getId());
            dishFlavorService.save(flavor);
        }
        return R.success("修改成功");
    }

    //批量删除
    @DeleteMapping()
    public R deleteAll(Long [] ids){
        //根据传的id进行删除操作
        service.removeByIds(Arrays.asList(ids));
        for (Long id : ids) {
            //删除口味
            LambdaQueryWrapper<DishFlavor> wrapper= new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId,id);
            dishFlavorService.remove(wrapper);
            //service.removeById(id);
        }
        return R.success("删除成功");
    }

    //批量起售
    @PostMapping("/status/{rest}")
    public R qishouAll(@PathVariable Integer rest,Long [] ids){

        LambdaUpdateWrapper<Dish> wrapper=new LambdaUpdateWrapper<>();
        wrapper.set(Dish::getStatus,rest).in(Dish::getId,ids);
        service.update(wrapper);


        //根据id修改菜品的起售状态
       /* for (Long id : ids) {
            //获得对象
            Dish byId = service.getById(id);
            //修改数据
            byId.setStatus(rest);
            service.updateById(byId);

        }*/
        return R.success("操作成功");
    }
    //新增套餐
    @GetMapping("/list")
    public R dishlist(Long categoryId){
        //菜品查询
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId,categoryId);
        List<Dish> list = service.list(wrapper);


        //todo 需要查询菜品的口味信息
        //1.把菜的集合转成DishDto
        String s = JSON.toJSONString(list);
        List<DishDto> dishDtos = JSON.parseArray(s, DishDto.class);
        //2.循环给菜添加口味信息'
        for (DishDto dishDto : dishDtos) {
            LambdaQueryWrapper<DishFlavor> wrapper2=new LambdaQueryWrapper<>();
            wrapper2.eq(DishFlavor::getDishId,dishDto.getId());
            List<DishFlavor> list1 = dishFlavorService.list(wrapper2);
            dishDto.setFlavors(list1);
        }
        //2.1根据菜品的id,查询菜的口味
        return R.success(dishDtos);
    }
}
