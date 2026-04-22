package com.message.config;

import com.common.config.InternalCallbackSecurityProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 调用各业务 /sys 回调时附带内部令牌（与下游 {@code security.internal.token-value} 一致）。
 */
@Configuration
@EnableConfigurationProperties(InternalCallbackSecurityProperties.class)
public class InternalCallbackFeignConfig {

    @Bean
    public RequestInterceptor internalCallbackTokenInterceptor(InternalCallbackSecurityProperties props) {
        return (RequestTemplate template) -> {
            String token = props.getTokenValue();
            if (!StringUtils.hasText(token)) {
                return;
            }
            template.header(props.getTokenHeaderName(), token);
        };
    }
}

