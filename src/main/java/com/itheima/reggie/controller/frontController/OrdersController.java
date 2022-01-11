package com.itheima.reggie.controller.frontController;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.wxpay.sdk.WXPay;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.controller.wxpay.MyConfig;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @version 1.0
 * @Author A_wei
 * @Date 2022/1/5 15:14
 * @注释 订单处理器
 */
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired //购物车
    private ShoppingCartService shoppingCartService;
    @Autowired //地址
    private AddressBookService addressBookService;
    @Autowired //用户
    private userService userservice;
    @Autowired //订单
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartController shoppingCartController;

    @PostMapping("/submit")
    public R submit(@RequestBody Orders orders) {
        //A. 获得当前用户id, 查询当前用户的购物车数据        // 购物车的数据(用户购买的商品信息)
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, currentId);
        //订单明细数据
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);
        //B. 根据当前登录用户id, 查询用户数据
        User user = userservice.getById(currentId);
        //C. 根据地址ID, 查询地址数据
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());

        //D. 组装订单明细数据, 批量保存订单明细 补全数据
        orders.setStatus(1);//待付款
        orders.setUserId(currentId);//用户id
        orders.setAddress(addressBook.getDetail());//收货人地址
        orders.setAmount(sumMoney(shoppingCarts));//计算获得的总金额
        orders.setCheckoutTime(LocalDateTime.now());//付款时间
        orders.setConsignee(addressBook.getConsignee());//收件人姓名
        orders.setOrderTime(LocalDateTime.now());//下单时间
        orders.setNumber(UUID.randomUUID().toString()); //订单编号
        orders.setPhone(addressBook.getPhone());
        orders.setUserName(user.getName());
        orderService.save(orders);
        //E. 组装订单数据, 批量保存订单数据
        String s = JSON.toJSONString(shoppingCarts);
        List<OrderDetail> orderDetails = JSON.parseArray(s, OrderDetail.class);
        //F. 删除当前用户的购物车列表数据
        orderDetails.stream().forEach(
                c -> c.setOrderId(orders.getId())
        );
        orderDetailService.saveBatch(orderDetails);//批量添加
        shoppingCartController.clean();
        //todo 请求微信获得支付的二维码信息
        MyConfig myConfig = new MyConfig();
        WXPay wxPay = null;//核心支付类
        try {
             wxPay = new WXPay(myConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap<String, String> map = new HashMap<>();
        //封装请求参数
        //购买的商品信息
        map.put("body", "红烧肉盖浇饭");
        //商家的订单号
        map.put("out_trade_no",orders.getId()+"");
        //付款金额
        map.put("total_fee","1");//以分为单位
        map.put("spbill_create_ip","127.0.0.1");
        //回调通知  商家接收微信的信息的接口
        map.put("notify_url","http://www.baidu.com");
        //支付类型
        map.put("trade_type","NATIVE");
        try {
            Map<String, String> stringMap = wxPay.unifiedOrder(map);
            return R.success(stringMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.success("下单失败");
    }

    /**
     * 累计计算购物车的总金额
     *
     * @param shoppingCarts
     * @return
     */
    private BigDecimal sumMoney(List<ShoppingCart> shoppingCarts) {
        BigDecimal bigDecimal = new BigDecimal(0);
        for (ShoppingCart shoppingCart : shoppingCarts) {
            bigDecimal = bigDecimal.add(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())));
        }
        return bigDecimal;
    }

    /**
     * 订单查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R userPage(Integer page, Integer pageSize) {
        //1.获取当前登陆用户的id
        Long currentId = BaseContext.getCurrentId();
        //2.根据id查询订单数据
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        IPage<Orders> p = new Page<>(page, pageSize);
        wrapper.eq(Orders::getUserId, currentId).orderByDesc(Orders::getOrderTime);
        IPage<Orders> page1 = orderService.page(p, wrapper);
        //准换成OrdersDto数据
        List<Orders> records = page1.getRecords();
        String s = JSON.toJSONString(records);
        List<OrdersDto> ordersDtos = JSON.parseArray(s, OrdersDto.class);

        for (OrdersDto ordersDto : ordersDtos) {
            Long id = ordersDto.getId();
            LambdaQueryWrapper<OrderDetail> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(OrderDetail::getOrderId, id);
            List<OrderDetail> list = orderDetailService.list(wrapper1);
            ordersDto.setOrderDetails(list);
        }
        String jsonString1 = JSON.toJSONString(page1);
        Page page2 = JSON.parseObject(jsonString1, Page.class);
        page2.setRecords(ordersDtos);
        return R.success(page2);
    }
}
