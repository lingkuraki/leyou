package com.leyou.search.pojo;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;

import java.util.List;
import java.util.Map;

public class SearchResult extends PageResult<Goods> {

    private List<Category> categoryList; // 分类过滤条件

    private List<Brand> brandList; // 品牌过滤条件

    private List<Map<String, Object>> specs; // 规格参数过滤条件

    public SearchResult() {
    }

    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categoryList, List<Brand> brandList) {
        super(total, totalPage, items);
        this.categoryList = categoryList;
        this.brandList = brandList;
    }

    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categoryList, List<Brand> brandList, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categoryList = categoryList;
        this.brandList = brandList;
        this.specs = specs;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public List<Brand> getBrandList() {
        return brandList;
    }

    public void setBrandList(List<Brand> brandList) {
        this.brandList = brandList;
    }

    public List<Map<String, Object>> getSpecs() {
        return specs;
    }

    public void setSpecs(List<Map<String, Object>> specs) {
        this.specs = specs;
    }
}
