package com.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.Assert;
import com.portal.dao.PortalBrandDao;
import com.portal.dto.PortalBrandDto;
import com.portal.entity.PortalBrand;
import com.portal.service.PortalBrandService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PortalBrandServiceImpl implements PortalBrandService {
    @Resource
    private PortalBrandDao portalBrandDao;

    @Override
    public List<PortalBrandDto> getBrand(int pageNum, int pageSize) {
        IPage<PortalBrand> page = new Page<>(pageNum, pageSize);
        portalBrandDao.selectPage(page, null);
        return page.convert(portalBrand -> {
            PortalBrandDto portalBrandDto = new PortalBrandDto();
            BeanUtils.copyProperties(portalBrand, portalBrandDto);
            return portalBrandDto;
        }).getRecords();
    }

    @Override
    public void addBrand(PortalBrandDto portalBrandDto) {
        PortalBrand portalBrand = new PortalBrand();
        BeanUtils.copyProperties(portalBrandDto, portalBrand);
        Date now = new Date();
        portalBrand.setCreateTime(now);
        portalBrand.setUpdateTime(now);
        portalBrandDao.insert(portalBrand);
    }

    @Override
    public void updateBrand(PortalBrandDto portalBrandDto) {
        if (portalBrandDto.getId() == null) {
            Assert.fail("品牌id不能为空");
        }
        int rows = portalBrandDao.update(null, new LambdaUpdateWrapper<PortalBrand>()
                .eq(PortalBrand::getId, portalBrandDto.getId())
                .set(PortalBrand::getBrandId, portalBrandDto.getBrandId())
                .set(PortalBrand::getBrandName, portalBrandDto.getBrandName())
                .set(PortalBrand::getUpdateTime, new Date()));
        if (rows == 0) {
            Assert.fail("品牌不存在，更新失败");
        }
    }

    @Override
    public void deleteBrand(Long id) {
        if (id == null) {
            Assert.fail("品牌id不能为空");
        }
        int rows = portalBrandDao.deleteById(id);
        if (rows == 0) {
            Assert.fail("品牌不存在，删除失败");
        }
    }
}
