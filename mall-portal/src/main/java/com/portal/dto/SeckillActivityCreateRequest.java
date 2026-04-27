package com.portal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "商家秒杀报名请求")
public class SeckillActivityCreateRequest {
    @NotBlank(message = "商品id不能为空")
    private String goodsId;

    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    @NotNull(message = "秒杀价格不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价格必须大于0")
    private BigDecimal seckillPrice;

    @NotNull(message = "活动库存不能为空")
    @Min(value = 1, message = "活动库存必须大于0")
    private Integer totalStock;

    @NotNull(message = "限购数量不能为空")
    @Min(value = 1, message = "限购数量必须大于0")
    private Integer perUserLimit;

    @NotNull(message = "开始时间不能为空")
    private Date startTime;

    @NotNull(message = "结束时间不能为空")
    private Date endTime;

    private String remark;
}
