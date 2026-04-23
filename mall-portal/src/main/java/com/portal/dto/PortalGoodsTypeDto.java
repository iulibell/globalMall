package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortalGoodsTypeDto {
    @NotNull(message = "类型id不能为空")
    private Long typeId;
    @NotBlank(message = "类型名称不能为空")
    private String typeName;
}
