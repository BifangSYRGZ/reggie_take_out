package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @neirong:
 * @banben:
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
}
