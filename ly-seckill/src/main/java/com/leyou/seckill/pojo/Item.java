package com.leyou.seckill.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    private SeckillGoods seckillGoods;

    private Map<String, Object> data;


}
