package com.portal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.validator.ParamValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("oms_order")
@Schema(description = "订单实体")
public class OmsOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 255)
    @Schema(description = "订单id(在此系统生成，联调物流系统)")
    private String orderId;
    @Size(max = 255)
    @Schema(description = "商家id")
    private String merchantId;
    @Size(max = 255)
    @Schema(description = "用户id")
    private String userId;
    @Size(max = 255)
    @Schema(description = "商品id(对应wms库存的stockId)")
    private String goodsId;
    @Schema(description = "库存所属的仓库id")
    private Long warehouseId;
    @Schema(description = "库位id")
    private Long locationId;
    @Size(max = 50)
    @Schema(description = "商品名称")
    private String skuName;
    @Size(max = 50)
    @Schema(description = "商品库存编号")
    private String skuCode;
    @Size(max = 10)
    @Schema(description = "支付金额")
    private BigDecimal price;
    @Size(max = 11)
    @Schema(description = "用户手机号")
    private String userPhone;
    @Size(max = 11)
    @Schema(description = "商家手机号")
    private String merchantPhone;
    @Size(max = 30)
    @Schema(description = "仓库所属城市")
    private String warehouseCity;
    @Size(max = 30)
    @Schema(description = "用户的城市")
    private String city;
    @Schema(description = "商品种类:0->普通,1->特殊")
    private Short category;
    @Size(max = 20)
    @Schema(description = "商品类型")
    private String type;
    @Schema(description = "订单状态: 0->待审核,1->未通过审核,2->已审核,3->待支付,4->超时未支付,5->已支付")
    @ParamValidator(value = {"0","1","2","3","4","5"}, message = "oms_order_status_invalid")
    private Short status;
    @Size(max = 500)
    @Schema(description = "备注(特殊商品未通过审核的原因)")
    private String remark;
    private Date createTime;
    private Date updateTime;
}
