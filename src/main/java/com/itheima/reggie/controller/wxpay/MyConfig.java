package com.itheima.reggie.controller.wxpay;

import com.github.wxpay.sdk.IWXPayDomain;
import com.github.wxpay.sdk.WXPayConfig;

import java.io.InputStream;

public class MyConfig extends WXPayConfig {
    @Override
    public String getAppID() {
        return "wx8397f8696b538317";
    }

    @Override
    public String getMchID() {
        return "1473426802";
    }

    @Override
    public String getKey() {
        return "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb";
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    /**
     * 微信的支付域名
     * @return
     */
    @Override
    public IWXPayDomain getWXPayDomain() {
        return new IWXPayDomain() {
            public void report(String domain, long elapsedTimeMillis, Exception
                    ex) {
            }
            public DomainInfo getDomain(WXPayConfig config) {
                return new DomainInfo("api.mch.weixin.qq.com",true);
            }
        };
    }
}