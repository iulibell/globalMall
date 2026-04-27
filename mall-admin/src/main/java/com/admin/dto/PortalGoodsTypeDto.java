package com.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "PortalGoodsTypeDto")
public class PortalGoodsTypeDto {
    private Long typeId;
    @NotBlank(message = "类型名称不能为空")
    private String typeName;
}
