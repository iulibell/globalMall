package com.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "SysUserInfoDto")
public class SysUserInfoDto {
    @NotBlank(message = "用户id不能为空")
    private String userId;
    private String nickname;
    private String password;
    private String phone;
    private String city;
}
