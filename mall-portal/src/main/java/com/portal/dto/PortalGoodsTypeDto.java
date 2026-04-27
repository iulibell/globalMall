package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商品类型参数")
public class PortalGoodsTypeDto {
    private Long typeId;
    @NotBlank(message = "类型名称不能为空")
    private String typeName;
}
