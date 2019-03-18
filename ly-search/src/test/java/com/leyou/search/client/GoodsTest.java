package com.leyou.search.client;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.SpuBo;
import com.leyou.search.LySearchService;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void deleteIndex() {
        elasticsearchTemplate.deleteIndex("goods");
    }

    @Test
    public void testCreate() {
        // 创建索引
        elasticsearchTemplate.createIndex(Goods.class);
        // 配置映射
        elasticsearchTemplate.putMapping(Goods.class);
    }

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    public void testLoadData(){
        // 创建索引
        elasticsearchTemplate.createIndex(Goods.class);
        // 配置映射
        elasticsearchTemplate.putMapping(Goods.class);
        int page = 1, rows = 100, size = 0;
        do {
            PageResult<SpuBo> result = goodsClient.querySpuBuPage(page, rows, true, null);
            List<SpuBo> spus = result.getItems();
            if (CollectionUtils.isEmpty(spus)) {
                break;
            }

            List<Goods> goodsList = spus.stream()
                    .map(spu -> searchService.buildGoods(spu)).collect(Collectors.toList());

            goodsRepository.saveAll(goodsList);
            size = spus.size();
            page++;
        }while (size == 100);
    }

    @Test
    public void loadData() {
        int page = 1, rows = 100, size = 0;
        do {
            // 查询分页数据
            PageResult<SpuBo> result = this.goodsClient.querySpuBuPage(page, rows, true, null);
            List<SpuBo> spus = result.getItems();
            if (CollectionUtils.isEmpty(spus)) {
                break;
            }
            // 创建Goods集合

            // 遍历spu
            List<Goods> goodsList = new ArrayList<>();
            for (SpuBo spu : spus) {
                try {
                    Goods goods = this.searchService.buildGoods(spu);
                    goodsList.add(goods);
                } catch (Exception e) {
                    continue;
                }
            }
            this.goodsRepository.saveAll(goodsList);
            // 设置条数
            size = spus.size();
            page++;
        } while (size == 100);
    }
}