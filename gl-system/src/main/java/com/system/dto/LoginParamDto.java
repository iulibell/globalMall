package com.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "LoginParamDto")
public class LoginParamDto {
    @NotBlank(message = "validation_username_required")
    String username;
    @NotBlank(message = "validation_password_required")
    String password;
    /**
     * 登录入口角色，与账号 userType 一致：super / manager / keeper / driver / reviewer
     */
    @NotBlank(message = "validation_role_required")
    private String requiredRoleKey;
}