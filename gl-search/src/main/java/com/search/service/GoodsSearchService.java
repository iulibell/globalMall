package com.search.service;

import com.search.dto.GoodsIndexRequest;
import com.search.dto.GoodsSearchPageVo;

public interface GoodsSearchService {

    void indexGoods(GoodsIndexRequest request);

    void removeGoods(String goodsId);

    /**
     * 关键词检索（名称、描述、类型）；仅返回 {@code status == 1} 的文档。
     *
     * @param keyword 关键词，空则按可售状态全量分页（match_all + filter）
     * @param page    从 0 开始
     * @param size    每页条数
     */
    GoodsSearchPageVo search(String keyword, int page, int size);
}
