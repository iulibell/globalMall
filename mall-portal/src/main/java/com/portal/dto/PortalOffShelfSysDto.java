package com.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 供物流 WMS 经 {@code /portal/sys/**} 拉取的下架申请快照（与 {@link com.portal.entity.PortalOffShelf} 字段对齐）。
 */
@Data
public class PortalOffShelfSysDto {
    private Long id;
    private String goodsId;
    private String merchantId;
    private String city;
    private BigDecimal fee;
    private Short status;
    private Date createTime;
    private Date updateTime;
    @Schema(description = "商城门户商品名称（便于仓管核对）")
    private String skuName;
}
