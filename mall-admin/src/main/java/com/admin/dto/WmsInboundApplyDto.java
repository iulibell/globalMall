package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WmsInboundApplyDto {
    private String goodsId;
    @NotBlank(message = "申请入库id不能为空")
    private String applyId;
    @NotBlank(message = "商家id不能为空")
    private String merchantId;
    @NotBlank(message = "商家手机号不能为空")
    private String merchantPhone;
    private String warehouseId;
    @NotBlank(message = "商品名称不能为空")
    private String skuName;
    @NotBlank(message = "商家所在城市不能为空")
    private String city;
    @NotBlank(message = "申请仓库所属城市不能为空")
    private String warehouseCity;
    @NotBlank(message = "申请数量不能为空")
    private Integer applyQuantity;
    private Short status;
    private String merchantRemark;
    private String rejectRemark;
    private BigDecimal fee;
}
