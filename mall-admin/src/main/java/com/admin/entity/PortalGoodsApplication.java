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
    private String skuName;
    @Size(max = 10)
    private BigDecimal price;
    @Size(max = 255)
    @Schema(description = "商品图片")
    private String picture;
    @Size(max = 500)
    @Schema(description = "商品描述")
    private String description;
    @Schema(description = "状态:0->待审核(默认),1->已过审,2->未过审,3->已取消")
    private Short status;
    @Size(max = 20)
    @Schema(description = "商家所在城市")
    private String city;
    @Size(max = 20)
    @Schema(description = "仓库所属城市")
    private String warehouseCity;
    @Schema(description = "关联入库申请单是否已支付:0->未支付(默认),1->已支付,2->超时未支付")
    private Short isPay;
    @Size(max = 500)
    @Schema(description = "备注(退回商品上架申请原因)")
    private String remark;
    @Schema(description = "入库申请支付费用")
    private BigDecimal fee;
    private Date createTime;
    private Date updateTime;
}
