package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderFromCartRequest {
    @NotNull(message = "购物车记录id不能为空")
    private Long cartId;

    @NotBlank(message = "用户手机号不能为空")
    private String userPhone;

    @NotBlank(message = "收货城市不能为空")
    private String city;
}
