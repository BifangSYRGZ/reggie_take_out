package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Setmeal;

/**
 * @neirong:
 * @banben:
 */
public interface SetmealService extends IService<Setmeal> {

    R selectListshuju(Long categoryId, Integer status);
}
