package com.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "请求发送注册验证码")
public class SendRegisterCaptchaDto {

    @NotBlank(message = "validation_phone_required")
    @Pattern(regexp = "^1\\d{10}$", message = "validation_phone_format")
    private String phone;
}
