package com.portal.service;

import com.portal.dto.SeckillActivityCreateRequest;
import com.portal.dto.SeckillActivityLaunchRequest;
import com.portal.dto.SeckillActivityReviewRequest;
import com.portal.entity.SeckillActivity;
import com.portal.entity.SeckillActivityGoods;

import java.util.List;

/**
 * 秒杀活动服务接口。
 * <p>
 * 覆盖活动发起、商家报名、审核与取消等核心流程。
 */
public interface SeckillActivityService {
    /**
     * 平台管理员发起秒杀活动（活动主信息）。
     *
     * @param request 活动发起参数
     * @return 创建后的活动主记录
     */
    SeckillActivity launchActivity(SeckillActivityLaunchRequest request);

    /**
     * 商家报名秒杀活动，创建后状态置为待审核。
     *
     * @param request 活动报名参数
     * @return 创建后的活动商品记录
     */
    SeckillActivityGoods applyActivity(SeckillActivityCreateRequest request);

    /**
     * 商家分页查询自己的秒杀活动记录。
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 秒杀活动商品列表
     */
    List<SeckillActivityGoods> listMyActivities(int pageNum, int pageSize);

    /**
     * 审核员分页查询活动报名记录。
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   报名状态过滤条件
     * @return 秒杀活动商品列表
     */
    List<SeckillActivityGoods> listForReviewer(int pageNum, int pageSize, Short status);

    /**
     * 审核员执行报名审核（通过/驳回）。
     *
     * @param request 审核请求
     * @return 是否审核成功
     */
    boolean review(SeckillActivityReviewRequest request);

    /**
     * 审核员取消活动（仅待审核、待开始状态可取消）。
     *
     * @param activityCode 活动编码
     * @param reviewRemark 取消备注
     * @return 是否取消成功
     */
    boolean cancelByReviewer(String activityCode, String reviewRemark);
}
