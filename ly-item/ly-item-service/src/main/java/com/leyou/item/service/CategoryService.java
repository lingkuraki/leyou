package com.leyou.item.service;

import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Items;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryListByParent(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        List<Category> categoryList = this.categoryMapper.select(category);
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(HttpStatus.NOT_FOUND, "当前分类下没有子分类");
        }
        return categoryList;
    }

    @Transactional
    public void addCategory(Category category) {
        category.setId(null);
        this.categoryMapper.insert(category);
        this.categoryMapper.updateIsParent(1, category.getParentId());
    }

    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }

    public void updateCategory(Category category) {
        this.categoryMapper.updateCategory(category);
    }

    @Transactional
    public void deleteCategory(Long cid) {
        Long parentId = this.categoryMapper.selectParentIdById(cid);
        this.categoryMapper.deleteByCategoryId(cid);
        int count = this.categoryMapper.countIdByParentId(parentId);
        if (count == 0) {
            this.categoryMapper.updateIsParent(0, parentId);
        }
    }

    public List<String> queryNameByIds(List<Long> ids) {
        List<Category> categoryList = this.categoryMapper.selectByIdList(ids);
        return categoryList.stream().map(Category::getName).collect(Collectors.toList());
    }

    public List<Category> queryCategoryByIds(List<Long> ids) {
        return categoryMapper.selectByIdList(ids);
    }

    public List<Category> queryAllByCid3(Long id) {
        Category category1 = this.categoryMapper.selectByPrimaryKey(id);
        Category category2 = this.categoryMapper.selectByPrimaryKey(category1.getParentId());
        Category category3 = this.categoryMapper.selectByPrimaryKey(category2.getParentId());
        return Arrays.asList(category1, category2, category3);
    }

    public List<Items> queryTwoAndThreeCategory(Long id) {
        List<Items> itemsList = new ArrayList<>();
        List<Category> categoryList = this.queryListByParent(id);
        categoryList.forEach(category -> {
            Items items = new Items();
            items.setCategory(category);
            items.setCategories(this.queryListByParent(category.getId()));
            itemsList.add(items);
        });
        return itemsList;
    }
}
