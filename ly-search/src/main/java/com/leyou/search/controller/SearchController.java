package com.leyou.search.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 搜索商品
     * @param searchRequest 搜索条件
     * @return 查询结果集
     */
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody(required = false) SearchRequest searchRequest) {
        PageResult<Goods> result = this.searchService.search(searchRequest);
        return ResponseEntity.ok(result);
    }
}
