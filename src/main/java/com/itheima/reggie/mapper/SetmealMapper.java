package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Setmeal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @neirong:
 * @banben:
 */
public interface SetmealMapper extends BaseMapper<Setmeal> {
    @Select("select *from setmeal where category_id=#{category_id} and status=#{status}")
    List<Setmeal> selectListshuju(@Param("category_id") Long categoryId, @Param("status") Integer status);
}
