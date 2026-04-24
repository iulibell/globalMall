package com.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("goods_application")
public class PortalGoodsApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 255)
    @Schema(description = "商品上架申请id")
    private String applyId;
    @Size(max = 255)
    @Schema(description = "商家id")
    private String merchantId;
    @Size(max = 11)
    @Schema(description = "商家手机号")
    private String merchantPhone;
    @Size(max = 50)
    @Schema(description = "商品名称")
    private String skuName;
    @Size(max = 10)
    @Schema(description = "商品价格")
    private BigDecimal price;
    @Size(max = 255)
    @Schema(description = "商品图片")
    private String picture;
    @Size(max = 500)
    @Schema(description = "商品描述")
    private String description;
    @Schema(description = "商城审核状态:0->待审核(默认),1->已通过,2->已退回,3->已取消")
    private Short mallStatus;
    @Schema(description = "物流审核状态:0->待审核(默认),1->已通过,2->已退回")
    private Short logisticStatus;
    @Size(max = 20)
    @Schema(description = "商家所在城市")
    private String city;
    @Size(max = 20)
    @Schema(description = "仓库所属城市")
    private String warehouseCity;
    @Schema(description = "申请数量")
    private Integer applyQuantity;
    @Schema(description = "关联入库申请单是否已支付:0->未支付(默认),1->已支付,2->超时未支付")
    private Short isPay;
    @Size(max = 255)
    @Schema(description = "关联运输单号")
    private String transportOrderId;
    @Size(max = 500)
    @Schema(description = "备注(退回商品上架申请原因)")
    private String remark;
    @Schema(description = "入库申请支付费用")
    private BigDecimal fee;
    private Date createTime;
    private Date updateTime;
}
