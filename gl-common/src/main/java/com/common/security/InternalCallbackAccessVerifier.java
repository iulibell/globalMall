package com.common.security;

import cn.dev33.satoken.exception.NotPermissionException;
import com.common.config.InternalCallbackSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * 校验内部回调路径与配置令牌是否一致：
 * {@code /oms/sys/**}、{@code /wms/sys/**}、{@code /tms/sys/**}、{@code /portal/sys/**}、{@code /search/goods/**}。
 */
public final class InternalCallbackAccessVerifier {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<String> INTERNAL_PATTERNS = List.of(
            "/oms/sys/**",
            "/wms/sys/**",
            "/tms/sys/**",
            "/portal/sys/**",
            "/search/goods/**"
    );

    private InternalCallbackAccessVerifier() {
    }

    public static boolean isInternalCallbackPath(String rawPath) {
        if (rawPath == null) {
            return false;
        }
        String path = normalizePath(rawPath);
        for (String pattern : INTERNAL_PATTERNS) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    public static void verifyServlet(HttpServletRequest request, InternalCallbackSecurityProperties p) {
        if (!p.isEnabled()) {
            return;
        }
        String path = normalizePath(servletRelativePath(request));
        if (!isInternalCallbackPath(path)) {
            return;
        }
        verifyToken(path, name -> request.getHeader(name), p);
    }

    public static void verifyReactive(ServerWebExchange exchange, InternalCallbackSecurityProperties p) {
        if (!p.isEnabled()) {
            return;
        }
        String path = normalizePath(exchange.getRequest().getURI().getPath());
        if (!isInternalCallbackPath(path)) {
            return;
        }
        verifyToken(
                path,
                name -> exchange.getRequest().getHeaders().getFirst(name),
                p
        );
    }

    private static void verifyToken(String path, java.util.function.Function<String, String> header, InternalCallbackSecurityProperties p) {
        if (!StringUtils.hasText(p.getTokenValue())) {
            throw new NotPermissionException("已开启内部接口保护，请配置 security.internal.token-value: " + path);
        }
        String sent = header.apply(p.getTokenHeaderName());
        if (!p.getTokenValue().equals(sent)) {
            throw new NotPermissionException("内部令牌无效: " + path);
        }
    }

    public static String servletRelativePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri != null && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri == null ? "" : uri;
    }

    static String normalizePath(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "/";
        }
        int q = uri.indexOf('?');
        String path = q >= 0 ? uri.substring(0, q) : uri;
        if (!path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }
}
