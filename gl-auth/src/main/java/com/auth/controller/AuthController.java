package com.auth.controller;

import com.common.api.CommonResult;
import com.auth.client.SystemServiceClient;
import com.auth.dto.LoginRequest;
import com.common.dto.RegisterParamDto;
import com.common.dto.SendRegisterCaptchaDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证门面：统一 /auth 前缀，转发 gl-system 完成注册、登录、登出。
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "统一 /auth 入口：注册、验证码、登录与登出，经 Feign 转发 gl-system。")
public class AuthController {

    @Resource
    private SystemServiceClient systemServiceClient;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "转发 gl-system /system/register")
    public CommonResult<?> register(@Valid @RequestBody RegisterParamDto registerParamDto) {
        return systemServiceClient.register(registerParamDto);
    }

    @PostMapping("/register/sendCaptcha")
    @Operation(summary = "发送注册验证码", description = "转发 gl-system /system/register/sendCaptcha")
    public CommonResult<?> sendRegisterCaptcha(@Valid @RequestBody SendRegisterCaptchaDto dto) {
        return systemServiceClient.sendRegisterCaptcha(dto);
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "转发 gl-system /system/login，requiredRoleKey: super|manager|keeper|driver|reviewer")
    public CommonResult<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return systemServiceClient.login(loginRequest);
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "需携带与登录一致的 token 请求头")
    public CommonResult<?> logout() {
        return systemServiceClient.logout();
    }
}
