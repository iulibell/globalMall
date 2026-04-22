package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PortalGoodsTypeDto {
    private Long typeId;
    @NotBlank(message = "类型名称不能为空")
    private String typeName;
}
