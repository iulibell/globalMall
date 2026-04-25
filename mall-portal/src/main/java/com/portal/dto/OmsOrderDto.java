package com.portal.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OmsOrderDto {
    @NotBlank(message = "oms_v_order_id")
    private String orderId;
    @NotBlank(message = "oms_v_merchant_id")
    private String merchantId;
    @NotBlank(message = "oms_v_user_id")
    private String userId;
    @NotBlank(message = "oms_v_goods_id")
    private String goodsId;
    @NotBlank(message = "oms_v_warehouse_id")
    private Long warehouseId;
    @NotBlank(message = "oms_v_location_id")
    private Long locationId;
    @NotBlank(message = "oms_v_sku_name")
    private String skuName;
    @NotBlank(message = "oms_v_sku_code")
    private String skuCode;
    @NotBlank(message = "oms_v_price")
    private BigDecimal price;
    @NotBlank(message = "oms_v_user_phone")
    private String userPhone;
    @NotBlank(message = "oms_v_merchant_phone")
    private String merchantPhone;
    @NotBlank(message = "oms_v_warehouse_city")
    private String warehouseCity;
    @NotBlank(message = "oms_v_city")
    private String city;
    @NotBlank(message = "oms_v_category")
    private Short category;
    @NotBlank(message = "oms_v_quantity")
    private Integer quantity;
    @NotBlank(message = "oms_v_type")
    private String type;
    private Short status;
    private String remark;
    private String transportOrderId;
    private Date createTime;
    private Date updateTime;
}
