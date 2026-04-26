package com.portal.service;

import com.portal.dto.SeckillActivityCreateRequest;
import com.portal.dto.SeckillActivityLaunchRequest;
import com.portal.dto.SeckillActivityReviewRequest;
import com.portal.entity.SeckillActivity;
import com.portal.entity.SeckillActivityGoods;

import java.util.List;

public interface SeckillActivityService {
    /**
     * super 发起秒杀活动（活动主信息）
     */
    SeckillActivity launchActivity(SeckillActivityLaunchRequest request);

    /**
     * 商家报名秒杀活动（创建后置为待审核）
     */
    SeckillActivityGoods applyActivity(SeckillActivityCreateRequest request);

    /**
     * 商家查询自己的秒杀活动
     */
    List<SeckillActivityGoods> listMyActivities(int pageNum, int pageSize);

    /**
     * 审核员分页查询活动
     */
    List<SeckillActivityGoods> listForReviewer(int pageNum, int pageSize, Short status);

    /**
     * 审核员审核（通过/驳回）
     */
    boolean review(SeckillActivityReviewRequest request);

    /**
     * 审核员取消活动（仅待审核、待开始可取消）
     */
    boolean cancelByReviewer(String activityCode, String reviewRemark);
}
