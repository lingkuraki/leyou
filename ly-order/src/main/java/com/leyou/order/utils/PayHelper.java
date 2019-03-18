package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.enums.PayStatusEnum;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.mapper.PayLogMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.pojo.PayLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PayHelper {

    private WXPay wxPay;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private String notifyUrl;

    private PayConfig payConfig;

    private final static Logger LOGGER = LoggerFactory.getLogger(PayHelper.class);

    public PayHelper(PayConfig payConfig) {
        // 使用微信官方提供的SDK工具，WxPay，并且把配置注入进去
        wxPay = new WXPay(payConfig);
        this.notifyUrl = payConfig.getNotifyUrl();
        this.payConfig = payConfig;
    }

    public String createPayUrl(Long orderId, String description, Long totalPay) {
        // 从缓存中取出支付链接
        String key = "order:pay:url:" + orderId;
        try {
            String url = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(url)) return url;
        } catch (Exception e) {
            LOGGER.error("查询缓存付款连接异常，订单编号：{}", orderId, e);
        }

        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", description);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            // 货币
            data.put("fee_type", "CNY");
            // 金额，单位是分
            data.put("total_fee", totalPay.toString());
            // 调用微信支付的终端ip
            data.put("spbill_create_ip", "127.0.0.1");
            // 回调地址
            data.put("notify_url", notifyUrl);
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            // 填充请求参数，并签名，然后将请求数据处理成字节
            byte[] request = WXPayUtil.mapToXml(wxPay.fillRequestData(data)).getBytes("UTF-8");
            // 发送请求，并得到响应结果
            String xml = restTemplate.postForObject(WXPayConstants.UNIFIEDORDER_URL, request, String.class);
            System.err.println("xml = " + xml);
            // 将结果处理为map
            Map<String, String> result = WXPayUtil.xmlToMap(xml);
            System.err.println("result = " + result);

            // 如果通信失败
            if (WXPayConstants.FAIL.equals(result.get("return_code"))) {
                LOGGER.error("【微信下单】与微信通信失败，错误信息：{}", result.get("result_msg"));
                return null;
            }

            // 如果是下单失败
            String resultCode = result.get("result_code");
            if (WXPayConstants.FAIL.equals(resultCode)) {
                LOGGER.error("【微信下单】创建预交易订单失败，错误码：{}，错误信息：{}", result.get("err_code"), result.get("err_code_des"));
                return null;
            }

            // 校验签名
            this.isSignatureValid(result);

            // 下单成功，获取支付连接
            String url = result.get("code_url");
            // 将支付连接缓存，时间为30分钟
            try {
                this.redisTemplate.opsForValue().set(key, url, 30, TimeUnit.MINUTES);
            } catch (Exception e) {
                LOGGER.error("【微信下单】缓存付款链接异常，订单编号：{}", orderId, e);
            }
            return url;
        } catch (Exception e) {
            LOGGER.error("【微信下单】创建预交易订单异常失败", e);
            return null;
        }
    }

    // 校验签名
    private void isSignatureValid(Map<String, String> result) {
        try {
            boolean bool1 = WXPayUtil.isSignatureValid(result, payConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            boolean bool2 = WXPayUtil.isSignatureValid(result, payConfig.getKey(), WXPayConstants.SignType.MD5);
            if (!bool1 && !bool2) {
                throw new LyException(HttpStatus.INTERNAL_SERVER_ERROR, "签名校验失败！");
            }
        } catch (Exception e) {
            LOGGER.error("【微信支付】校验签名失败，数据：{}", result);
            throw new LyException(HttpStatus.INTERNAL_SERVER_ERROR, "签名校验失败！");
        }
    }

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private PayLogMapper payLogMapper;

    /**
     * 修改支付状态
     *
     * @param msg 信息
     */
    public void handleNotify(Map<String, String> msg) {
        // 1.校验签名
        this.isSignatureValid(msg);
        // 2.校验金额
        // 2.1解析数据
        String totalFee = msg.get("total_fee");
        String outTradeNo = msg.get("out_trade_no");
        String transactionId = msg.get("transaction_id");
        String bankType = msg.get("bank_type");
        if (StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(totalFee) || StringUtils.isBlank(transactionId) || StringUtils.isBlank(bankType)) {
            LOGGER.error("【微信支付回调】支付回调返回的数据不正确");
            throw new LyException(HttpStatus.INTERNAL_SERVER_ERROR, "支付回调的数据有误！");
        }
        // 2.2 查询订单
        Order order = this.orderMapper.selectByPrimaryKey(Long.valueOf(outTradeNo));
        // 2.3 校验金额，此处因为我们支付的都是1，所以写死了，应该与订单中的对比
        if (1L != Long.valueOf(totalFee)) {
            LOGGER.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException(HttpStatus.INTERNAL_SERVER_ERROR, "支付回到返回的数据不正确！");
        }
        // 2.4 判断支付状态
        OrderStatus orderStatus = this.orderStatusMapper.selectByPrimaryKey(order.getOrderId());
        if (orderStatus.getStatus() != OrderStatusEnum.INIT.value()) {
            // 只要不是未支付状态，则都认为支付成功
            return;
        }
        // 3. 修改支付日志状态
        PayLog payLog = this.payLogMapper.selectByPrimaryKey(order.getOrderId());
        // 只有未支付订单才需要修改
        if (payLog.getStatus() == PayStatusEnum.NOT_PAY.value()) {
            payLog.setOrderId(order.getOrderId());
            payLog.setStatus(PayStatusEnum.SUCCESS.value());
            payLog.setTransactionId(transactionId);
            payLog.setBankType(bankType);
            payLog.setPayTime(new Date());
            this.payLogMapper.updateByPrimaryKeySelective(payLog);
        }
        // 4. 修改订单状态表
        OrderStatus status = new OrderStatus();
        status.setOrderId(order.getOrderId());
        status.setPaymentTime(new Date());
        status.setStatus(OrderStatusEnum.PAY_UP.value());
        this.orderStatusMapper.updateByPrimaryKeySelective(status);
    }

    /**
     * 查询支付状态
     */
    public PayState queryPayState(Long orderId) {
        Map<String, String> data = new HashMap<>();
        // 订单号
        data.put("out_trade_no", orderId.toString());
        try {
            Map<String, String> result = this.wxPay.orderQuery(data);
            // 链接失败
            if (result == null || WXPayConstants.FAIL.equals(result.get("return_code"))) {
                // 未查询到结果或链接失败，认为是未付款
                LOGGER.info("【支付状态查询】链接微信服务失败，订单编号：{}", orderId);
                return PayState.NOT_PAY;
            }
            // 查询失败
            if (WXPayConstants.FAIL.equals(result.get("result_code"))) {
                LOGGER.error("【支付状态查询】查询微信订单支付状态失败，错误码：{}，错误信息：{}", result.get("err_code"), result.get("err_code_des"));
                return PayState.NOT_PAY;
            }
            // 校验签名
            this.isSignatureValid(result);
            // 设置支付状态依据
            String state = result.get("trade_state");
            if ("SUCCESS".equals(state)) {
                //修改支付状态信息
                this.handleNotify(result);
                // 返回付款成功
                return PayState.SUCCESS;
            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                // 未付款或正在付款，都认为是未付款
                return PayState.NOT_PAY;
            } else {
                // 其他状态，都认为支付失败
                return PayState.FAIL;
            }
        } catch (Exception e) {
            LOGGER.error("查询订单状态异常", e);
            return PayState.NOT_PAY;
        }
    }
}
