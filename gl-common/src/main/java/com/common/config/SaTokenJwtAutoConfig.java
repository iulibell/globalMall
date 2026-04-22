package com.common.config;

import cn.dev33.satoken.jwt.StpLogicJwtForMixin;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import com.common.satoken.CommonSaTokenPermissionInterface;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 与 gl-system 登录一致：JWT + Redis Mixin，供各业务服务与网关共用同一套验票与权限读取。
 */
@AutoConfiguration
public class SaTokenJwtAutoConfig {

    @Bean
    @Primary
    @ConditionalOnClass(StpLogicJwtForMixin.class)
    public StpLogic stpLogicJwtForMixin() {
        return new StpLogicJwtForMixin();
    }

    @Bean
    public StpInterface stpInterface() {
        return new CommonSaTokenPermissionInterface();
    }
}