package com.system.controller;

import com.common.api.CommonResult;
import com.common.constant.AuthConstant;
import com.system.dto.LoginParamDto;
import com.system.service.LoginAndLogoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/system")
@Tag(name = "LoginController", description = "系统用户登录与登出；校验账号密码及门户入口身份（requiredRoleKey）。")
public class LoginController {

    @Resource
    private LoginAndLogoutService loginAndLogoutService;

    @PostMapping("/login")
    @Operation(
            summary = "用户登录",
            description = "校验用户名密码与 requiredRoleKey（super/manager/keeper/driver/reviewer），签发 Sa-Token JWT 并返回用户信息。")
    public CommonResult<Map<String, Object>> login(@Valid @RequestBody LoginParamDto loginParamDto) {
        var tokenInfo = loginAndLogoutService.login(loginParamDto, loginParamDto.getRequiredRoleKey().trim());
        Map<String, Object> body = loginAndLogoutService.buildLoginResponse(tokenInfo, AuthConstant.TOKEN_HEAD);
        return CommonResult.success(body);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "注销当前会话中的 Sa-Token，需携带有效 token。")
    public CommonResult<?> logout() {
        loginAndLogoutService.logout();
        return CommonResult.successMsg("logout_success");
    }
}
