package com.portal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
@TableName("portal_brand")
public class PortalBrand {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 255)
    @Schema(description = "品牌id")
    private String brandId;
    @Size(max = 20)
    @Schema(description = "品牌名称")
    private String brandName;
    private Date createTime;
    private Date updateTime;
}
