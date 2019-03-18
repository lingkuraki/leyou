package com.leyou.cart.listener;

import com.leyou.cart.service.CartService;
import com.leyou.common.pojo.Message;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    @Autowired
    private CartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.remove.order.queue", durable = "true"),
            exchange = @Exchange(
                    value = "leyou.order.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "order.remove"))
    public void listenRemove(Message message) throws Exception {
        if (CollectionUtils.isEmpty(message.getIds())) return;
        // 从缓存中移除这些商品
        this.cartService.removeSkus(message);
    }

}
