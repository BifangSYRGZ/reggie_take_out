package com.itheima.reggie.filter;

/**
 * @neirong:
 * @banben:
 */

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
//过滤器注解  filterName别名 不写默认为类名首字母小写 urlPatterns 设置的拦截路径
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    private static AntPathMatcher apm = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //转换请求响应格式
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //添加需要放行的数据
        String[] url = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/**"
        };
        //获取本次请求的路径
        String requestURI = request.getRequestURI();
        //判断此次请求是否需要放行
        for (String urls : url) {
            boolean match = apm.match(urls, requestURI);
            if (match) {
                //需要放行
                log.info("本次请求{}不需要处理", requestURI);
                filterChain.doFilter(request, response);
                return;
            }
        }
        /*if (request.getSession().getAttribute("employee") != null) {
            //是登陆状态
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
        }*/
        Long employee = (Long) request.getSession().getAttribute("employee");
        Long userId = (Long) request.getSession().getAttribute("userId");
        if (employee == null && userId == null) {
            log.info("用户未登录");
            //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            //response.sendRedirect("/backend/page/login/login.html");
            return;
        }
        //判断是否是登陆状态
        if (employee != null){
            BaseContext.threadLocal.set(employee);
        }else if (userId != null){
            BaseContext.threadLocal.set(userId);
        }
        filterChain.doFilter(request, response);
        return;
    }

}
