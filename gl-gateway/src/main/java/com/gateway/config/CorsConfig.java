package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*");
        config.addAllowedOriginPattern("*");
        /*
         * allowCredentials(true) 时，规范不允许 Access-Control-Allow-Headers 为 *，
         * 浏览器预检会拒绝带 Authorization 的请求，须逐条声明。
         * 预检里 Access-Control-Request-Headers 常为 authorization（小写），一并允许。
         */
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("satoken");
        config.addAllowedHeader("X-Internal-Token");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

}
