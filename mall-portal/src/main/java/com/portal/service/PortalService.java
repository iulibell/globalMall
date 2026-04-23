package com.portal.service;

import com.common.api.CommonResult;
import com.portal.dto.PortalGoodsDto;
import com.portal.dto.PortalGoodsNeededDto;
import com.portal.entity.PortalGoods;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface PortalService {
    /**
     * wms在确认入库后将上架商品并且补充商品所需的信息
     * @param portalGoodsNeededDto 商品所需补充的信息
     */
    void goodsOnShelf(PortalGoodsNeededDto portalGoodsNeededDto);

    /**
     * 根据商品种类(0->普通商品,1->特殊商品)
     * @param pageNum 页数
     * @param pageSize 页大小
     * @param category 种类
     * @return 商品列表
     */
    List<PortalGoodsDto> getPortalGoodsByCategory(int pageNum, int pageSize,Short category);

    /**
     * 根据商品id获取单个商品
     * @param goodsId 商品id
     * @return 单个商品
     */
    PortalGoodsDto getPortalGoodsById(String goodsId);

    /**
     * wms确认出库后，回写下架申请状态为已完成
     *
     * @param offShelfId 下架申请id
     * @return 本次是否更新成功
     */
    boolean markOffShelfCompleted(Long offShelfId);
}
