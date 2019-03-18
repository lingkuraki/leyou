package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping
@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     *
     * @param cart 购物车
     * @return 返回void
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        this.cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询购物车列表
     *
     * @return 该用户的购物车集合
     */
    @GetMapping
    public ResponseEntity<List<Cart>> queryCartList() {
        List<Cart> cartList = this.cartService.queryCartList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(cartList);
    }

    /**
     * 修改数量
     *
     * @param skuId 商品skuId
     * @param num   商品数量
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        this.cartService.updateNum(skuId, num);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{skuId}")
    public ResponseEntity<Cart> deleteCart(@PathVariable("skuId") String skuId) {
        Cart cart = this.cartService.deleteCart(skuId);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/merge")
    public ResponseEntity<Void> mergeCart(@RequestBody List<Cart> cartList) {
        this.cartService.mergeCart(cartList);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/checked/{skuIds}")
    public ResponseEntity<List<Cart>> deleteCheckedCart(@PathVariable("skuIds") List<Long> skuIds) {
        List<Cart> cartList = this.cartService.deleteCheckedCart(skuIds);
        return ResponseEntity.ok(cartList);
    }

    @GetMapping("pastedCart")
    public ResponseEntity<List<Cart>> queryPastedCart() {
        return ResponseEntity.ok(this.cartService.queryPastedCart());
    }

    @PostMapping("reAddToCart")
    public ResponseEntity<Void> reAddToCart(@RequestBody Cart cart){
        this.cartService.reAddToCart(cart);
        return ResponseEntity.ok().build();
    }
}
