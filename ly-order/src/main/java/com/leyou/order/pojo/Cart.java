package com.leyou.order.pojo;

import lombok.Data;

import javax.persistence.Transient;
import java.util.Map;

@Data
public class Cart {

    private Long userId;// 用户ID
    private Long skuId;// 商品id
    private String title;// 标题
    private String image;// 图片
    private Long price;// 加入购物车时的价格
    private Integer num;// 购买数量
    private String ownSpec;// 商品规格参数
    @Transient
    private Integer stock;// 商品的库存量
    @Transient
    private Long secPrice;// 秒杀价
    @Transient
    private Object paramMap;// 参数集合
}
