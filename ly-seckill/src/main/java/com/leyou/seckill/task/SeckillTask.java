package com.leyou.seckill.task;

import com.leyou.auth.utils.ObjectUtils;
import com.leyou.common.utils.JsonUtils;
import com.leyou.seckill.mapper.SeckillGoodsMapper;
import com.leyou.seckill.pojo.SeckillGoods;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SeckillTask {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    private static final String SECKILL = "ly:seckill:goods";

    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("执行了秒杀商品增量更新的任务调度：" + LocalDate.now());
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(SECKILL);
        // 查询缓存中的秒杀商品集合，将返回的集合转换为List集合
        Set<Object> set = hashOps.keys();
        List<Object> objList = new ArrayList<>(set);
        List<Long> list = objList.stream().map(ObjectUtils::toLong).collect(Collectors.toList());
        // 向数据库中查询商品
        Example example = new Example(SeckillGoods.class);
        Criteria criteria = example.createCriteria();
        criteria.andLessThan("seckillTotal", 30);
        criteria.andGreaterThanOrEqualTo("endTime", new Date());
        criteria.andGreaterThan("seckillStock", 0);
        criteria.andEqualTo("enable", 1);
        if (CollectionUtils.isNotEmpty(list)) criteria.andNotIn("skuId", list);
        List<SeckillGoods> seckillGoodsList = this.seckillGoodsMapper.selectByExample(example);
        System.out.println("seckillGoodsList = " + seckillGoodsList);
        // 将列表数据装入缓存
        seckillGoodsList.forEach(seckillGoods -> hashOps.put(seckillGoods.getSkuId().toString(), JsonUtils.serialize(seckillGoods)));
    }

    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(SECKILL);
        // 查询出缓存中的数据，扫描每条记录，判断时间，如果当前时间超过了截止时间，就移除此记录
        List<Object> objectList = hashOps.values();
        if (CollectionUtils.isEmpty(objectList)) return;
        List<SeckillGoods> seckillGoodsList = objectList.stream().map(obj -> JsonUtils.parse(obj.toString(), SeckillGoods.class)).collect(Collectors.toList());
        seckillGoodsList.forEach(seckillGoods -> {
            if (seckillGoods.getEndTime().getTime() < new Date().getTime() || seckillGoods.getSeckillTotal() >= seckillGoods.getSeckillStock()) {
                // 将缓存中的数据同步到数据库中
                this.seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                // 清除缓存
                hashOps.delete(seckillGoods.getSkuId().toString());
                System.out.println("秒杀商品" + seckillGoods.getId() + "已经过期！");
            }
        });
    }
}
