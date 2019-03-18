package com.leyou.task;

import com.leyou.seckill.LySeckillApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySeckillApplication.class)
public class TestTask {

    @Test
    @Scheduled(cron = "0 * * * * ?")
    public void testTask() {
        System.out.println("执行的调度" + new Date());
    }
}
