package com.common.security;

import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.common.config.InternalCallbackSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;

/**
 * 内部 /sys 回调（可选令牌）→ 其余路径登录 → 按角色 permission。
 */
public final class SaTokenRouteChecks {

    private static final String[] BUILTIN_ANON = {
    };

    private SaTokenRouteChecks() {
    }

    public static List<String> mergedWhitelist(List<String> configuredWhitelist) {
        List<String> noLogin = new ArrayList<>();
        for (String p : BUILTIN_ANON) {
            noLogin.add(p);
        }
        if (configuredWhitelist != null) {
            for (String p : configuredWhitelist) {
                if (p != null && !p.isBlank()) {
                    noLogin.add(p.trim());
                }
            }
        }
        return noLogin;
    }

    public static void runServlet(List<String> configuredWhitelist, InternalCallbackSecurityProperties internal) {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            // 无 Servlet 请求属性时 SaHolder 未初始化，禁止调用 SaRouter
            return;
        }
        HttpServletRequest request = sra.getRequest();
        String path = InternalCallbackAccessVerifier.servletRelativePath(request);
        if (handleInternalCallbackPath(path, request, null, internal)) {
            return;
        }
        applyUserRouteRules(configuredWhitelist);
    }

    public static void runReactive(ServerWebExchange exchange, List<String> configuredWhitelist, InternalCallbackSecurityProperties internal) {
        String path = normalizeUriPath(exchange.getRequest().getURI().getPath());
        if (handleInternalCallbackPath(path, null, exchange, internal)) {
            return;
        }
        applyUserRouteRules(configuredWhitelist);
    }

    private static boolean handleInternalCallbackPath(
            String path,
            HttpServletRequest servletRequest,
            ServerWebExchange exchange,
            InternalCallbackSecurityProperties internal
    ) {
        if (!InternalCallbackAccessVerifier.isInternalCallbackPath(path)) {
            return false;
        }
        if (!internal.isEnabled()) {
            return true;
        }
        if (servletRequest != null) {
            InternalCallbackAccessVerifier.verifyServlet(servletRequest, internal);
        } else if (exchange != null) {
            InternalCallbackAccessVerifier.verifyReactive(exchange, internal);
        }
        return true;
    }

    private static void applyUserRouteRules(List<String> configuredWhitelist) {
        List<String> noLogin = mergedWhitelist(configuredWhitelist);
        String[] anon = noLogin.toArray(new String[0]);
    }

    private static String normalizeUriPath(String uri) {
        if (uri == null) {
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
