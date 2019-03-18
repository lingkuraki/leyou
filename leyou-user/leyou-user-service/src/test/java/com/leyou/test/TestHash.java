package com.leyou.test;

import com.leyou.user.LeyouUserApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouUserApplication.class)
public class TestHash {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void setValue() {
        Map<String, String> map = new HashMap<>();
        map.put("1", "唐僧");
        map.put("2", "悟空");
        map.put("3", "悟能");
        map.put("4", "悟净");
        redisTemplate.boundHashOps("kuraki").putAll(map);
    }

    @Test
    public void getKeys(){
        Set<Object> keys = this.redisTemplate.boundHashOps("kuraki").keys();
        System.out.println("keys = " + keys);
    }

    @Test // 获取所有的value
    public void getValues() {
        List<Object> values = this.redisTemplate.boundHashOps("kuraki").values();
        System.out.println(values);
    }

    @Test // 根据key获取value
    public void getValueByKey() {
        String str = (String) this.redisTemplate.boundHashOps("kuraki").get("2");
        System.out.println(str);
    }

    @Test // 移除指定key的值
    public void removeValue() {
        this.redisTemplate.boundHashOps("kuraki").delete("3");
    }

    @Test
    public void deleteKey(){
        System.out.println(this.redisTemplate.delete("kuraki"));
    }
}
