package com.portal.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.Assert;
import com.portal.dao.PortalGoodsDao;
import com.portal.dao.SeckillActivityDao;
import com.portal.dao.SeckillActivityGoodsDao;
import com.portal.dto.SeckillActivityCreateRequest;
import com.portal.dto.SeckillActivityLaunchRequest;
import com.portal.dto.SeckillActivityReviewRequest;
import com.portal.entity.PortalGoods;
import com.portal.entity.SeckillActivity;
import com.portal.entity.SeckillActivityGoods;
import com.portal.service.SeckillActivityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SeckillActivityServiceImpl implements SeckillActivityService {
    @Resource
    private SeckillActivityDao seckillActivityDao;
    @Resource
    private SeckillActivityGoodsDao seckillActivityGoodsDao;
    @Resource
    private PortalGoodsDao portalGoodsDao;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public SeckillActivity launchActivity(SeckillActivityLaunchRequest request) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("super");
        String activityName = request.getActivityName() == null ? "" : request.getActivityName().trim();
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        if (activityName.isBlank()) {
            Assert.fail("活动名称不能为空");
        }
        if (startTime == null || endTime == null || !endTime.after(startTime)) {
            Assert.fail("结束时间必须晚于开始时间");
        }
        Date now = new Date();
        if (now.before(startTime) || !now.before(endTime)) {
            Assert.fail("仅可在预约开始到结束时间内报名活动");
        }
        SeckillActivity exists = seckillActivityDao.selectOne(new LambdaQueryWrapper<SeckillActivity>()
                .eq(SeckillActivity::getActivityName, activityName)
                .eq(SeckillActivity::getStartTime, startTime)
                .eq(SeckillActivity::getEndTime, endTime)
                .in(SeckillActivity::getStatus, (short) 0, (short) 1, (short) 2));
        if (exists != null) {
            Assert.fail("相同活动窗口已存在");
        }
        SeckillActivity activity = new SeckillActivity();
        activity.setActivityCode("SKA" + snowflakeIdGenerator.nextId());
        activity.setActivityName(activityName);
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setStatus((short) 0);
        activity.setLauncherId(StpUtil.getLoginIdAsString());
        activity.setRemark(request.getRemark());
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        seckillActivityDao.insert(activity);
        return activity;
    }

    @Override
    public SeckillActivityGoods applyActivity(SeckillActivityCreateRequest request) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        String merchantId = StpUtil.getLoginIdAsString();
        return buildAndInsertActivityGoods(
                merchantId,
                request.getGoodsId(),
                request.getActivityName(),
                request.getSeckillPrice(),
                request.getTotalStock(),
                request.getPerUserLimit(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRemark()
        );
    }

    private SeckillActivityGoods buildAndInsertActivityGoods(String merchantIdRaw,
                                                             String goodsIdRaw,
                                                             String activityNameRaw,
                                                             java.math.BigDecimal seckillPrice,
                                                             Integer totalStock,
                                                             Integer perUserLimit,
                                                             Date startTime,
                                                             Date endTime,
                                                             String remark) {
        Date now = new Date();
        String merchantId = merchantIdRaw == null ? "" : merchantIdRaw.trim();
        String goodsId = goodsIdRaw == null ? "" : goodsIdRaw.trim();
        String activityName = activityNameRaw == null ? "" : activityNameRaw.trim();
        if (merchantId.isBlank() || goodsId.isBlank() || activityName.isBlank()) {
            Assert.fail("商家、商品与活动名称不能为空");
        }
        if (startTime == null || endTime == null || !endTime.after(startTime)) {
            Assert.fail("结束时间必须晚于开始时间");
        }
        if (!endTime.after(now)) {
            Assert.fail("结束时间必须晚于当前时间");
        }
        SeckillActivity launch = seckillActivityDao.selectOne(new LambdaQueryWrapper<SeckillActivity>()
                .eq(SeckillActivity::getActivityName, activityName)
                .eq(SeckillActivity::getStartTime, startTime)
                .eq(SeckillActivity::getEndTime, endTime)
                .in(SeckillActivity::getStatus, (short) 0, (short) 1, (short) 2));
        if (launch == null) {
            Assert.fail("活动不存在或已失效，请联系super重新发起活动");
        }
        if (seckillPrice == null || seckillPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            Assert.fail("秒杀价必须大于0");
        }
        if (totalStock == null || totalStock <= 0 || perUserLimit == null || perUserLimit <= 0) {
            Assert.fail("活动库存与限购数量必须大于0");
        }
        PortalGoods goods = portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, goodsId)
                .eq(PortalGoods::getMerchantId, merchantId)
                .eq(PortalGoods::getStatus, (short) 1));
        if (goods == null) {
            Assert.fail("商品不存在、未上架或不属于当前商家");
        }
        if (seckillPrice.compareTo(goods.getPrice()) >= 0) {
            Assert.fail("秒杀价必须低于原价");
        }
        SeckillActivityGoods exists = seckillActivityGoodsDao.selectOne(new LambdaQueryWrapper<SeckillActivityGoods>()
                .eq(SeckillActivityGoods::getLaunchActivityCode, launch.getActivityCode())
                .eq(SeckillActivityGoods::getGoodsId, goods.getGoodsId())
                .in(SeckillActivityGoods::getStatus, (short) 1, (short) 3));
        if (exists != null) {
            Assert.fail("该商品在当前活动中已报名或已通过审核");
        }
        SeckillActivityGoods apply = new SeckillActivityGoods();
        apply.setActivityCode("SKG" + snowflakeIdGenerator.nextId());
        apply.setLaunchActivityCode(launch.getActivityCode());
        apply.setActivityName(launch.getActivityName());
        apply.setGoodsId(goods.getGoodsId());
        apply.setMerchantId(merchantId);
        apply.setOriginPrice(goods.getPrice());
        apply.setSeckillPrice(seckillPrice);
        apply.setTotalStock(totalStock);
        apply.setAvailableStock(totalStock);
        apply.setPerUserLimit(perUserLimit);
        apply.setStatus((short) 1);
        apply.setRemark(remark);
        apply.setCreateTime(now);
        apply.setUpdateTime(now);
        seckillActivityGoodsDao.insert(apply);
        return apply;
    }

    @Override
    public List<SeckillActivityGoods> listMyActivities(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        String merchantId = StpUtil.getLoginIdAsString();
        IPage<SeckillActivityGoods> page = new Page<>(pageNum, pageSize);
        seckillActivityGoodsDao.selectPage(page, new LambdaQueryWrapper<SeckillActivityGoods>()
                .eq(SeckillActivityGoods::getMerchantId, merchantId)
                .orderByDesc(SeckillActivityGoods::getCreateTime));
        return page.getRecords();
    }

    @Override
    public List<SeckillActivityGoods> listForReviewer(int pageNum, int pageSize, Short status) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        IPage<SeckillActivityGoods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillActivityGoods> wrapper = new LambdaQueryWrapper<SeckillActivityGoods>()
                .orderByDesc(SeckillActivityGoods::getCreateTime);
        if (status != null) {
            wrapper.eq(SeckillActivityGoods::getStatus, status);
        }
        seckillActivityGoodsDao.selectPage(page, wrapper);
        return page.getRecords();
    }

    @Override
    public boolean review(SeckillActivityReviewRequest request) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        String reviewerId = StpUtil.getLoginIdAsString();
        String activityCode = request.getActivityCode() == null ? "" : request.getActivityCode().trim();
        if (activityCode.isBlank()) {
            Assert.fail("活动编码不能为空");
        }
        Date now = new Date();
        short targetStatus = Boolean.TRUE.equals(request.getApproved()) ? (short) 3 : (short) 2; // 3=已过审,2=未过审
        int rows = seckillActivityGoodsDao.update(null, new LambdaUpdateWrapper<SeckillActivityGoods>()
                .eq(SeckillActivityGoods::getActivityCode, activityCode)
                .eq(SeckillActivityGoods::getStatus, (short) 1)
                .set(SeckillActivityGoods::getStatus, targetStatus)
                .set(SeckillActivityGoods::getReviewerId, reviewerId)
                .set(SeckillActivityGoods::getReviewRemark, request.getReviewRemark())
                .set(SeckillActivityGoods::getUpdateTime, now));
        return rows > 0;
    }

    @Override
    public boolean cancelByReviewer(String activityCode, String reviewRemark) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        if (activityCode == null || activityCode.isBlank()) {
            Assert.fail("活动编码不能为空");
        }
        String reviewerId = StpUtil.getLoginIdAsString();
        int rows = seckillActivityGoodsDao.update(null, new LambdaUpdateWrapper<SeckillActivityGoods>()
                .eq(SeckillActivityGoods::getActivityCode, activityCode.trim())
                .eq(SeckillActivityGoods::getStatus, (short) 1)
                .set(SeckillActivityGoods::getStatus, (short) 2)
                .set(SeckillActivityGoods::getReviewerId, reviewerId)
                .set(SeckillActivityGoods::getReviewRemark, reviewRemark)
                .set(SeckillActivityGoods::getUpdateTime, new Date()));
        return rows > 0;
    }
}
