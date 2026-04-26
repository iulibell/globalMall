package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillActivityReviewRequest {
    @NotBlank(message = "活动编码不能为空")
    private String activityCode;

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    private String reviewRemark;
}
