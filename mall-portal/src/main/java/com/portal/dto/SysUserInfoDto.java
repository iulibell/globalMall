package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysUserInfoDto {
    @NotBlank(message = "用户id不能为空")
    private String userId;
    private String nickname;
    private String password;
    private String phone;
    private String city;
}
