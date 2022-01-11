package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @neirong:
 * @banben:
 */
@RestController
@RequestMapping("/employee")
@Slf4j
public class employeeController {
    @Autowired
    private EmployeeService service;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/login")
    public R login(@RequestBody Employee employee) {

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> wapper = new LambdaQueryWrapper<>();
        wapper.eq(Employee::getUsername, employee.getUsername());
        Employee employees = service.getOne(wapper);

        //3、如果没有查询到则返回登录失败结果
        if (employees == null) {
            //查询不到数据
            return R.error("登录失败");

        }
        //4、密码比对，如果不一致则返回登录失败结果
        if(!employees.getPassword().equals(password)){
            return R.error("登录失败");
        }
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(employees.getStatus() == 0){
            return R.error("账号已禁用");
        }
        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",employees.getId());
        return R.success(employees);

    }
    //清除session数据 完成退出登陆操作
    @PostMapping("/logout")
    private R logout(){
        HttpSession session = request.getSession();
        session.removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping()
    public R employee(@RequestBody Employee employee){
        //补全数据
        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //获得当前登录用户的id
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        service.save(employee);
        return R.success("新增员工成功");
    }

    @GetMapping("/page")
    public R page(Integer page,Integer pageSize,String name){
        LambdaQueryWrapper<Employee> wapper = new LambdaQueryWrapper<>();
        IPage<Employee> p=new Page<>(page,pageSize);
        if (name==null){
            //没有模糊查询 进行数据分页展示
            IPage<Employee> page1 = service.page(p,null);
            return R.success(page1);
        }
        //添加过滤条件
        wapper.like(Employee::getName,name);
        IPage<Employee> page1 = service.page(p, wapper);
        return R.success(page1);
    }


    //修改员工状态
    @PutMapping()
    public R updatestatus(@RequestBody Employee employee){
        //修改员工状态,根据id和状态
        Long empId = (Long)request.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empId);
        service.updateById(employee);
        return R.success("员工信息修改成功");
    }
    //员工数据回显
    @GetMapping("/{id}")
    public R FindByid(@PathVariable Long id){
        Employee employee = service.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }


}
