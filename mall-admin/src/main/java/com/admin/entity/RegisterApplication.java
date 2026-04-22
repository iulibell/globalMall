package com.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@TableName("register_application")
@Schema(description = "注册申请实体")
public class RegisterApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 20)
    @Schema(description = "用户名")
    private String username;
    @Size(max = 100)
    @Schema(description = "密码(加密)")
    private String password;
    @Size(max = 20)
    @Schema(description = "用户昵称，可为空(空时展示username)")
    private String nickname;
    @Schema(description = "用户类型:1->超管,2->管理员,3->仓库管理员,4->司机,5->审核员")
    @Min(1)
    @Max(5)
    private Short userType;
    @Size(max = 11)
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "状态:0->待审核,1->已审核,2->未过审")
    private Short status;
}
