package com.portal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("seckill_activity_goods")
public class SeckillActivityGoods {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "活动编码")
    private String activityCode;

    @Schema(description = "super发起活动编码")
    private String launchActivityCode;

    @Schema(description = "活动名称快照")
    private String activityName;

    @Schema(description = "关联商品id")
    private String goodsId;

    @Schema(description = "报名商家id")
    private String merchantId;

    @Schema(description = "原价快照")
    private BigDecimal originPrice;

    @Schema(description = "秒杀价")
    private BigDecimal seckillPrice;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "可用库存")
    private Integer availableStock;

    @Schema(description = "单用户限购数量")
    private Integer perUserLimit;

    @Schema(description = "状态:1待审核,2未过审,3已过审")
    private Short status;

    @Schema(description = "审核员id")
    private String reviewerId;

    @Schema(description = "审核备注")
    private String reviewRemark;

    @Schema(description = "备注")
    private String remark;

    private Date createTime;
    private Date updateTime;
}
