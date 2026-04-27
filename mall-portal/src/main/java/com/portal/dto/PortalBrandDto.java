package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商品品牌参数")
public class PortalBrandDto {
    private Long id;
    @NotBlank(message = "品牌id不能为空")
    private String brandId;
    @NotBlank(message = "品牌名称不能为空")
    private String brandName;
}
