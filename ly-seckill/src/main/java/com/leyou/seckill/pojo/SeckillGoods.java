package com.leyou.seckill.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Table(name = "tb_seckill_goods")
@Data
public class SeckillGoods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long skuId;
    private Long spuId;
    private String title;
    private String images;
    private Long price;// 原价
    private Long secPrice;// 秒杀价
    private String ownSpec;// 商品特殊规格的键值对
    private String indexes;// 商品特殊规格的下标
    private Boolean enable;// 是否有效。逻辑删除使用
    private Date startTime;// 秒杀开始时间
    private Date endTime;// 秒杀结束时间
    private Integer seckillStock;// 秒杀库存
    private Integer seckillTotal;// 已秒杀数量

}
