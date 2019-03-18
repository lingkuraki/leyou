package com.leyou.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ly.worker")
public class IdWorkerProperties {
    // 当前机器ID
    private long workerId;
    // 序列号
    private long dataCenterId;
}
