package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WmsOutboundCreateDto {
    @NotBlank(message = "outboundId不能为空")
    private String outboundId;
    @NotNull(message = "warehouseId不能为空")
    private Long warehouseId;
    @NotBlank(message = "stockId不能为空")
    private String stockId;
    @NotBlank(message = "orderId不能为空")
    private String orderId;
    @NotNull(message = "locationId不能为空")
    private Long locationId;
    @NotBlank(message = "skuName不能为空")
    private String skuName;
    @NotBlank(message = "skuCode不能为空")
    private String skuCode;
    @NotBlank(message = "userPhone不能为空")
    private String userPhone;
    @NotBlank(message = "merchantPhone不能为空")
    private String merchantPhone;
    @NotBlank(message = "city不能为空")
    private String city;
    @NotNull(message = "status不能为空")
    private Short status;

    /**
     * 商城下架已支付总费用（含下架处理费与运费）；仅 orderId 以 OFF 开头时由 WMS 用于创建 TMS 运输单。
     */
    private BigDecimal paidFee;
}
