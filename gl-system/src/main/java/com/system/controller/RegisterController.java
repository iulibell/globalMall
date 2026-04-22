package com.system.controller;

import com.common.api.CommonResult;
import com.common.dto.RegisterParamDto;
import com.common.dto.SendRegisterCaptchaDto;
import com.system.service.RegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
@Tag(name = "RegisterController", description = "门户用户注册：验证码、重复申请校验及向管理端写入待审核注册申请。")
public class RegisterController {
    @Resource
    private RegisterService registerService;

    @PostMapping("/register")
    @Operation(
            summary = "提交注册申请",
            description = "校验短信验证码与手机号唯一性，将申请写入 Redis 防重复并回调管理端生成待审核记录。")
    public CommonResult<?> register(@Valid @RequestBody RegisterParamDto registerParamDto) {
        return registerService.register(registerParamDto);
    }

    @PostMapping("/register/sendCaptcha")
    @Operation(
            summary = "发送注册验证码",
            description = "向指定手机号下发 6 位数字验证码并写入 Redis；同一手机号 60 秒内不可重复发送。")
    public CommonResult<?> sendRegisterCaptcha(@Valid @RequestBody SendRegisterCaptchaDto dto) {
        return registerService.sendRegisterCaptcha(dto.getPhone());
    }
}
