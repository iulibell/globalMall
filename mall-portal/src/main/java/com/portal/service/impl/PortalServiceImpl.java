package com.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.constant.PortalConstant;
import com.common.constant.RedisConstant;
import com.common.service.RedisService;
import com.portal.dao.PortalGoodsDao;
import com.portal.dao.PortalOffShelfDao;
import com.portal.dto.PortalGoodsDto;
import com.portal.dto.PortalGoodsNeededDto;
import com.portal.entity.PortalGoods;
import com.portal.entity.PortalOffShelf;
import com.portal.service.PortalService;
import jakarta.annotation.Resource;
import org.redisson.Redisson;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PortalServiceImpl implements PortalService {

    @Resource
    private PortalGoodsDao portalGoodsDao;
    @Resource
    private PortalOffShelfDao portalOffShelfDao;
    @Resource
    private RedisService redisService;
    @Resource
    private Redisson redisson;

    @Override
    public void goodsOnShelf(PortalGoodsNeededDto portalGoodsNeededDto) {
        portalGoodsDao.update(new LambdaUpdateWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId,portalGoodsNeededDto.getGoodsId())
                .set(PortalGoods::getLocationId,portalGoodsNeededDto.getLocationId())
                .set(PortalGoods::getSkuCode,portalGoodsNeededDto.getSkuCode())
                .set(PortalGoods::getWarehouseId,portalGoodsNeededDto.getWarehouseId())
                .set(PortalGoods::getCategory, portalGoodsNeededDto.getCategory())
                .set(PortalGoods::getStatus,(short)1));
    }

    @Override
    public List<PortalGoodsDto> getPortalGoodsByCategory(int pageNum, int pageSize, Short category) {
        IPage<PortalGoods> page = new Page<>(pageNum,pageSize);
        portalGoodsDao.selectPage(page,new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getCategory,category));
        return page.convert(portalGoods -> {
            PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
            BeanUtils.copyProperties(portalGoods,portalGoodsDto);
            return portalGoodsDto;
        }).getRecords();
    }

    @Override
    public PortalGoodsDto getPortalGoodsById(String goodsId) {
        PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
        BeanUtils.copyProperties(portalGoodsDao.selectOne(new LambdaUpdateWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId,goodsId)),portalGoodsDto);
        return portalGoodsDto;
    }

    @Override
    public boolean markOffShelfCompleted(Long offShelfId) {
        int rows = portalOffShelfDao.update(null, new LambdaUpdateWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getId, offShelfId)
                .eq(PortalOffShelf::getStatus, (short) 1)
                .set(PortalOffShelf::getStatus, (short) 3)
                .set(PortalOffShelf::getUpdateTime, new Date()));
        return rows > 0;
    }

    @Override
    public PortalGoodsDto getGoodsDetail(String goodsId) {
        PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
        BeanUtils.copyProperties(portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId,goodsId)),portalGoodsDto);
        return portalGoodsDto;
    }

    @Override
    public List<PortalGoodsDto> getHotPortalGoods(int pageNum, int pageSize) {
        IPage<PortalGoods> page = new Page<>(pageNum, pageSize);
        portalGoodsDao.selectPage(page, new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getStatus, (short) 1)
                .ge(PortalGoods::getVisitVolume, PortalConstant.HOT_GOODS_VISIT_THRESHOLD)
                .orderByDesc(PortalGoods::getVisitVolume)
                .orderByDesc(PortalGoods::getUpdateTime));
        return page.getRecords().stream().map(portalGoods -> {
            String cacheKey = RedisConstant.HOT_GOODS_PREFIX + portalGoods.getGoodsId();
            Object cached = redisService.get(cacheKey);
            if (cached instanceof PortalGoodsDto cachedDto) {
                return cachedDto;
            }
            PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
            BeanUtils.copyProperties(portalGoods, portalGoodsDto);
            redisService.set(cacheKey, portalGoodsDto, RedisConstant.HOT_GOODS_EXPIRE, TimeUnit.HOURS);
            return portalGoodsDto;
        }).toList();
    }

    @Override
    public void clickGoods(String goodsId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redisson.getAtomicLong(RedisConstant.VISIT_COUNT_PREFIX
                + date
                + ":"
                + goodsId).incrementAndGet();
    }
}
