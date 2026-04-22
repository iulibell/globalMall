package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OmsCartDto {
    private Long id;
    @NotBlank(message = "用户id不能为空")
    private String userId;
    @NotBlank(message = "商家id不能为空")
    private String merchantId;
    @NotBlank(message = "商品id不能为空")
    private String goodsId;
    private String skuCode;
    @NotBlank(message = "商品名称不能为空")
    private String skuName;
    @NotBlank(message = "商品图片不能为空")
    private String picture;
    @NotNull(message = "商品价格不能为空")
    private BigDecimal price;
    @NotNull(message = "购买数量不能为空")
    private Integer quantity;
    private Short checked;
}
