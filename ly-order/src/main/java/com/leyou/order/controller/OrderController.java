package com.leyou.order.controller;

import com.leyou.order.pojo.Cart;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDTO;
import com.leyou.order.service.OrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("carts")
    public ResponseEntity<List<Cart>> queryCarts(@RequestParam("skuIds") List<Long> skuIds) {
        List<Cart> cartList = this.orderService.queryCarts(skuIds);
        if (CollectionUtils.isEmpty(cartList)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(cartList);
    }

    @PostMapping("order")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDTO orderDTO) {
        Order order = this.orderService.createOrder(orderDTO);
        return ResponseEntity.ok(order);
    }

    @PostMapping("seckillOrder")
    public ResponseEntity<Order> createSeckillOrder(@RequestBody OrderDTO orderDTO) {
        Order order = this.orderService.createSeckillOrder(orderDTO);
        return ResponseEntity.ok(order);
    }

    @GetMapping("url/{id}")
    public ResponseEntity<String> generateUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(this.orderService.generateUrl(orderId));
    }

    @GetMapping("seckill/{skuId}")
    public ResponseEntity<List<Cart>> querySeckillGoods(@PathVariable("skuId") Long skuId) {
        List<Cart> carts = this.orderService.querySeckillGoods(skuId);
        if (CollectionUtils.isEmpty(carts)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(carts);
    }
}
