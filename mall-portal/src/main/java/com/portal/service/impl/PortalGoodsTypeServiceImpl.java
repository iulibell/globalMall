package com.portal.service.impl;

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

    @Override
    public List<PortalGoodsTypeDto> getType(int pageNum, int pageSize) {
        IPage<PortalGoodsType> page = new Page<>(pageNum,pageSize);
        portalGoodsTypeDao.selectPage(page,null);
        return page.convert(portalGoodsType -> {
            PortalGoodsTypeDto portalGoodsTypeDto = new PortalGoodsTypeDto();
            BeanUtils.copyProperties(portalGoodsType,portalGoodsTypeDto);
            return portalGoodsTypeDto;
        }).getRecords();
    }

    @Override
    public void addType(PortalGoodsTypeDto portalGoodsTypeDto) {
        PortalGoodsType portalGoodsType = new PortalGoodsType();
        BeanUtils.copyProperties(portalGoodsTypeDto, portalGoodsType);
        Date now = new Date();
        portalGoodsType.setCreateTime(now);
        portalGoodsType.setUpdateTime(now);
        portalGoodsTypeDao.insert(portalGoodsType);
    }

    @Override
    public void updateType(PortalGoodsTypeDto portalGoodsTypeDto) {
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
        if (typeId == null) {
            Assert.fail("类型id不能为空");
        }
        int rows = portalGoodsTypeDao.deleteById(typeId);
        if (rows == 0) {
            Assert.fail("类型不存在，删除失败");
        }
    }
}
