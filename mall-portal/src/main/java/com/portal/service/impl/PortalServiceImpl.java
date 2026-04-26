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
import com.portal.dao.SeckillActivityDao;
import com.portal.dao.SeckillActivityGoodsDao;
import com.portal.dto.PortalGoodsDto;
import com.portal.dto.PortalGoodsNeededDto;
import com.portal.dto.PortalOffShelfSysDto;
import com.portal.entity.PortalGoods;
import com.portal.entity.PortalOffShelf;
import com.portal.entity.SeckillActivity;
import com.portal.entity.SeckillActivityGoods;
import com.portal.service.PortalService;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PortalServiceImpl implements PortalService {

    @Resource
    private PortalGoodsDao portalGoodsDao;
    @Resource
    private PortalOffShelfDao portalOffShelfDao;
    @Resource
    private RedisService redisService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private SeckillActivityDao seckillActivityDao;
    @Resource
    private SeckillActivityGoodsDao seckillActivityGoodsDao;

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
                .eq(PortalOffShelf::getStatus, (short) 2)
                .set(PortalOffShelf::getStatus, (short) 4)
                .set(PortalOffShelf::getUpdateTime, new Date()));
        return rows > 0;
    }

    @Override
    public List<PortalOffShelfSysDto> listOffShelfPendingForSys(int pageNum, int pageSize) {
        IPage<PortalOffShelf> page = new Page<>(pageNum, pageSize);
        portalOffShelfDao.selectPage(page, new LambdaQueryWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getStatus, (short) 0)
                .orderByDesc(PortalOffShelf::getCreateTime));
        List<PortalOffShelf> records = page.getRecords();
        if (records.isEmpty()) {
            return List.of();
        }
        List<String> goodsIds = records.stream().map(PortalOffShelf::getGoodsId).distinct().collect(Collectors.toList());
        List<PortalGoods> goodsRows = goodsIds.isEmpty()
                ? List.of()
                : portalGoodsDao.selectList(new LambdaQueryWrapper<PortalGoods>().in(PortalGoods::getGoodsId, goodsIds));
        Map<String, String> skuNameByGoodsAndMerchant = new HashMap<>();
        for (PortalGoods g : goodsRows) {
            String k = String.valueOf(g.getGoodsId()) + '\0' + String.valueOf(g.getMerchantId());
            skuNameByGoodsAndMerchant.putIfAbsent(k, g.getSkuName());
        }
        return records.stream().map(os -> {
            PortalOffShelfSysDto dto = new PortalOffShelfSysDto();
            BeanUtils.copyProperties(os, dto);
            String k = String.valueOf(os.getGoodsId()) + '\0' + String.valueOf(os.getMerchantId());
            dto.setSkuName(skuNameByGoodsAndMerchant.get(k));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean bindOffShelfTransportOrderId(Long offShelfId, String transportOrderId) {
        if (offShelfId == null || transportOrderId == null || transportOrderId.isBlank()) {
            return false;
        }
        int rows = portalOffShelfDao.update(null, new LambdaUpdateWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getId, offShelfId)
                .in(PortalOffShelf::getStatus, (short) 2, (short) 4)
                .set(PortalOffShelf::getTransportOrderId, transportOrderId.trim())
                .set(PortalOffShelf::getUpdateTime, new Date()));
        return rows > 0;
    }

    @Override
    public PortalGoodsDto getGoodsDetail(String goodsId) {
        PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
        BeanUtils.copyProperties(portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId,goodsId)),portalGoodsDto);
        applySeckillPricing(List.of(portalGoodsDto));
        return portalGoodsDto;
    }

    @Override
    public List<PortalGoodsDto> getGoodsDetailBatch(List<String> goodsIds) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            return List.of();
        }
        List<String> normalized = goodsIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<PortalGoodsDto> list = portalGoodsDao.selectList(new LambdaQueryWrapper<PortalGoods>()
                        .in(PortalGoods::getGoodsId, normalized))
                .stream()
                .map(row -> {
                    PortalGoodsDto dto = new PortalGoodsDto();
                    BeanUtils.copyProperties(row, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        applySeckillPricing(list);
        return list;
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
        }).collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
            applySeckillPricing(list);
            return list;
        }));
    }

    @Override
    public void clickGoods(String goodsId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        redissonClient.getAtomicLong(RedisConstant.VISIT_COUNT_PREFIX
                + date
                + ":"
                + goodsId).incrementAndGet();
    }

    private void applySeckillPricing(List<PortalGoodsDto> goodsDtos) {
        if (goodsDtos == null || goodsDtos.isEmpty()) return;
        List<String> goodsIds = goodsDtos.stream()
                .map(PortalGoodsDto::getGoodsId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (goodsIds.isEmpty()) return;
        Date now = new Date();
        List<SeckillActivityGoods> approvedGoods = seckillActivityGoodsDao.selectList(
                new LambdaQueryWrapper<SeckillActivityGoods>()
                        .in(SeckillActivityGoods::getGoodsId, goodsIds)
                        .eq(SeckillActivityGoods::getStatus, (short) 3)
                        .orderByDesc(SeckillActivityGoods::getUpdateTime));
        if (approvedGoods.isEmpty()) return;
        List<String> launchCodes = approvedGoods.stream()
                .map(SeckillActivityGoods::getLaunchActivityCode)
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();
        if (launchCodes.isEmpty()) return;
        List<SeckillActivity> activeLaunches = seckillActivityDao.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .in(SeckillActivity::getActivityCode, launchCodes)
                        .eq(SeckillActivity::getStatus, (short) 2)
                        .le(SeckillActivity::getStartTime, now)
                        .gt(SeckillActivity::getEndTime, now));
        if (activeLaunches.isEmpty()) return;
        Map<String, SeckillActivity> activeLaunchMap = activeLaunches.stream().collect(Collectors.toMap(
                SeckillActivity::getActivityCode,
                a -> a,
                (a, b) -> a
        ));
        Map<String, SeckillActivityGoods> bestSeckillByGoods = new HashMap<>();
        for (SeckillActivityGoods item : approvedGoods) {
            if (!activeLaunchMap.containsKey(item.getLaunchActivityCode())) continue;
            String goodsId = item.getGoodsId();
            if (goodsId == null || goodsId.isBlank()) continue;
            if (!bestSeckillByGoods.containsKey(goodsId)) {
                bestSeckillByGoods.put(goodsId, item);
            }
        }
        for (PortalGoodsDto dto : goodsDtos) {
            if (dto == null || dto.getGoodsId() == null) continue;
            SeckillActivityGoods match = bestSeckillByGoods.get(dto.getGoodsId());
            if (match == null || match.getSeckillPrice() == null) continue;
            dto.setOriginPrice(dto.getPrice());
            dto.setSeckillPrice(match.getSeckillPrice());
            dto.setPrice(match.getSeckillPrice());
            dto.setSeckill(Boolean.TRUE);
            dto.setSeckillActivityCode(match.getLaunchActivityCode());
        }
    }
}
