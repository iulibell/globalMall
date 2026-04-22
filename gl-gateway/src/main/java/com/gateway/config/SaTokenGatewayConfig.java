package com.gateway.config;

import com.common.api.CommonResult;
import com.common.config.InternalCallbackSecurityProperties;
import com.common.config.SecurityWhitelistProperties;
import com.common.security.SaTokenRouteChecks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;

@Configuration
@EnableConfigurationProperties({SecurityWhitelistProperties.class, InternalCallbackSecurityProperties.class})
public class SaTokenGatewayConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SaReactorFilter saReactorFilter(SecurityWhitelistProperties whitelistProperties,
                                           InternalCallbackSecurityProperties internalCallbackSecurityProperties) {
        final JsonMapper errorJsonMapper = JsonMapper.builder()
                .configure(JsonWriteFeature.ESCAPE_NON_ASCII, true)
                .build();
        return new SaReactorFilter()
                .addInclude("/**")
                .setAuth(obj -> {
                    ServerWebExchange ex = SaReactorSyncHolder.getContext();
                    if (ex != null && HttpMethod.OPTIONS.equals(ex.getRequest().getMethod())) {
                        return;
                    }
                    if (ex != null) {
                        SaTokenRouteChecks.runReactive(ex, whitelistProperties.getUrls(), internalCallbackSecurityProperties);
                    }
                })
                .setError(e -> {
                    ServerWebExchange exchange = SaReactorSyncHolder.getContext();
                    HttpHeaders headers = exchange.getResponse().getHeaders();
                    headers.set("Content-Type", "application/json; charset=utf-8");
                    headers.set("Access-Control-Allow-Origin", "*");
                    headers.set(
                            "Access-Control-Allow-Headers",
                            "Authorization, Content-Type, Accept, Origin, X-Requested-With, satoken, X-Internal-Token");
                    headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                    headers.set("Cache-Control", "no-cache");

                    CommonResult<?> result;
                    if (e instanceof NotLoginException) {
                        result = CommonResult.unauthorized(null);
                    } else if (e instanceof NotPermissionException) {
                        result = CommonResult.forbidden();
                    } else {
                        result = CommonResult.failed("gateway_internal_error");
                    }
                    try {
                        return errorJsonMapper.writeValueAsString(result);
                    } catch (JsonProcessingException ex) {
                        return "{\"code\":500,\"message\":\"gateway_response_serialize_failed\",\"data\":null}";
                    }
                })
                .setExcludeList(SaTokenRouteChecks.mergedWhitelist(whitelistProperties.getUrls()));
    }
}
