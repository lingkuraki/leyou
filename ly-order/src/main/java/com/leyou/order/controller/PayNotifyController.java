package com.leyou.order.controller;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
public class PayNotifyController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/wxpay/notify")
    public ResponseEntity<String> payNotify(@RequestBody Map<String, String> msg) {
        // 处理回调结果
        this.orderService.handleNotify(msg);
        // 没有异常，返回成功
        String result = "<xml>\n" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<return_msg><![CDATA[OK]]></return_msg>\n" +
                "</xml>";
        return ResponseEntity.ok(result);
    }

    @GetMapping("status/{id}")
    public ResponseEntity<Integer> queryPayState(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(this.orderService.queryStatus(orderId));
    }
}
