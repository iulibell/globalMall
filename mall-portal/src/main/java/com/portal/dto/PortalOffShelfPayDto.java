package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PortalOffShelfPayDto {
    @NotNull(message = "offShelfId不能为空")
    private Long offShelfId;
    @NotBlank(message = "userPhone不能为空")
    private String userPhone;
    @NotBlank(message = "merchantPhone不能为空")
    private String merchantPhone;
    @NotBlank(message = "city不能为空")
    private String city;
    @NotNull(message = "fee不能为空")
    private BigDecimal fee;
}
