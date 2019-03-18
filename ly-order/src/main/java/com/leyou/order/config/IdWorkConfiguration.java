package com.leyou.order.config;

import com.leyou.common.utils.IdWorker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IdWorkerProperties.class)
public class IdWorkConfiguration {

    @Bean
    public IdWorker idWorker(IdWorkerProperties properties) {
        return new IdWorker(properties.getWorkerId(), properties.getDataCenterId());
    }
}
