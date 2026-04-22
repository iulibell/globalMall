package com.portal.service;

import com.portal.dto.PortalGoodsTypeDto;

import java.util.List;

public interface PortalGoodsTypeService {
    /**
     * 获取类型列表(manager操作)
     * @param pageNum 页数
     * @param pageSize 页大小
     * @return 类型列表
     */
    List<PortalGoodsTypeDto> getType(int pageNum,int pageSize);

    /**
     * 新增商品类型(manager操作)
     * @param portalGoodsTypeDto 类型dto
     */
    void addType(PortalGoodsTypeDto portalGoodsTypeDto);

    /**
     * 更新商品类型(manager操作)
     * @param portalGoodsTypeDto 类型dto
     */
    void updateType(PortalGoodsTypeDto portalGoodsTypeDto);

    /**
     * 删除商品类型(manager操作)
     * @param typeId 类型id
     */
    void deleteType(Long typeId);
}
