package com.leyou.seckill.controller;

import com.leyou.seckill.pojo.Item;
import com.leyou.seckill.pojo.SeckillGoods;
import com.leyou.seckill.service.SeckillService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping
    public ResponseEntity<List<SeckillGoods>> queryAllSeckillGoods(@RequestParam(value = "targetTime") String targetTime) {
        List<SeckillGoods> seckillGoodsList = this.seckillService.queryAllSeckillGoods(targetTime);
        if (CollectionUtils.isEmpty(seckillGoodsList)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(seckillGoodsList);
    }

    @GetMapping("{spuId}/{skuId}")
    public ResponseEntity<Item> queryItem(@PathVariable("spuId") Long spuId, @PathVariable("skuId") Long skuId) {
        Item item = this.seckillService.queryItem(spuId, skuId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(item);
    }

}
