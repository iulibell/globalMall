package com.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysUserDto {
    @NotBlank(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "用户类型不能为空")
    private String userType;
    /** 展示名；登录响应里若为空则回退为 username */
    private String nickname;
}
