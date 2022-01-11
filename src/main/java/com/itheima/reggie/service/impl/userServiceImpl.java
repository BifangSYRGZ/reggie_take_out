package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.front.userMapper;
import com.itheima.reggie.service.userService;
import org.springframework.stereotype.Service;

/**
 * @neirong:
 * @banben:
 */
@Service
public class userServiceImpl extends ServiceImpl<userMapper, User> implements userService {
}
