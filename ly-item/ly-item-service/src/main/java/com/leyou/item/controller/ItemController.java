package com.leyou.item.controller;

import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.Item;
import com.leyou.item.service.ItemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("item")
    public ResponseEntity<Item> saveItem(Item item) {
        if (item.getPrice() == null || StringUtils.isBlank(item.getName())) {
            throw new LyException(HttpStatus.BAD_REQUEST, "价格不能为空");
        }
        Item result = itemService.saveItem(item);
        return ResponseEntity.ok(result);
    }
}
