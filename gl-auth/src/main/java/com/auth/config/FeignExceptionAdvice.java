package com.auth.config;

import com.common.api.CommonResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 下游 gl-system 若仍返回 HTTP 5xx，Feign 会抛异常；将响应体中的 {@link CommonResult} 形态转回统一 JSON，避免 auth 侧再 500。
 */
@ControllerAdvice
public class FeignExceptionAdvice {

    /** Boot 4 默认可能只注册 JsonMapper，不注入 {@link ObjectMapper}，此处仅解析 JSON 用本地实例即可 */
    private static final ObjectMapper JSON = new ObjectMapper();

    @ResponseBody
    @ExceptionHandler(FeignException.class)
    public CommonResult<?> handleFeign(FeignException e) {
        String body = e.contentUTF8();
        if (body != null && !body.isBlank()) {
            try {
                JsonNode n = JSON.readTree(body);
                if (n.has("code") && n.has("message") && n.get("message").isTextual()) {
                    int code = n.get("code").asInt();
                    String msg = n.get("message").asText();
                    return CommonResult.of(code, msg, null);
                }
            } catch (Exception ignored) {
                /* 非 CommonResult JSON（如 Spring 默认 error）走兜底 */
            }
        }
        return CommonResult.failed("internal_error");
    }
}
