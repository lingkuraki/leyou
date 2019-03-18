package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.order.enums.PayStatusEnum;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.PayLogMapper;
import com.leyou.order.pojo.PayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PayLogService {

    @Autowired
    private PayLogMapper payLogMapper;

    public void createPayLog(Long orderId, Long actualPay) {
        // 创建支付对象
        PayLog payLog = new PayLog();
        payLog.setOrderId(orderId);
        payLog.setPayType(1);
        payLog.setStatus(PayStatusEnum.NOT_PAY.value());
        payLog.setCreateTime(new Date());
        payLog.setTotalFee(actualPay);
        // 用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        payLog.setUserId(userInfo.getId());
        PayLog payLog1 = payLogMapper.selectByPrimaryKey(payLog.getOrderId());
        if (payLog1 == null) {
            this.payLogMapper.insertSelective(payLog);
        } else {
            this.payLogMapper.updateByPrimaryKeySelective(payLog);
        }
    }
}
