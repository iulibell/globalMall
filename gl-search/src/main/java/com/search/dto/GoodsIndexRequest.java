package com.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "GoodsIndexRequest")
public class GoodsIndexRequest {

    @NotBlank
    private String goodsId;
    @NotBlank
    private String merchantId;
    @NotBlank
    private String skuName;
    private String picture;
    @NotNull
    private Integer category;
    @NotBlank
    private String type;
    private String description;
    private BigDecimal price;
    /** 1 表示已上架可搜，其它值写入后不会在默认搜索中出现 */
    @NotNull
    private Integer status;
}
