package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.categoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @neirong:
 * @banben:
 */
@RestController
@RequestMapping("/category")
public class categoryController {
    @Autowired
    private categoryService servcie;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @PostMapping()
    public R addcategory(@RequestBody Category category) {
        boolean save = servcie.save(category);
        if (save) {
            return R.success("添加成功");
        }
        return R.error("添加失败");
    }

    @GetMapping("/page")
    public R pagecategory(int page, int pageSize) {

        IPage<Category> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> wapper = new LambdaQueryWrapper<>();
        wapper.orderByAsc(Category::getSort);
        IPage<Category> page1 = servcie.page(p, wapper);
        return R.success(page1);
    }

    @DeleteMapping()
    public R pagecategory(Long id) {
        //判断菜系是否为空
        LambdaQueryWrapper<Dish> wapper = new LambdaQueryWrapper<>();
        wapper.eq(Dish::getCategoryId, id);
        List<Dish> list = dishService.list(wapper);
        if (list.size() > 0) {
            //不为空,不能删除
            return R.error("此分类下还有菜,不能删除");
        }
        //判断套餐是否为空
        LambdaQueryWrapper<Setmeal> wappers = new LambdaQueryWrapper<>();
        wappers.eq(Setmeal::getCategoryId, id);
        List<Setmeal> list1 = setmealService.list(wappers);
        if (list1.size() > 0) {
            //不为空,不能删除
            return R.error("此分类下还有菜,不能删除");
        }
        boolean b = servcie.removeById(id);
        if (b) {
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

    @PutMapping()
    public R updatecategory(@RequestBody Category category) {

        boolean b = servcie.updateById(category);
        if (b) {
            return R.success("更新成功");
        }
        return R.error("更新失败");

    }

    //菜品新增
    //1.菜品分类回显
    @GetMapping("/list")
    //前段传过来的是id
    public R findAllCategory(Integer type) {
        LambdaQueryWrapper<Category> wapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wapper.eq(Category::getType, type);
        }
        List<Category> list = servcie.list(wapper);
        return R.success(list);

    }
}
