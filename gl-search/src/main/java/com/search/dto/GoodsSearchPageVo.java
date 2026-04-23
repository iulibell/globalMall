package com.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsSearchPageVo {
    private long total;
    private int page;
    private int size;
    private List<GoodsSearchItemVo> items;
}
