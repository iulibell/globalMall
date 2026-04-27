package com.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "OmsCartSettlePreviewDto")
public class OmsCartSettlePreviewDto {
    private Integer checkedItemCount;
    private Integer checkedQuantityTotal;
    private BigDecimal totalAmount;
}
