package com.leyou.order.listener;

import com.leyou.order.service.OrderService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HomeOrderListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.homeOrder.shipment.queue", durable = "true"),
            exchange = @Exchange(
                    value = "leyou.home.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "homeOrder.shipment"))
    public void listenShipment(Long orderId) {
        if (orderId == null) return;
        this.orderService.updateOrderToShipment(orderId);
    }
}
