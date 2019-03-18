package com.leyou.task;

import com.leyou.seckill.LySeckillApplication;
import com.leyou.seckill.mapper.SeckillGoodsMapper;
import com.leyou.seckill.pojo.SeckillGoods;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@SpringBootTest(classes = LySeckillApplication.class)
@RunWith(SpringRunner.class)
public class TestSeckill {

    @Test
    public void test01() throws ParseException {
        System.out.println(LocalDate.now());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = dateFormat.parse(LocalDate.now() + " 12:00:00");
        String s = dateFormat.format(date);
        System.out.println(s.split(" ")[1]);

    }

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Test
    public void test02() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SeckillGoods seckillGoods = this.seckillGoodsMapper.selectByPrimaryKey(1L);
        System.out.println(dateFormat.format(seckillGoods.getEndTime()));
    }
}
