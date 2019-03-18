package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecificationClient specificationClient;

    public Map<String, Object> loadData(Long spuId) {
        // 查询spu
        SpuBo spu = this.goodsClient.querySpuById(spuId);
        // 查询分类
        List<Category> categoryList = this.categoryClient.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询品牌
        Brand brand = this.brandClient.queryById(spu.getBrandId());
        // 查询规格参数组
        List<SpecGroup> specGroupList = this.specificationClient.querySpecsByCid(spu.getCid3());

        // 查询规格参数
        List<SpecParam> paramList = this.specificationClient.querySpecParams(null, spu.getCid3(), null, false);
        Map<Long, String> paramMap = new HashMap<>();
        paramList.forEach(param -> paramMap.put(param.getId(), param.getName()));

        // 封装数据
        Map<String, Object> data = new HashMap<>();
        data.put("specs", specGroupList);
        data.put("brand", brand);
        data.put("categories", categoryList);
        data.put("skus", spu.getSkus());
        data.put("spuDetail", spu.getSpuDetail());
        data.put("paramMap", paramMap);

        // 防止数据重复
        spu.setSkus(null);
        spu.setSpuDetail(null);
        data.put("spu", spu);
        return data;
    }

}
