package com.admin.service;

import com.admin.dto.PortalGoodsDto;
import com.admin.dto.PortalBrandDto;
import com.admin.dto.PortalGoodsTypeDto;

import java.util.List;

public interface ManagerService {
    /**
     * 获取前台商品列表(manager操作)
     * @param pageNum 页数
     * @param pageSize 页大小
     * @return 商品列表
     */
    List<PortalGoodsDto> getPortalGoods(int pageNum, int pageSize,short category);

    /**
     * 根据商品id获取前台商品(manager操作)
     * @param goodsId 商品id
     * @return 单个商品
     */
    PortalGoodsDto getPortalGoodsById(String goodsId);

    /**
     * 获取商品类型列表(manager操作)
     */
    List<PortalGoodsTypeDto> getGoodsType(int pageNum, int pageSize);

    /**
     * 新增商品类型(manager操作)
     */
    void addGoodsType(PortalGoodsTypeDto portalGoodsTypeDto);

    /**
     * 更新商品类型(manager操作)
     */
    void updateGoodsType(PortalGoodsTypeDto portalGoodsTypeDto);

    /**
     * 删除商品类型(manager操作)
     */
    void deleteGoodsType(Long typeId);

    /**
     * 获取品牌列表(manager操作)
     */
    List<PortalBrandDto> getBrand(int pageNum, int pageSize);

    /**
     * 新增品牌(manager操作)
     */
    void addBrand(PortalBrandDto portalBrandDto);

    /**
     * 更新品牌(manager操作)
     */
    void updateBrand(PortalBrandDto portalBrandDto);

    /**
     * 删除品牌(manager操作)
     */
    void deleteBrand(Long id);
}
