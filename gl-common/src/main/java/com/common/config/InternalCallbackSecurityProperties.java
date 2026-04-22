package com.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内部 /sys 回调：可选开关 + 请求头令牌（默认 {@code X-Internal-Token}）。
 * <p>
 * {@code enabled=false} 时不校验（与本地开发一致）。{@code enabled=true} 时必须配置 {@code token-value}，且请求头与配置一致才放行。
 */
@ConfigurationProperties(prefix = "security.internal")
public class InternalCallbackSecurityProperties {

    private boolean enabled = false;

    private String tokenHeaderName = "X-Internal-Token";

    /** 与调用方（如 gl-message）使用同一配置值，建议环境变量 */
    private String tokenValue = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTokenHeaderName() {
        return tokenHeaderName;
    }

    public void setTokenHeaderName(String tokenHeaderName) {
        this.tokenHeaderName = tokenHeaderName;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
