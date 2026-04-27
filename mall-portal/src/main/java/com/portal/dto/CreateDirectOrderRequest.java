package com.portal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户直接购买创建订单请求")
public class CreateDirectOrderRequest {
    @NotBlank(message = "商品id不能为空")
    private String goodsId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    private Integer quantity;

    @NotBlank(message = "用户手机号不能为空")
    private String userPhone;

    @NotBlank(message = "收货城市不能为空")
    private String city;
}
