package com.itheima.reggie.controller.wxpay;

import com.github.wxpay.sdk.WXPay;

import java.util.HashMap;
import java.util.Map;

public class Demo {
    public static void main(String[] args) throws Exception {
        /**
         * url ：  /pay/unifiedorder    wxPay.unifiedOrder
         * 请求方式
         * 请求参数    requsetMap
         * 返回值      responseMap
         */
        //微信支付的测试接口
        //1.创建一个微信支付核心对象
        MyConfig my = new MyConfig();
        //CONFIG配置类
        WXPay wxPay = new WXPay(my);
        //3.可以进行微信支付接口的调用了
        //统一下单接口
        //4.wxPay.unifiedOrder(); = 调用微信的支付接口  参数map
        Map<String, String> requsetMap = new HashMap<>();
        //购买的商品信息
        requsetMap.put("body", "红烧肉盖浇饭");
        //商家的订单号
        requsetMap.put("out_trade_no","J13013013013010123");
        //付款金额
        requsetMap.put("total_fee","1");//以分为单位

        requsetMap.put("spbill_create_ip","127.0.0.1");
        //回调通知  商家接收微信的信息的接口
        requsetMap.put("notify_url","http://www.baidu.com");
        //支付类型
        requsetMap.put("trade_type","NATIVE");

        //模拟浏览器发送一个http请求
        /**
         * requsetMap 代表请求参数
         * responseMap  返回值
         */
        Map<String, String> responseMap = wxPay.unifiedOrder(requsetMap);

        System.out.println(responseMap);
        //返回值中 code_url 就是支付二维码连接

        /**
         * return_msg  代表成功
         * result_code SUCCESS
         * code_url   生成支付二维码  weixin://wxpay/bizpayurl?pr=23CaFbOzz, = 二维码
         *
         *
         * {nonce_str=IQ1juuH6JdbiJygc, code_url=weixin://wxpay/bizpayurl?pr=23CaFbOzz,
         * appid=wx8397f8696b538317,
         * sign=0BB8257245E8255C6D6176930516546398AB9A0F23A4F49D8D7B46078CF3B6EA,
         * trade_type=NATIVE, return_msg=OK, result_code=SUCCESS, mch_id=1473426802,
         * return_code=SUCCESS, prepay_id=wx10094200313158c49bc3946a2e6a7d0000}
         */
    }
}