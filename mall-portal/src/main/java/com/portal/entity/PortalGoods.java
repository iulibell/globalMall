package com.portal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("portal_goods")
public class PortalGoods {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 255)
    @Schema(description = "商家id")
    private String merchantId;
    @Size(max = 255)
    @Schema(description = "商品id(对应wms库存的stockId)")
    private String goodsId;
    @Schema(description = "库存所属的仓库id")
    private Long warehouseId; //入库后增加
    @Schema(description = "库位id")
    private Long locationId; //入库后增加
    @Size(max = 50)
    @Schema(description = "商品名称")
    private String skuName;
    @Size(max = 50)
    @Schema(description = "商品库存编号")
    private String skuCode; //入库后增加
    @Size(max = 255)
    @Schema(description = "商品图片")
    private String picture;
    @Schema(description = "商品种类:0->普通,1->特殊")
    private Short category;
    @Size(max = 10)
    @Schema(description = "商品价格")
    private BigDecimal price;
    @Size(max = 20)
    @Schema(description = "商品类型")
    private String type;
    @Size(max = 500)
    @Schema(description = "商品描述")
    private String description;
    @Schema(description = "访问次数(初始值为0)")
    private Long visitVolume;
    @Schema(description = "商品状态:0->未上架,1->已上架(可展示),2->已下架")
    private Short status;
    private Date createTime;
    private Date updateTime;
}
