package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PortalGoodsDto {
    @NotBlank(message = "商家id不能为空")
    private String merchantId;
    @NotBlank(message = "商品id不能为空")
    private String goodsId;
    @NotBlank(message = "商品名称不能为空")
    private String skuName;
    @NotBlank(message = "商品图片不能为空")
    private String picture;
    @NotBlank(message = "种类不能为空")
    private Short category;
    @NotBlank(message = "价格不能为空")
    private BigDecimal price;
    @NotBlank(message = "商品类型不能为空")
    private String type;
    private String description;
}
