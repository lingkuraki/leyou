package com.leyou.order.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    @NotNull
    private Address address;// 收货人地址
    @NotNull
    private Integer paymentType;// 付款类型
    @NotNull
    private List<OrderDetail> orderDetails;// 订单详情
    @NotNull
    private Long totalPrice;// 总金额
    @NotNull
    private Long actualPrice;// 实付金额
}
