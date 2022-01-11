package com.itheima.reggie.entity;

import lombok.Data;

import java.util.List;

/**
 * @neirong:
 * @banben:
 */
@Data
public class setmealDto extends Setmeal{

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
