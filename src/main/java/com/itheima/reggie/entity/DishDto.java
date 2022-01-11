package com.itheima.reggie.entity;

import lombok.Data;

import java.util.List;

/**
 * @neirong:
 * @banben:
 */
@Data
public class DishDto extends Dish{


    private List<DishFlavor> flavors;
    //查询
    private String categoryName;
    private Integer copies;
}
