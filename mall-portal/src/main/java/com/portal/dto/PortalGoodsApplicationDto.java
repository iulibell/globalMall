package com.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商品上架申请参数")
public class PortalGoodsApplicationDto {
    @NotBlank(message = "商家id不能为空")
    private String userId;
    /** 与 {@link #userId} 一致时由服务端默认填充；传给 mall-admin 写入 goods_application.merchant_id */
    private String merchantId;
    @NotBlank(message = "上架申请id不能为空")
    private String applyId;
    @NotBlank(message = "商品名称不能为空")
    private String skuName;
    /** 对应 {@code portal_goods_type.type_id}，商家从 {@code /portal/getGoodsType} 选择 */
    @NotNull(message = "商品类型不能为空")
    private Long typeId;
    @NotBlank(message = "商家手机号不能为空")
    private String merchantPhone;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
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
    private Short mallStatus;
    private Short logisticStatus;
    private String description;
    private String remark;
    private BigDecimal fee;
}
