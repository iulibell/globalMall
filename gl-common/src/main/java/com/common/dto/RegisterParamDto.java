package com.common.dto;

import com.common.validator.ParamValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterParamDto {
    @NotBlank(message = "validation_register_username_required")
    private String username;
    @NotBlank(message = "validation_register_password_required")
    private String password;
    private String nickname;
    @Schema(description = "用户身份: 1 超管、2 管理员、3 仓库管理员、4 司机、5 审核员")
    @ParamValidator(value = {"1","2","3","4","5"}, message = "validation_usertype_invalid")
    private String userType;
    @NotBlank(message = "validation_phone_required")
    @Pattern(regexp = "^1\\d{10}$", message = "validation_phone_format")
    @Size(max = 11, message = "validation_phone_too_long")
    private String phone;

    /**
     * 门户自助注册必填；管理端审核接口可不带此字段。
     */
    @Schema(description = "注册前通过发送验证码接口获取的6位数字验证码")
    private String verifyCode;
    private Short status;
}
