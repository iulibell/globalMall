package com.portal.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.constant.RedisConstant;
import com.portal.dao.PortalOffShelfDao;
import com.portal.entity.PortalOffShelf;
import com.portal.service.MerchantService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class OffShelfPaymentTimeoutSchedule {
    @Resource
    private PortalOffShelfDao portalOffShelfDao;
    @Resource
    private MerchantService merchantService;

    /**
     * 每 10 分钟扫描一次未支付下架申请，超过配置时效自动标记超时。
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void markOffShelfTimeout() {
        Date deadline = new Date(System.currentTimeMillis() - RedisConstant.OFF_SHELF_PAY_EXPIRE * 3600_000L);
        List<PortalOffShelf> timeoutList = portalOffShelfDao.selectList(new LambdaQueryWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getStatus, (short) 0)
                .le(PortalOffShelf::getCreateTime, deadline));
        for (PortalOffShelf offShelf : timeoutList) {
            merchantService.markOffShelfPaymentTimeout(offShelf.getId());
        }
    }
}
