package com.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsSearchItemVo {
    private String goodsId;
    private String merchantId;
    private String skuName;
    private String picture;
    private Integer category;
    private String type;
    private String description;
    private Double price;
    private Integer status;
}
