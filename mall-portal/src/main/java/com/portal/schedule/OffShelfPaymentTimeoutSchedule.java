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
     * 每 30 分钟扫描一次「待支付」下架申请，自进入待支付起的时效内未支付则标记超时。
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void markOffShelfTimeout() {
        Date deadline = new Date(System.currentTimeMillis() - RedisConstant.OFF_SHELF_PAY_EXPIRE * 3600_000L);
        List<PortalOffShelf> timeoutList = portalOffShelfDao.selectList(new LambdaQueryWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getStatus, (short) 1)
                .le(PortalOffShelf::getUpdateTime, deadline));
        for (PortalOffShelf offShelf : timeoutList) {
            merchantService.markOffShelfPaymentTimeout(offShelf.getId());
        }
    }
}
