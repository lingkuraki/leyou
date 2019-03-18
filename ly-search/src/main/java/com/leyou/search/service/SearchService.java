package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elTemplate;


    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    public Goods buildGoods(SpuBo spu) {
        Long id = spu.getId();
        // 查询规格模板
        List<SpecParam> specParamList = this.specClient.querySpecParams(null, spu.getCid3(), true, null);
        // 查询sku信息
        List<Sku> skus = this.goodsClient.querySkuBySpuId(id);
        // 查询详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(id);
        // 查询商品分类名称
        List<String> categoryNames = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 准备sku集合
        List<Map<String, Object>> skuList = new ArrayList<>();
        // 准备价格集合
        Set<Long> price = new HashSet<>();
        for (Sku s : skus) {
            price.add(s.getPrice());
            Map<String, Object> sku = new HashMap<>();
            sku.put("id", s.getId());
            sku.put("price", s.getPrice());
            sku.put("image", StringUtils.isBlank(s.getImages()) ? "" : s.getImages().split(",")[0]);
            sku.put("title", s.getTitle());
            skuList.add(sku);
        }

        // 获取商品详情中的规格参数
        Map<String, Object> specialSpecs = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<String, Object>>() {
        });
        // 取出通用参数
        Map<String, Object> genericSpecs = JsonUtils.nativeRead(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        // 规格参数map:其key来自于tb_spec_param的规格参数，其值来自于spuDetail中的specialSpec和genericSpec
        Map<String, Object> specMap = new HashMap<>();
        // 封装规格参数
        specParamList.forEach(p -> {
            // 判断是否可以搜索
            if (p.getSearching()) {
                // 判断是否是通用属性
                if (p.getGeneric()) {
                    String value = genericSpecs.get(p.getId().toString()).toString();
                    // 判断该规格参数是否是数值类型
                    if (p.getNumeric()) {
                        // 对value分段
                        value = chooseSegment(value, p);
                    }
                    specMap.put(p.getName(), StringUtils.isBlank(value) ? "其它" : value);
                } else {
                    specMap.put(p.getName(), specialSpecs.get(p.getId().toString()));
                }
            }
        });
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(id);
        goods.setSubTitle(spu.getSubTitle());
        goods.setAll(spu.getTitle() + " " + StringUtils.join(categoryNames, " ")); //全文检索字段
        goods.setPrice(new HashSet<>(price));
        goods.setSkus(JsonUtils.serialize(skuList));
        goods.setSpecs(specMap);
        return goods;
    }

    private String chooseSegment(String value, SpecParam specParam) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : specParam.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 读取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + specParam.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + specParam.getUnit() + "以下";
                } else {
                    result = segment + specParam.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest searchRequest) {
        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (searchRequest == null || StringUtils.isBlank(searchRequest.getKey()))
            throw new LyException(HttpStatus.BAD_REQUEST, "查询条件不能为空!");
        String key = searchRequest.getKey();
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 1.1通过sourceFilter，设置返回的结果字段，我们只需要id，skus，subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        // 1.2.对key进行全文检所查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND));
        // 1.3分页
        this.searchWithPageAndSort(searchRequest, queryBuilder);
        // 2 搜索条件
        QueryBuilder basicQuery = this.buildBasicQueryWithFilter(searchRequest);
        queryBuilder.withQuery(basicQuery);

        // 3.聚合
        String categoryAggName = "category"; // 商品分类聚合名称
        String brandAggName = "brand"; // 品牌聚合名称
        // 对商品分类进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 对品牌进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 4.查询，获取结果
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

        // 解析查询结果
        // 5.封装结果并返回
        // 5.1 总条数
        Long total = pageInfo.getTotalElements();
        // 5.2 总页数
        Long totalPage = new Double(Math.ceil(1.0 * total / searchRequest.getSize())).longValue();
        // 5.3 商品集合
        List<Goods> list = pageInfo.getContent();
        // 5.4 商品分类的聚合结果
        List<Category> categoryList = this.getCategoryAggResult(pageInfo.getAggregation(categoryAggName));
        // 5.5 品牌的聚合结果
        List<Brand> brandList = this.getBrandAggResult(pageInfo.getAggregation(brandAggName));

        // 判断商品分类数量，看是否需要对规格参数进行聚合
        List<Map<String, Object>> specs;
        if (categoryList.size() == 1) {
            // 如果分类只剩下一个，才进行规格参数过滤
            specs = this.getSpecs(categoryList.get(0).getId(), basicQuery);
            return new SearchResult(total, totalPage, list, categoryList, brandList, specs);
        }
        // 返回结果
        return new SearchResult(total, totalPage, list, categoryList, brandList);
    }

    // 聚合规格参数
    private List<Map<String, Object>> getSpecs(Long cid, QueryBuilder basicQuery) {
        try {
            // 根据分类查询规格
            List<SpecParam> specParamList = this.specClient.querySpecParams(null, cid, true, null);
            // 创建集合，保存规格过滤条件
            List<Map<String, Object>> specs = new ArrayList<>();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(basicQuery);
            // queryBuilder.withPageable(PageRequest.of(0, 1));
            // 聚合规格参数
            specParamList.forEach(param -> {
                String name = param.getName();
                queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
            });

            // 搜索聚合结果
            AggregatedPage<Goods> aggregatedPage = this.elTemplate.queryForPage(queryBuilder.build(), Goods.class);

            // 解析聚合结果
            Aggregations aggs = aggregatedPage.getAggregations();
            specParamList.forEach(param -> {
                StringTerms terms = aggs.get(param.getName());
                // 创建结果map
                Map<String, Object> spec = new HashMap<>();
                if (!"品牌".equals(param.getName())) {
                    spec.put("key", param.getName());
                    spec.put("options", terms.getBuckets().stream().map(StringTerms.Bucket::getKeyAsString));
                    specs.add(spec);
                }
            });
            return specs;
        } catch (Exception e) {
            logger.error("规格聚合出现异常：", e);
            return null;
        }
    }

    // 构建基本查询条件
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()));

        // 整理过滤条件
        Map<String, String> filter = searchRequest.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 商品分类和品牌要特殊处理
            if (!"cid3".equals(key) && !"brandId".equals(key)) {
                key = "specs." + key + ".keyword";
            }
            // 添加过滤条件
            queryBuilder.filter(QueryBuilders.termQuery(key, value));
        }
        return queryBuilder;
    }

    // 解析品牌聚合结果
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        try {
            // 转换数据类型
            LongTerms brandAgg = (LongTerms) aggregation;
            // 从桶中获取bid
            List<Long> bids = brandAgg.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            // 查询
            List<Brand> brandList = this.brandClient.queryBrandByIds(bids);
            return brandList;
        } catch (Exception e) {
            logger.error("解析品牌数据错误", e);
            return null;
        }
    }

    // 解析商品分类聚合结果
    private List<Category> getCategoryAggResult(Aggregation aggregation) {
        try {
            List<Category> categoryList = new ArrayList<>();
            // 转换数据类型
            LongTerms categoryAgg = (LongTerms) aggregation;
            // 从桶中获取cid
            List<Long> cids = categoryAgg.getBuckets().stream().map(c -> c.getKeyAsNumber().longValue()).collect(Collectors.toList());
            // 查询,根据id查询分类名称
            List<String> nameList = this.categoryClient.queryNameByIds(cids);

            for (int i = 0; i < nameList.size(); i++) {
                Category category = new Category();
                category.setId(cids.get(i));
                category.setName(nameList.get(i));
                categoryList.add(category);
            }
            return categoryList;
        } catch (Exception e) {
            logger.error("解析商品分类数数据错误", e);
            return null;
        }
    }

    // 构建基本查询条件
    private void searchWithPageAndSort(SearchRequest searchRequest, NativeSearchQueryBuilder queryBuilder) {
        // 准备分页参数
        int page = searchRequest.getPage();
        int size = searchRequest.getSize();

        // 分页
        queryBuilder.withPageable(PageRequest.of(page - 1, size));
        // 排序
        String sortBy = searchRequest.getSortBy();
        Boolean desc = searchRequest.getDescending();
        if (StringUtils.isNotBlank(sortBy)) {
            // 如果sortBy不为空，则进行排序
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }
    }

    // 创建索引
    public void createIndex(Long id) throws IOException {
        SpuBo spu = this.goodsClient.querySpuById(id);
        // 构建商品
        Goods goods = this.buildGoods(spu);
        // 保存数据到索引库
        this.goodsRepository.save(goods);
    }

    // 删除索引
    public void deleteIndex(Long id) {
        this.goodsRepository.deleteById(id);
    }
}
