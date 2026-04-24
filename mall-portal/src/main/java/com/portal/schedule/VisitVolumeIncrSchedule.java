package com.portal.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.constant.RedisConstant;
import com.portal.dao.PortalGoodsDao;
import com.portal.entity.PortalGoods;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VisitVolumeIncrSchedule {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private PortalGoodsDao portalGoodsDao;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 每 2 分钟扫描一次redis中含有浏览增值的商品
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void incrVisitVolume() throws InterruptedException {
        RLock lock = redissonClient.getLock(RedisConstant.VISIT_COUNT_LOCK);
        if (!lock.tryLock(RedisConstant.COUNT_LOCK_WAIT_TIME,RedisConstant.COUNT_LOCK_LEASE_TIME, TimeUnit.SECONDS))
            return;
        try{
        String date = LocalDate.now().format(DATE_FORMATTER);
        String pattern = RedisConstant.VISIT_COUNT_PREFIX
                + date + ":*";
        Iterable<String> ids = redissonClient.getKeys().getKeysByPattern(pattern);
        for (String keys: ids) {
            String[] arr = keys.split(":");
            String goodsId = arr[3];
            long delta = redissonClient.getAtomicLong(keys).getAndSet(0L);
            if (delta <= 0) {
                continue;
            }
            portalGoodsDao.update(new LambdaUpdateWrapper<PortalGoods>()
                    .eq(PortalGoods::getGoodsId, goodsId)
                    .setSql("visit_volume = visit_volume + {0}",
                            delta));
            }
        }catch (Exception e){
            log.info("统计浏览次数任务出现异常"+e.getMessage());
        }finally {
            if(lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }
}
