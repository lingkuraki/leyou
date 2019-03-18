package com.leyou.test;

import com.leyou.sms.LeyouSmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouSmsApplication.class)
public class SmsTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void test() throws InterruptedException {
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", "18705160837");
        msg.put("code", "123");
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);
        Thread.sleep(1000);
    }
}
