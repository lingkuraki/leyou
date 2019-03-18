package com.leyou.home.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.leyou.home.utils.LongJsonDeserializer;
import com.leyou.home.utils.LongJsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDTO {

    // 订单编号
    @JsonSerialize(using = LongJsonSerializer.class)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    private Long orderId;
    // 商品单价
    private Long price;
    // 商品数量
    private Integer num;
    // 商品标题
    private String title;
    // 商品规格数据
    private String ownSpec;
    // 图片
    private String image;
    // 秒杀商品单价
    private Long secPrice;

}
