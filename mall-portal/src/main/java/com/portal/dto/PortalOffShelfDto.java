package com.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "PortalOffShelfDto")
public class PortalOffShelfDto {
    @NotBlank(message = "商品id不能为空")
    private String goodsId;
    @NotBlank(message = "商家id不能为空")
    private String merchantId;
    @NotBlank(message = "商家所在城市不能为空")
    private String city;
    private BigDecimal fee;
    @NotBlank(message = "状态不能为空")
    private Short status;
}
