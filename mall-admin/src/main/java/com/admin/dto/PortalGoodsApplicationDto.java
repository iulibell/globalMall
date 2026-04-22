package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PortalGoodsApplicationDto {
    @NotBlank(message = "上架申请id不能为空")
    private String applyId;
    @NotBlank(message = "商品名称不能为空")
    private String skuName;
    @NotBlank(message = "价格不能为空")
    private BigDecimal price;
    @NotBlank(message = "商家手机号不能为空")
    private String merchantPhone;
    @NotBlank(message = "商品图片不能为空")
    private String picture;
    @NotBlank(message = "商家所在城市不能为空")
    private String city;
    @NotBlank(message = "仓库所属城市不能为空")
    private String warehouseCity;
    private Short isPay;
    private String description;
    private String remark;
}
