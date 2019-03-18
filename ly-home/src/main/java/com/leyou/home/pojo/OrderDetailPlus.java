package com.leyou.home.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailPlus {

    // 订单商品详情
    private OrderDTO orderDTO;
    // 订单商品状态
    private OrderStatus orderStatus;
    // 收货地址
    private String address;
}
