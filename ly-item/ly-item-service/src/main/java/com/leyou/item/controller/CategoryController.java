package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Item;
import com.leyou.item.pojo.Items;
import com.leyou.item.service.CategoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点查询商品类目
     *
     * @param pid 商品ID
     * @return 返回商品类别的集合的json格式数据
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryByParentId(@RequestParam(value = "pid", defaultValue = "0") Long pid) {
        return ResponseEntity.ok(this.categoryService.queryListByParent(pid));
    }

    @PostMapping("list")
    public ResponseEntity<Void> addCategory(@RequestBody Category category) {
        this.categoryService.addCategory(category);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("list")
    public ResponseEntity<Void> updateCategory(@RequestBody Category category) {
        this.categoryService.updateCategory(category);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("list/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long cid) {
        this.categoryService.deleteCategory(cid);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid) {
        List<Category> categoryList = this.categoryService.queryByBrandId(bid);
        if (categoryList == null || categoryList.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(categoryList);
    }

    /**
     * 根据商品分类id查询名称
     *
     * @param ids 要查询的分类id集合
     * @return 多个名称的集合
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids") List<Long> ids) {
        List<String> list = this.categoryService.queryNameByIds(ids);
        if (list == null || list.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

    /**
     * 根据商品分类的id查询商品分类
     *
     * @param ids 商品id
     * @return 返回商品分类集合
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(categoryService.queryCategoryByIds(ids));
    }

    @GetMapping("all/level/{id}")
    public ResponseEntity<List<Category>> queryAllByCid3(@PathVariable("id") Long id) {
        List<Category> categoryList = this.categoryService.queryAllByCid3(id);
        if (categoryList == null || categoryList.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(categoryList);
    }

    @GetMapping("index/{cid1}")
    public ResponseEntity<List<Items>> queryTwoAndThreeCategory(@PathVariable("cid1")Long id){
        List<Items> itemsList = this.categoryService.queryTwoAndThreeCategory(id);
        if (CollectionUtils.isEmpty(itemsList)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(itemsList);
    }


}
