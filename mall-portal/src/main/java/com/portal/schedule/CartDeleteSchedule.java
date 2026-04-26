package com.portal.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.portal.dao.OmsCartDao;
import com.portal.entity.OmsCart;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CartDeleteSchedule {
    @Resource
    private OmsCartDao omsCartDao;
    /**
     * 每 5 分钟扫描一次「已删除」购物车商品
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void cartDelete() {
        omsCartDao.delete(new LambdaQueryWrapper<OmsCart>()
                .eq(OmsCart::getDeleted,(short)1));
    }
}
