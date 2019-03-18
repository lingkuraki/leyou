package com.leyou.home.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.home.interceptor.LoginInterceptor;
import com.leyou.home.mapper.OrderDetailMapper;
import com.leyou.home.mapper.OrderMapper;
import com.leyou.home.mapper.OrderStatusMapper;
import com.leyou.home.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    public List<OrderDTO> queryOrderDTO() {
        // 获取当前登录用户id
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        if (userInfo == null) return null;
        Long userId = userInfo.getId();
        // 定义OrderDTO集合
        List<OrderDTO> orderDTOList = new ArrayList<>();
        // 根据此id查询该用户的所用订单
        List<Order> orderList = this.queryOrderByUserId(userId);
        orderList.forEach(order -> {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderId(order.getOrderId());
            orderDTO.setActualPrice(order.getActualPay());
            orderDTO.setPaymentType(order.getPaymentType());
            orderDTO.setCreateTime(order.getCreateTime());
            // 设置List<OrderDetailDTO>属性
            orderDTO.setOrderDetailList(this.queryDetailByOrderId(order.getOrderId(), orderDTO));
            orderDTO.setStatus(this.orderStatusMapper.queryStatusByOrderId(order.getOrderId()));
            orderDTOList.add(orderDTO);
        });
        return orderDTOList;
    }

    // 查询用户的所有订单,并封装到OrderDetailDTO中
    private List<Order> queryOrderByUserId(Long userId) {
        return this.orderMapper.queryOrderByUserId(userId);
    }

    // 根据订单id查询所有该订单下所有的订单详情
    private List<OrderDetailDTO> queryDetailByOrderId(Long orderId, OrderDTO orderDTO) {
        OrderDetail orderDetail1 = new OrderDetail();
        orderDetail1.setOrderId(orderId);
        List<OrderDetail> orderDetailList = this.orderDetailMapper.select(orderDetail1);
        orderDetailList.forEach(orderDetail -> {
            OrderDetailDTO orderDetailDTO = new OrderDetailDTO();
            orderDetailDTO.setOrderId(orderDetail.getOrderId());
            orderDetailDTO.setNum(orderDetail.getNum());
            orderDetailDTO.setPrice(orderDetail.getPrice());
            orderDetailDTO.setTitle(orderDetail.getTitle());
            orderDetailDTO.setImage(orderDetail.getImage());
            orderDetailDTO.setOwnSpec(orderDetail.getOwnSpec());
            orderDetailDTO.setSecPrice(orderDetail.getSecPrice());
            orderDTO.getOrderDetailList().add(orderDetailDTO);
        });
        return orderDTO.getOrderDetailList();
    }

    /**
     * 提醒发货
     *
     * @param orderId 订单id
     */
    public void updateOrderToShipment(Long orderId) {
        this.sendMessage("shipment", orderId);
    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    // 发送消息
    private void sendMessage(String type, Long orderId) {
        try {
            this.amqpTemplate.convertAndSend("homeOrder." + type, orderId);
        } catch (Exception e) {
            LOGGER.error("提醒商家商品发货异常，商品id：{}", orderId, e);
        }
    }

    /**
     * 确认收货
     *
     * @param orderId 订单id
     */
    public void updateOrderToReceive(Long orderId) {
        Date date = new Date();
        this.orderStatusMapper.updateOrderToReceive(orderId, date);
    }

    @Transactional
    public void updateOrderToEvaluate(Long orderId) {
        Date commentTime = new Date();
        String[] messages = {"我用双手成就你的梦想", "面对疾风吧", "我于杀戮之中绽放", "亦如黎明中的花朵", "所有人都得死", "是时候展现真正的技术了"};
        String message = messages[new Random().nextInt(6)];
        this.orderMapper.updateMessageByOrderId(message, orderId);
        this.orderStatusMapper.updateStatusByOrderId(commentTime, orderId);
    }

    public OrderDetailPlus queryOneOrderDTOByOrderId(Long orderId) {
        // 查询当前用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        if (userInfo == null) return null;
        // 根据orderId查询订单
        Order order = this.orderMapper.selectByPrimaryKey(orderId);
        // 创建一个OrderDTO对象
        OrderDetailPlus orderDetailPlus = new OrderDetailPlus();
        OrderDTO orderDTO = new OrderDTO();
        // 封装数据
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setActualPrice(order.getActualPay());
        orderDTO.setPaymentType(order.getPaymentType());
        orderDTO.setCreateTime(order.getCreateTime());
        // 1.1设置List<OrderDetailDTO>属性
        orderDTO.setOrderDetailList(this.queryDetailByOrderId(order.getOrderId(), orderDTO));
        orderDetailPlus.setOrderStatus(this.orderStatusMapper.selectByPrimaryKey(orderId));
        orderDTO.setStatus(orderDetailPlus.getOrderStatus().getStatus());
        orderDetailPlus.setOrderDTO(orderDTO);
        // 1.2 设置订单商品派送地址
        String address = order.getReceiverState() + order.getReceiverCity() + order.getReceiverDistrict() + order.getReceiverAddress();
        orderDetailPlus.setAddress(address);
        return orderDetailPlus;
    }
}
