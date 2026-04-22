package com.message.schedlue;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.message.dao.MqMessageLogDao;
import com.message.entity.MqMessageLog;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MessageSchedule {

    @Resource
    private MqMessageLogDao mqMessageLogDao;
    /**
     * 清理消费成功的消息日志,每小时进行清理
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Async
    public void cleanReviewedApplication() {
        mqMessageLogDao.delete(new LambdaQueryWrapper<MqMessageLog>()
                .eq(MqMessageLog::getStatus,(short)1));
    }
}
