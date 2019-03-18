package com.leyou.test;

import com.leyou.user.LeyouUserApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouUserApplication.class)
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testRedis() {
        // 存储数据
        this.redisTemplate.opsForValue().set("key1", "value1");
        // 获取数据
        String val = this.redisTemplate.opsForValue().get("key1");
        System.out.println("val = " + val);
    }

    @Test
    public void testRedis2() {
        // 存储数据，并指定剩余生命时间10秒钟
        this.redisTemplate.opsForValue().set("key2", "地表最狂", 10, TimeUnit.SECONDS);
    }

    @Test
    public void testHash() {
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps("user");
        // 操作hash数据
        hashOps.put("name", "kuraki");
        hashOps.put("age", "25");
        // 获取单个数据
        Object name = hashOps.get("name");
        System.out.println("name = " + name);
        // 获取所有数据
        Map<Object, Object> map = hashOps.entries();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

}
