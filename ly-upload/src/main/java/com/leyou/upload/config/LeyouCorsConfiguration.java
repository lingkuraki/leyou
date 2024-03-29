package com.leyou.upload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class LeyouCorsConfiguration {

    @Bean
    public CorsFilter corsFilter() {
        // 1.1 添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        // 1.2 允许的域，不要写*，否则cookie就无法使用了
        config.addAllowedOrigin("http://manage.leyou.com");
        config.addAllowedOrigin("http://www.leyou.com");
        // 1.3 允许的请求方式
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("POST");
        // 1.4 允许的头信息
        config.addAllowedHeader("*");

        // 2. 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);

        // 3. 返回新的CorsFilter
        return new CorsFilter(configSource);
    }

}
