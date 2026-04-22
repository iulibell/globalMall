package com.portal.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OmsCartSettlePreviewDto {
    private Integer checkedItemCount;
    private Integer checkedQuantityTotal;
    private BigDecimal totalAmount;
}
