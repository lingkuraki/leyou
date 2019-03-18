package com.leyou.home.controller;

import com.leyou.home.pojo.OrderDTO;
import com.leyou.home.pojo.OrderDetailPlus;
import com.leyou.home.service.OrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 查询所有订单
     *
     * @return 返回当前用户下的所有订单
     */
    @GetMapping("/order")
    public ResponseEntity<List<OrderDTO>> queryOrderDTO() {
        List<OrderDTO> orderDTOList = this.orderService.queryOrderDTO();
        if (CollectionUtils.isEmpty(orderDTOList)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(orderDTOList);
    }

    /**
     * 提醒发货
     *
     * @param orderId 订单ID
     */
    @PutMapping("order/shipment/{orderId}")
    public ResponseEntity<Void> updateOrderToShipment(@PathVariable("orderId") Long orderId) {
        this.orderService.updateOrderToShipment(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 确认收货
     *
     * @param orderId 订单ID
     */
    @PutMapping("order/receive/{orderId}")
    public ResponseEntity<Void> updateOrderToReceive(@PathVariable("orderId") Long orderId) {
        this.orderService.updateOrderToReceive(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 订单评价
     *
     * @param orderId 订单ID
     */
    @PutMapping("order/evaluate/{orderId}")
    public ResponseEntity<Void> updateOrderToEvaluate(@PathVariable("orderId") Long orderId) {
        this.orderService.updateOrderToEvaluate(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询单张订单
     */
    @GetMapping("oneOrder/{orderId}")
    public ResponseEntity<OrderDetailPlus> queryOneOrderDTOByOrderId(@PathVariable("orderId") Long orderId) {
        OrderDetailPlus orderDetailPlus = this.orderService.queryOneOrderDTOByOrderId(orderId);
        if (orderDetailPlus == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(orderDetailPlus);
    }
}
