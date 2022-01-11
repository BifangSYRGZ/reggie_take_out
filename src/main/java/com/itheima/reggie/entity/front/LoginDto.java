package com.itheima.reggie.entity.front;

import lombok.Data;

/**
 * @neirong:
 * @banben:
 */
@Data
public class LoginDto {
    //手机号
    private String phone;
    //验证码
    private String code;
}
