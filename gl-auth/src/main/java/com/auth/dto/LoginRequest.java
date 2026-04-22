package com.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 与 gl-system {@code LoginParamDto} JSON 字段一致，供 Feign 序列化。
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "登录角色不能为空")
    private String requiredRoleKey;
}

