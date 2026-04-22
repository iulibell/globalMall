package com.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 与网关原 {@code secure.ignore.urls} 一致：无需登录的 URL（Ant 风格）。
 */
@ConfigurationProperties(prefix = "secure.ignore")
public class SecurityWhitelistProperties {

    private List<String> urls = new ArrayList<>();

    public List<String> getUrls() {
        return urls == null ? Collections.emptyList() : urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
