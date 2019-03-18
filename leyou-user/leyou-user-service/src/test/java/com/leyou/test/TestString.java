package com.leyou.test;

import com.leyou.user.LeyouUserApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouUserApplication.class)
public class TestString {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Before
    public void setValue() {
        this.redisTemplate.boundValueOps("name").set("kuraki");
    }

    @Test
    public void getValue() {
        String name = this.redisTemplate.boundValueOps("name").get();
        System.out.println("name = " + name);
    }

    @Test
    public void delValue(){
        redisTemplate.delete("name");
        System.out.println(this.redisTemplate.boundValueOps("name").get());
    }
}
