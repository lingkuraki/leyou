package com.leyou.test;

import com.leyou.user.LeyouUserApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouUserApplication.class)
public class TestList {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void setLeftValue() {
        this.redisTemplate.boundListOps("kuraki").leftPushAll("迪丽热巴", "古力娜扎", "马尔扎哈");
    }

    @Test
    public void getLeftValue() {
        List<String> list = this.redisTemplate.boundListOps("kuraki").range(0, -1);
        System.out.println("list = " + list);
        System.out.println("list.getClass() = " + list.getClass());
    }

    @Test // 根据索引查询某值
    public void searchByIndex() {
        String name = redisTemplate.boundListOps("kuraki").index(1);
        System.out.println("name = " + name);
    }

    @Test // 删除某个值
    public void removeValue() {
        Long remove = this.redisTemplate.boundListOps("kuraki").remove(0, "古力娜扎");
        System.out.println("remove = " + remove);
    }
}
