package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    public PageResult<SpuBo> querySpuByPageAndSort(Integer page, Integer rows, Boolean saleable, String key) {
        // 1.查询SPU
        // 分页，最多允许查询100条
        PageHelper.startPage(page, Math.min(rows, 200));

        // 创建查询条件
        Example example = new Example(Spu.class);
        Criteria criteria = example.createCriteria();

        // 是否过滤上架
        if (saleable != null) criteria.orEqualTo("saleable", saleable);

        // 是否模糊查询
        if (StringUtils.isNotBlank(key)) criteria.andLike("title", "%" + key + "%");
        Page<Spu> pageInfo = (Page<Spu>) this.spuMapper.selectByExample(example);

        List<SpuBo> list = pageInfo.getResult().stream().map(spu -> {
            // 把spu变为 spuBo
            SpuBo spuBo = new SpuBo();
            // 属性拷贝
            BeanUtils.copyProperties(spu, spuBo);
            // 2.查询spu的商品分类名称，要查三级分类
            List<String> names = this.categoryService.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            // 把分类名称拼接后存入
            spuBo.setCname(StringUtils.join(names, "/"));

            // 3.查询spu的品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            return spuBo;
        }).collect(Collectors.toList());
        return new PageResult<>(pageInfo.getTotal(), list);
    }

    @Transactional
    public void save(SpuBo spuBo) {
        // 1.保存spu
        /* ----- 设置固定参数 ----- */
        spuBo.setSaleable(true); // 设置是否上架
        spuBo.setValid(true); // 设置是否有效
        spuBo.setCreateTime(new Date());// 设置添加事件
        spuBo.setLastUpdateTime(spuBo.getCreateTime());// 设置最后更新时间
        this.spuMapper.insert(spuBo);
        // 2.保存spu详情
        spuBo.getSpuDetail().setSpuId(spuBo.getId());
        this.spuDetailMapper.insert(spuBo.getSpuDetail());
        // 3.保存sku和库存信息
        saveSkuAndStock(spuBo.getSkus(), spuBo.getId());

        this.sendMessage(spuBo.getId(), "insert");
    }

    @Transactional
    public void updateGoods(SpuBo spuBo) {
        // 更新spu
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        // 查询以前的sku
        List<Sku> skuList = this.querySkuBySpuId(spuBo.getId());
        // 1.如果该集合不为null，则删除
        if (!CollectionUtils.isEmpty(skuList)) {
            List<Long> ids = skuList.stream().map(s -> s.getId()).collect(Collectors.toList());
            // 删除以前库存
            Example example = new Example(Stock.class);
            Criteria criteria = example.createCriteria();
            criteria.andIn("skuId", ids);
            this.stockMapper.deleteByExample(example);
            // 删除以前的sku
            Sku sku = new Sku();
            sku.setSpuId(spuBo.getId());
            this.skuMapper.delete(sku);
        }
        // 2.新增sku和库存
        saveSkuAndStock(spuBo.getSkus(), spuBo.getId());
        // 3.更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        this.sendMessage(spuBo.getId(), "update");
    }

    private void saveSkuAndStock(List<Sku> skus, Long spuId) {
        for (Sku sku : skus) {
            if (!sku.getEnable()) continue;
            // 保存sku
            sku.setSpuId(spuId);
            // 初始化事件
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);
            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    public SpuDetail querySpuDetailById(Long id) {
        return this.spuDetailMapper.selectByPrimaryKey(id);
    }

    public List<Sku> querySkuBySpuId(Long id) {
        // 查询sku
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = this.skuMapper.select(sku);
        for (Sku sku1 : skuList) {
            sku1.setStock(this.stockMapper.selectByPrimaryKey(sku1.getId()).getStock());
        }
        return skuList;
    }

    public void updateSaleableById(Long id, Boolean saleable) {
        // 修改spu表中的是否上架字段
        this.spuMapper.updateSaleableById(id, saleable);
    }

    @Transactional
    public void deleteGoodsById(Long id) {
        // 1.删除spu表中id数据
        this.spuMapper.deleteByPrimaryKey(id);
        // 2.删除spu_detail表中id数据
        this.spuDetailMapper.deleteByPrimaryKey(id);
        // 3.删除sku表中id数据
        List<Long> skuIds = this.skuMapper.selectSkuIdBySpuId(id);
        this.skuMapper.deleteBySpuId(id);
        // 4.删除stock表中的库存
        skuIds.forEach(skuId -> this.stockMapper.deleteByPrimaryKey(skuId));

        this.sendMessage(id, "delete");
    }

    public SpuBo querySpuById(Long id) {
        // 查询Spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        // 属性拷贝
        SpuBo spuBo = new SpuBo();
        BeanUtils.copyProperties(spu, spuBo);
        // 查询spu下的sku集合
        spuBo.setSkus(querySkuBySpuId(id));
        // 查询spuDetail
        spuBo.setSpuDetail(querySpuDetailById(id));
        return spuBo;
    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsService.class);

    // 发送消息到rabbitmq的方法
    private void sendMessage(Long id, String type) {
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            LOGGER.error("{}商品消息发送异常：商品id：{}", type, id, e);
        }
    }
}
