package com.portal.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.constant.RedisConstant;
import com.portal.dao.PortalGoodsDao;
import com.portal.dao.PortalOffShelfDao;
import com.portal.entity.PortalGoods;
import com.portal.entity.PortalOffShelf;
import com.portal.service.MerchantService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class VisitVolumeDeductSchedule {
    @Resource
    private PortalGoodsDao portalGoodsDao;

    /**
     * 每 6 小时扫描一次未支付下架申请，超过配置时效自动标记超时。
     */
    @Scheduled(cron = "0 6 * * * ?")
    public void markOffShelfTimeout() {
        List<PortalGoods> portalGoodsList = portalGoodsDao.selectList(new LambdaQueryWrapper<PortalGoods>()
                .le(PortalGoods::getVisitVolume,10000));
        for (PortalGoods portalGoods: portalGoodsList) {
            long deduct = portalGoods.getVisitVolume() / 3;
            portalGoodsDao.update(new LambdaUpdateWrapper<PortalGoods>()
                    .setSql("visit_volume = visit_volume - {0}",deduct));
        }
    }
}
