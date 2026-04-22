package com.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4 默认装配 Jackson 3 的 {@code JsonMapper}，不会注册 {@link ObjectMapper}。
 * {@link com.system.service.impl.CacheServiceImpl} 等仍使用 Jackson 2 API，需显式提供 Bean。
 */
@Configuration
public class JacksonObjectMapperConfig {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}