package com.admin.schedule;

import com.admin.dao.RegisterApplicationDao;
import com.admin.entity.RegisterApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;

@Component
public class RegisterReviewedSchedule {

    @Resource
    private RegisterApplicationDao registerApplicationDao;
    /**
     * 清理已过审的注册申请,每十分钟进行清理
     */
    @Scheduled(cron = "0 */10 * * * ?")
    @Async
    public void cleanReviewedApplication() {
        registerApplicationDao.delete(new LambdaQueryWrapper<RegisterApplication>()
                .eq(RegisterApplication::getStatus,(short)1));
    }
}