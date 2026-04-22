package com.portal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("oms_cart")
public class OmsCart {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 255)
    @Schema(description = "用户id")
    private String userId;
    @Size(max = 255)
    @Schema(description = "商家id")
    private String merchantId;
    @Size(max = 255)
    @Schema(description = "商品id(对应wms库存的stockId)")
    private String goodsId;
    @Size(max = 50)
    @Schema(description = "商品库存编号")
    private String skuCode;
    @Size(max = 50)
    @Schema(description = "商品名称")
    private String skuName;
    @Size(max = 255)
    @Schema(description = "商品图片")
    private String picture;
    @Size(max = 10)
    @Schema(description = "商品价格")
    private BigDecimal price;
    @Schema(description = "购买数量")
    private Integer quantity;
    @Schema(description = "是否勾选:0->未勾选,1->已勾选")
    private Short checked;
    @Schema(description = "删除标记:0->正常,1->已删除")
    private Short deleted;
    private Date createTime;
    private Date updateTime;
}
