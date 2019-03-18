package com.leyou.test;

import com.leyou.user.LeyouUserApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouUserApplication.class)
public class TestSet {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void setValue() {
        this.redisTemplate.boundSetOps("kuraki").add("迪丽热巴", "古力娜扎", "马尔扎哈");
    }

    @Test // 查询该指定key的集合中的所有元素
    public void getValue() {
        Set set = this.redisTemplate.boundSetOps("kuraki").members();
        System.out.println(set.getClass());
    }
}