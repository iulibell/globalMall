package com.portal.service;

import com.portal.dto.PortalBrandDto;

import java.util.List;

public interface PortalBrandService {
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
