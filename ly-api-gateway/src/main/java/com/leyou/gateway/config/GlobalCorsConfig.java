package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1.添加CORS的配置信息
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的域，不要写*，否则无法使用cookie
        configuration.addAllowedOrigin("http://manage.leyou.com");
        configuration.addAllowedOrigin("http://www.leyou.com");
        // 是否允许发送cookie信息
        configuration.setAllowCredentials(true);
        // 允许的请求方式
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("HEAD");
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("PATCH");
        // 允许的头信息
        configuration.addAllowedHeader("*");
        // 配置有效时长
        configuration.setMaxAge(3600 * 24L);
        // 2.添加映射的路径，拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", configuration);
        // 3.返回新的CorsFilter
        return new CorsFilter(configSource);
    }
}
