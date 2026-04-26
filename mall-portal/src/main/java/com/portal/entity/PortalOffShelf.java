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
@TableName("portal_goods_off_shelf")
public class PortalOffShelf {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Size(max = 255)
    @Schema(description = "商品id")
    private String goodsId;
    @Size(max = 255)
    @Schema(description = "商家id")
    private String merchantId;
    @Size(max = 20)
    @Schema(description = "商家所属城市")
    private String city;
    @Size(max = 10)
    @Schema(description = "所支付的下架处理费用与运费")
    private BigDecimal fee;
    @Size(max = 255)
    @Schema(description = "关联物流运输单号")
    private String transportOrderId;
    @Schema(description = "状态:0->待审核,1->待支付,2->已支付,3->超时未支付,4->下架完成")
    private Short status;
    private Date createTime;
    private Date updateTime;
}
