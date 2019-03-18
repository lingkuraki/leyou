package com.leyou.seckill.service;

import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.seckill.client.GoodsClient;
import com.leyou.seckill.client.SpecificationClient;
import com.leyou.seckill.mapper.SeckillGoodsMapper;
import com.leyou.seckill.pojo.Item;
import com.leyou.seckill.pojo.SeckillGoods;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeckillService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SECKILL = "ly:seckill:goods";

    public List<SeckillGoods> queryAllSeckillGoods(String targetTime) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (this.redisTemplate.hasKey(SECKILL)) {
            // 从缓存中获取
            List<Object> objectList = this.redisTemplate.boundHashOps(SECKILL).values();
            if (CollectionUtils.isEmpty(objectList)) return null;
            return objectList.stream().map(obj -> JsonUtils.parse(obj.toString(), SeckillGoods.class))
                    .filter(good -> targetTime.equals(format.format(good.getStartTime()))).collect(Collectors.toList());
        } else {
            // 从数据库中获取
            List<SeckillGoods> seckillGoodsList = this.seckillGoodsMapper.selectAll();
            // 将数据存入缓存
            seckillGoodsList.forEach(good -> this.redisTemplate.boundHashOps(SECKILL).put(good.getSkuId().toString(), JsonUtils.serialize(good)));
            return seckillGoodsList.stream().filter(good -> targetTime.equals(format.format(good.getStartTime()))).collect(Collectors.toList());
        }
    }

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    private final static String DETAIL = "ly:seckill:detail";

    public Item queryItem(Long spuId, Long skuId) {

        if (this.redisTemplate.hasKey(DETAIL) && this.redisTemplate.boundHashOps(DETAIL).get(skuId.toString()) != null) {
            // 从缓存中获取
            String json = this.redisTemplate.boundHashOps(DETAIL).get(skuId.toString()).toString();
            return JsonUtils.parse(json, Item.class);
        } else {
            // 根据spuId获取商品详情
            SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spuId);
            // 根据skuId查询秒杀商品的信息
            SeckillGoods seckillGoods = this.seckillGoodsMapper.queryBySkuId(skuId);
            // 根据spuId获取该商品属于哪个分类
            Long cid = this.seckillGoodsMapper.queryCid3BySpuId(spuId);
            // 根据cid获取该商品所有规格参数组
            List<SpecGroup> specGroups = this.specificationClient.querySpecsByCid(cid);
            // 查询规格参数
            List<SpecParam> paramList = this.specificationClient.querySpecParams(null, cid, null, null);
            Map<Long, String> paramMap = new HashMap<>();
            paramList.forEach(param -> paramMap.put(param.getId(), param.getName()));
            // 封装数据
            Map<String, Object> data = new HashMap<>();
            data.put("spuDetail", spuDetail);
            data.put("specGroups", specGroups);
            data.put("paramMap", paramMap);
            Item item = new Item();
            // 设置item对象
            item.setSeckillGoods(seckillGoods);
            item.setData(data);
            this.redisTemplate.boundHashOps(DETAIL).put(item.getSeckillGoods().getSkuId().toString(), JsonUtils.serialize(item));
            return item;
        }
    }
}
