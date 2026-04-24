package com.portal.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.Assert;
import com.portal.dao.PortalGoodsTypeDao;
import com.portal.dto.PortalGoodsTypeDto;
import com.portal.entity.PortalGoodsType;
import com.portal.service.PortalGoodsTypeService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PortalGoodsTypeServiceImpl implements PortalGoodsTypeService {
    @Resource
    private PortalGoodsTypeDao portalGoodsTypeDao;

    /**
     * 分页查询商品类型（名称字典）。
     * 商家申请上架时通过 {@code GET /portal/getGoodsType} 拉取列表供选择；管理员亦用于维护。
     */
    @Override
    public List<PortalGoodsTypeDto> getType(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermissionOr("manager","merchant");
        IPage<PortalGoodsType> page = new Page<>(pageNum,pageSize);
        portalGoodsTypeDao.selectPage(page, new LambdaQueryWrapper<PortalGoodsType>()
                .orderByAsc(PortalGoodsType::getTypeId));
        return page.convert(portalGoodsType -> {
            PortalGoodsTypeDto portalGoodsTypeDto = new PortalGoodsTypeDto();
            BeanUtils.copyProperties(portalGoodsType,portalGoodsTypeDto);
            return portalGoodsTypeDto;
        }).getRecords();
    }

    @Override
    public void addType(PortalGoodsTypeDto portalGoodsTypeDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        PortalGoodsType portalGoodsType = new PortalGoodsType();
        BeanUtils.copyProperties(portalGoodsTypeDto, portalGoodsType);
        Date now = new Date();
        portalGoodsType.setCreateTime(now);
        portalGoodsType.setUpdateTime(now);
        portalGoodsTypeDao.insert(portalGoodsType);
    }

    @Override
    public void updateType(PortalGoodsTypeDto portalGoodsTypeDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        if (portalGoodsTypeDto.getTypeId() == null) {
            Assert.fail("类型id不能为空");
        }
        int rows = portalGoodsTypeDao.update(null, new LambdaUpdateWrapper<PortalGoodsType>()
                .eq(PortalGoodsType::getTypeId, portalGoodsTypeDto.getTypeId())
                .set(PortalGoodsType::getTypeName, portalGoodsTypeDto.getTypeName())
                .set(PortalGoodsType::getUpdateTime, new Date()));
        if (rows == 0) {
            Assert.fail("类型不存在，更新失败");
        }
    }

    @Override
    public void deleteType(Long typeId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("manager");
        if (typeId == null) {
            Assert.fail("类型id不能为空");
        }
        int rows = portalGoodsTypeDao.deleteById(typeId);
        if (rows == 0) {
            Assert.fail("类型不存在，删除失败");
        }
    }
}
