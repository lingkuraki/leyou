package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPageAndSort(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        // 开始分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)) {
            example.createCriteria().andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }
        // 排序
        if (StringUtils.isNotBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        // 查询
        Page<Brand> pageInfo = (Page<Brand>) brandMapper.selectByExample(example);
        // 返回结果
        return new PageResult<>(pageInfo.getTotal(), pageInfo);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌信息
        this.brandMapper.insertSelective(brand);
        // 新增品牌和分类中间表
        cids.forEach(cid -> this.brandMapper.insertCategoryBrand(cid, brand.getId()));
    }

    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        // 修改品牌信息
        this.brandMapper.updateByPrimaryKey(brand);
        // 修改品牌和分类中间表
        // 1.先将数据库中中间表的对应数据删除
        this.brandMapper.deleteByBrandId(brand.getId());
        // 2.再将新的商品匪类加入到中间表中
        for (Long cid : cids) {
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }

    @Transactional
    public void deleteBrand(Long bid) {
        // 先删除tb_category中的品牌数据
        this.brandMapper.deleteByPrimaryKey(bid);
        // 再删除tb_category_brand表中的级联信息
        this.brandMapper.deleteByBrandId(bid);
    }

    public List<Brand> queryByCategoryId(Long cid) {
        return this.brandMapper.queryByCategoryId(cid);
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }

    public Brand queryById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

}
