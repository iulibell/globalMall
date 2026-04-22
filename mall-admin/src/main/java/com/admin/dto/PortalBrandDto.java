package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PortalBrandDto {
    private Long id;
    @NotBlank(message = "品牌id不能为空")
    private String brandId;
    @NotBlank(message = "品牌名称不能为空")
    private String brandName;
}
