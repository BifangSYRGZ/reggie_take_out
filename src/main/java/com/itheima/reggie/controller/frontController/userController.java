package com.itheima.reggie.controller.frontController;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.entity.front.LoginDto;
import com.itheima.reggie.service.categoryService;
import com.itheima.reggie.service.userService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Random;

/**
 * @neirong:
 * @banben:
 */

@RestController
@RequestMapping("/user")
public class userController {
    @Autowired
    private userService userservice;

    @Autowired
    private HttpServletRequest request;
    @PostMapping("/sendMsg")
    public R sendMsg(@RequestBody LoginDto loginDto){
        //给loginDto中手机号发送短信
        //Random random = new Random();
        //随即一个验证码
        //int code = random.nextInt(1000000);
        String code = ValidateCodeUtils.generateValidateCode4String(6);
        //模拟发送短息
        System.out.println("发送验证码"+code);
        //验证码储存一份
        HttpSession session = request.getSession();
        session.setAttribute(loginDto.getPhone(),code);
        return R.success("短信发送成功");
    }

    @PostMapping("/login")
    public R login(@RequestBody LoginDto loginDto){
        if(loginDto==null){
            return R.error("非法参数");
        }
        //1). 获取前端传递的手机号和验证码
        String phone = loginDto.getPhone();
        String code = loginDto.getCode();
        //2). 从Session中获取到手机号对应的正确的验证码
        HttpSession session = request.getSession();
        String code1 = (String) session.getAttribute(loginDto.getPhone());
        //3). 进行验证码的比对 , 如果比对失败, 直接返回错误信息
        //健壮性判断
        if (code==null){
            return R.error("请先获得验证码");
        }
        if (!code1.equals(code)){
            return R.error("验证码错误");
        }
        //4). 如果比对成功, 需要根据手机号查询当前用户, 如果用户不存在, 则自动注册一个新用户
        //查询用户是否存在
        LambdaQueryWrapper<User> warpper =new LambdaQueryWrapper<>();
        warpper.eq(User::getPhone,loginDto.getPhone());
        User user = userservice.getOne(warpper);
        if (user==null){
            //用户不存在 新加一个用户
            User user1 = new User();
            user1.setPhone(loginDto.getPhone());
            userservice.save(user1);
            session.setAttribute("userId",user1.getId());
            return R.success("成功");
        }
        //存在就登陆成功
        //5). 将登录用户的ID存储Session中
        session.setAttribute("userId",user.getId());
        return R.success("成功");
    }

    /**
     * 退出登陆
     * @return
     */
    @PostMapping("/loginout")
    public R loginout(){
        HttpSession session = request.getSession();
        session.invalidate();
        return R.success("退出成功");
    }
}
