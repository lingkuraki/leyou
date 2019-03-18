package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    // 分页查询所有品牌信息
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key) {
        PageResult<Brand> result = this.brandService.queryBrandByPageAndSort(page, rows, sortBy, desc, key);
        if (result == null || result.getItems().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    // 新增品牌
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        this.brandService.saveBrand(brand, cids);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // 修改品牌
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        this.brandService.updateBrand(brand, cids);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // 删除品牌
    @DeleteMapping("{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bid") Long bid) {
        this.brandService.deleteBrand(bid);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 根据商品分类查询该分类下的商品品牌
     *
     * @param cid 分类id
     * @return 返回品牌集合
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryByCategoryId(@PathVariable("cid") Long cid) {
        List<Brand> brandList = this.brandService.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(brandList)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(brandList);
    }

    /**
     * 根据多个id查询品牌
     *
     * @param ids 品牌id集合
     * @return 返回品牌集合
     */
    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids) {
        List<Brand> brandList = this.brandService.queryBrandByIds(ids);
        if (brandList == null || brandList.size() == 0) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(brandList);
    }

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(brandService.queryById(id));
    }
}
