package com.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PortalGoodsApplicationDto {
    private Long id;
    @NotBlank(message = "上架申请id不能为空")
    private String applyId;
    private String merchantId;
    @NotBlank(message = "商品名称不能为空")
    private String skuName;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    @NotBlank(message = "商家手机号不能为空")
    private String merchantPhone;
    @NotBlank(message = "商品图片不能为空")
    private String picture;
    @NotBlank(message = "商家所在城市不能为空")
    private String city;
    @NotBlank(message = "仓库所属城市不能为空")
    private String warehouseCity;
    @NotNull(message = "申请数量不能为空")
    private Integer applyQuantity;
    private Short isPay;
    private String transportOrderId;
    /** 商城审核状态:0待审,1通过,2退回,3取消 */
    private Short mallStatus;
    /** 物流审核状态:0待审,1通过,2退回 */
    private Short logisticStatus;
    private String description;
    private String remark;
    private BigDecimal fee;
}
