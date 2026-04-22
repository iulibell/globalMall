package com.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.validator.ParamValidator;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 255)
    @Schema(description = "用户唯一Id")
    private String userId;
    @Size(max = 20)
    @Schema(description = "用户名称")
    private String username;
    @Schema(description = "用户昵称")
    private String nickname;
    @Size(max = 255)
    @Schema(description = "密码(加密)")
    private String password;
    @Size(max = 11)
    @Schema(description = "用户手机号")
    private String phone;
    @Schema(description = "用户身份:1->super,2->manager,3->merchant,4->user,5->reviewer")
    @ParamValidator(value = {"1","2","3","4","5"},message = "非本平台用户")
    private String userType;
    @Size(max = 20)
    @Schema(description = "所在城市")
    private String city;
    @Schema(description = "状态:0->禁用,1->启用")
    private Short status;
    private Date createTime;
    private Date updateTime;
}
