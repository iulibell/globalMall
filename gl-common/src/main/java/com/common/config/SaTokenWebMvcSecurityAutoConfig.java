package com.common.config;

import com.common.security.SaTokenRouteChecks;
import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 在引入 gl-common 的 Servlet Web 服务上注册 Sa-Token 拦截器（网关为 WebFlux，不走此配置）。
 */
@AutoConfiguration(after = SaTokenJwtAutoConfig.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SaInterceptor.class)
@EnableConfigurationProperties({SecurityWhitelistProperties.class, InternalCallbackSecurityProperties.class})
public class SaTokenWebMvcSecurityAutoConfig {

    @Bean
    public WebMvcConfigurer saTokenWebMvcConfigurer(SecurityWhitelistProperties whitelistProperties,
                                                    InternalCallbackSecurityProperties internalCallbackSecurityProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new SaInterceptor(handle ->
                                SaTokenRouteChecks.runServlet(whitelistProperties.getUrls(), internalCallbackSecurityProperties)))
                        .addPathPatterns("/**")
                        // forward 到 /error 时 Sa-Token 未绑定上下文，SaRouter 会抛 SaTokenContextException
                        .excludePathPatterns("/error", "/error/**");
            }
        };
    }
}