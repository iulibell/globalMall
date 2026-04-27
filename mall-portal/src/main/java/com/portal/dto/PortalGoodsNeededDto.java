package com.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "PortalGoodsNeededDto")
public class PortalGoodsNeededDto {
    @NotBlank(message = "商品id不能为空")
    private String goodsId;
    @NotBlank(message = "仓库id不能为空")
    private Long warehouseId;
    @NotBlank(message = "库位id不能为空")
    private Long locationId;
    @NotBlank(message = "商品编号不能为空")
    private String skuCode;
    private Short category;
}
