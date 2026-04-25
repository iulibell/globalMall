package com.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.admin.dto.PortalGoodsDto;
import com.admin.dto.PortalBrandDto;
import com.admin.dto.PortalGoodsTypeDto;
import com.admin.service.ManagerService;
import com.admin.service.client.PortalServiceClient;
import com.common.api.CommonResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ManagerServiceImpl implements ManagerService {
    @Resource
    private PortalServiceClient portalServiceClient;

    @Override
    public List<PortalGoodsDto> getPortalGoods(int pageNum, int pageSize, short category) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        return portalServiceClient.getPortalGoods(pageNum,pageSize,category);
    }

    @Override
    public PortalGoodsDto getPortalGoodsById(String goodsId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        return  portalServiceClient.getPortalGoodsById(goodsId);
    }

    @Override
    public List<PortalGoodsTypeDto> getGoodsType(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        CommonResult<List<PortalGoodsTypeDto>> result = portalServiceClient.getGoodsType(pageNum, pageSize);
        return result != null && result.getData() != null ? result.getData() : Collections.emptyList();
    }

    @Override
    public void addGoodsType(PortalGoodsTypeDto portalGoodsTypeDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        portalServiceClient.addGoodsType(portalGoodsTypeDto);
    }

    @Override
    public void updateGoodsType(PortalGoodsTypeDto portalGoodsTypeDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        portalServiceClient.updateGoodsType(portalGoodsTypeDto);
    }

    @Override
    public void deleteGoodsType(Long typeId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        portalServiceClient.deleteGoodsType(typeId);
    }

    @Override
    public List<PortalBrandDto> getBrand(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        CommonResult<List<PortalBrandDto>> result = portalServiceClient.getBrand(pageNum, pageSize);
        return result != null && result.getData() != null ? result.getData() : Collections.emptyList();
    }

    @Override
    public void addBrand(PortalBrandDto portalBrandDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        portalServiceClient.addBrand(portalBrandDto);
    }

    @Override
    public void updateBrand(PortalBrandDto portalBrandDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        portalServiceClient.updateBrand(portalBrandDto);
    }

    @Override
    public void deleteBrand(Long id) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        portalServiceClient.deleteBrand(id);
    }
}
