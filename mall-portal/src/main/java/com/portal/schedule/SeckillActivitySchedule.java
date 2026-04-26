package com.portal.schedule;

import com.portal.dao.SeckillActivityDao;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SeckillActivitySchedule {
    @Resource
    private SeckillActivityDao seckillActivityDao;

    /**
     * 每 5 秒扫描一次：将预约/待开始且到达开始时间的活动置为「进行中」。
     */
    @Scheduled(fixedDelay = 5000)
    public void startDueActivities() {
        seckillActivityDao.startDueActivities(new Date());
    }

    /**
     * 每 5 秒扫描一次：将进行中且已到结束时间的活动置为「已结束」。
     */
    @Scheduled(fixedDelay = 5000)
    public void endDueActivities() {
        seckillActivityDao.endDueActivities(new Date());
    }
}
