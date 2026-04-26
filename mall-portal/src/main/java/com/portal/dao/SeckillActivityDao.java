package com.portal.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.portal.entity.SeckillActivity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

public interface SeckillActivityDao extends BaseMapper<SeckillActivity> {

    /**
     * 将到开始时间的预约/待开始活动切换为进行中（真正开启秒杀窗口）。
     */
    @Update("""
            UPDATE seckill_activity
            SET status = 2, update_time = NOW()
            WHERE status IN (0, 1)
              AND start_time <= #{now}
              AND end_time > #{now}
            """)
    int startDueActivities(@Param("now") Date now);

    /**
     * 将已到结束时间的进行中活动切换为已结束。
     */
    @Update("""
            UPDATE seckill_activity
            SET status = 3, update_time = NOW()
            WHERE status = 2
              AND end_time <= #{now}
            """)
    int endDueActivities(@Param("now") Date now);
}
