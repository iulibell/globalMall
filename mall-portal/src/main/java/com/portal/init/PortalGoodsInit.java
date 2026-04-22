package com.portal.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.constant.PortalConstant;
import com.common.constant.RedisConstant;
import com.common.service.RedisService;
import com.portal.dao.PortalGoodsDao;
import com.portal.dto.PortalGoodsDto;
import com.portal.entity.PortalGoods;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PortalGoodsInit {
    private Date lastSyncTime = new Date(0L);

    @Resource
    private RedisService redisService;
    @Resource
    private PortalGoodsDao portalGoodsDao;

    @Scheduled(initialDelay = 10000, fixedDelay = 300000)
    public synchronized void hotPortalGoodsInit(){
        Date currentRun = new Date();
        List<PortalGoods> list = portalGoodsDao.selectList(new LambdaQueryWrapper<PortalGoods>()
                .ge(PortalGoods::getVisitVolume, PortalConstant.HOT_GOODS_VISIT_THRESHOLD)
                .eq(PortalGoods::getStatus,(short)1)
                .gt(PortalGoods::getUpdateTime, lastSyncTime));
        List<PortalGoodsDto> dtoList = list.stream().map(portalGoods -> {
            PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
            BeanUtils.copyProperties(portalGoods,portalGoodsDto);
            return portalGoodsDto;
        }).toList();
        for(PortalGoodsDto portalGoodsDto: dtoList){
            redisService.set(RedisConstant.HOT_GOODS_PREFIX + portalGoodsDto.getGoodsId(),
                    portalGoodsDto,
                    RedisConstant.HOT_GOODS_EXPIRE,
                    TimeUnit.HOURS);
        }
        lastSyncTime = currentRun;
    }
}
