package com.leyou.home.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.leyou.home.utils.LongJsonDeserializer;
import com.leyou.home.utils.LongJsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    // 创建订单日期
    private Date createTime;
    // 订单编号
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    private Long orderId;
    // 商品实际支付价格
    private Long actualPrice;
    // 订单支付类型
    private Integer paymentType;
    // 商品详情
    private List<OrderDetailDTO> orderDetailList = new ArrayList<>();
    // 订单状态码
    private Integer status;

}
