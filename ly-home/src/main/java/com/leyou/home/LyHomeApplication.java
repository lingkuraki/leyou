package com.leyou.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.leyou.home.mapper")
public class LyHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyHomeApplication.class, args);
    }
}
