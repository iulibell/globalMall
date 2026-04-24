package com.admin.schedule;

import com.admin.dao.GoodsApplicationDao;
import com.admin.entity.PortalGoodsApplication;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PortalGoodsApplicationPayTimeoutSchedule {

    @Resource
    private GoodsApplicationDao goodsApplicationDao;
    /**
     * 清理消费成功的消息日志,每十分钟进行清理
     */
    @Scheduled(cron = "0 */10 * * * ?")
    @Async
    public void cleanGoodsApplication() {
        goodsApplicationDao.delete(new LambdaQueryWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getMallStatus,(short)1)
                .eq(PortalGoodsApplication::getLogisticStatus,(short)1)
                .eq(PortalGoodsApplication::getIsPay,(short)2));
    }
}
