package com.common.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * Spring Boot 默认装配的 {@code redisTemplate} 使用 JDK 序列化，要求 value 为 {@link java.io.Serializable}。
     * 在默认 Bean 初始化完成后替换为 JSON 序列化，与业务中存放 DTO、实体（如 {@code SysUser}）一致。
     * 采用 BeanPostProcessor 可避免与 Boot 再注册一个 {@code redisTemplate} 产生 Bean 定义冲突。
     */
    @Bean
    public BeanPostProcessor redisTemplateJsonSerializerBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (!"redisTemplate".equals(beanName) || !(bean instanceof RedisTemplate<?, ?> template)) {
                    return bean;
                }
                StringRedisSerializer stringSerializer = new StringRedisSerializer();
                GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                        .enableUnsafeDefaultTyping()
                        .build();
                template.setKeySerializer(stringSerializer);
                template.setHashKeySerializer(stringSerializer);
                template.setValueSerializer(jsonSerializer);
                template.setHashValueSerializer(jsonSerializer);
                return template;
            }
        };
    }
}
