package com.portal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
@TableName("portal_goods_type")
public class PortalGoodsType {
    @TableId(type = IdType.AUTO)
    private Long typeId;
    @Size(max = 20)
    @Schema(description = "类型名称")
    private String typeName;
    private Date createTime;
    private Date updateTime;
}
