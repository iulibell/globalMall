package com.portal.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.api.CommonResult;
import com.common.constant.RedisConstant;
import com.common.exception.Assert;
import com.common.service.RedisService;
import com.common.util.PictureUtil;
import com.portal.dao.PortalGoodsDao;
import com.portal.dao.PortalOffShelfDao;
import com.portal.dto.*;
import com.portal.entity.PortalGoods;
import com.portal.entity.PortalOffShelf;
import com.portal.service.MerchantService;
import com.portal.service.client.AdminServiceClient;
import com.portal.service.client.SystemServiceClient;
import com.portal.service.client.WmsServiceClient;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MerchantServiceImpl implements MerchantService {
    @Resource
    private AdminServiceClient adminServiceClient;
    @Resource
    private PortalGoodsDao portalGoodsDao;
    @Resource
    private PortalOffShelfDao portalOffShelfDao;
    @Resource
    private RedisService redisService;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Resource
    private WmsServiceClient wmsServiceClient;
    @Resource
    private SystemServiceClient systemServiceClient;

    @Override
    public void payForInbound(String applyId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        String merchantId = StpUtil.getLoginIdAsString();
        CommonResult<?> wmsPayResult = wmsServiceClient.payForInbound(applyId);
        if (wmsPayResult == null || wmsPayResult.getCode() != 200) {
            Assert.fail(wmsPayResult != null && StrUtil.isNotBlank(wmsPayResult.getMessage()) ? wmsPayResult.getMessage() : "支付失败");
        }
        String transportOrderId = wmsPayResult.getData() == null ? "" : String.valueOf(wmsPayResult.getData()).trim();
        if (StrUtil.isBlank(transportOrderId)) {
            Assert.fail("支付成功但运输单号为空");
        }
        CommonResult<?> bindResult = adminServiceClient.bindTransportOrderId(applyId, merchantId, transportOrderId);
        if (bindResult == null || bindResult.getCode() != 200) {
            Assert.fail(bindResult != null && StrUtil.isNotBlank(bindResult.getMessage()) ? bindResult.getMessage() : "运输单号绑定失败");
        }
        PortalGoods portalGoods = new PortalGoods();
        BeanUtils.copyProperties(adminServiceClient.getPortalDtoById(applyId), portalGoods);
        portalGoodsDao.insert(portalGoods);
    }

    @Override
    public void cancelGoodsApply(String applyId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        String merchantId = StpUtil.getLoginIdAsString();
        CommonResult<?> result = adminServiceClient.cancelGoodsApply(applyId, merchantId);
        if (result == null || result.getCode() != 200) {
            Assert.fail(result != null && StrUtil.isNotBlank(result.getMessage()) ? result.getMessage() : "取消申请失败");
        }
    }

    @Override
    public void applyGoods(PortalGoodsApplicationDto portalGoodsApplicationDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        if (StrUtil.isEmpty(portalGoodsApplicationDto.getSkuName()) || StrUtil.isEmpty(portalGoodsApplicationDto.getPicture())
        || portalGoodsApplicationDto.getPrice() == null || portalGoodsApplicationDto.getTypeId() == null) {
            Assert.fail("商品信息不完整");
        }

        try {
            String pictureName = PictureUtil.persistPictureField(portalGoodsApplicationDto.getPicture());
            portalGoodsApplicationDto.setPicture(pictureName);
        } catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail("图片上传失败，请稍后重试");
        }

        String loginId = StpUtil.getLoginIdAsString();
        if (StrUtil.isBlank(portalGoodsApplicationDto.getUserId())) {
            portalGoodsApplicationDto.setUserId(loginId);
        }
        if (StrUtil.isBlank(portalGoodsApplicationDto.getMerchantId())) {
            portalGoodsApplicationDto.setMerchantId(loginId);
        }

        String applyId = String.valueOf(snowflakeIdGenerator.nextId());
        portalGoodsApplicationDto.setApplyId(applyId);
        adminServiceClient.addGoodsApplication(portalGoodsApplicationDto);

        redisService.set(RedisConstant.GOODS_APPLY_PREFIX + portalGoodsApplicationDto.getUserId(),
                "",
                RedisConstant.GOODS_APPLY_EXPIRE,
                TimeUnit.HOURS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyForOffShelf(String goodsId, String city) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        if (StrUtil.isBlank(city)) {
            Assert.fail("城市不能为空");
        }
        String merchantId = StpUtil.getLoginIdAsString();

        PortalGoods portalGoods = portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, goodsId)
                .eq(PortalGoods::getMerchantId, merchantId)
                .eq(PortalGoods::getStatus, (short) 1));
        if (portalGoods == null) {
            Assert.fail("商品不存在或不属于当前商家，无法申请下架");
        }

        Date now = new Date();
        PortalOffShelf portalOffShelf = new PortalOffShelf();
        portalOffShelf.setGoodsId(goodsId);
        portalOffShelf.setMerchantId(merchantId);
        portalOffShelf.setFee(BigDecimal.ZERO);
        portalOffShelf.setCity(city.trim());
        portalOffShelf.setStatus((short) 0);
        portalOffShelf.setCreateTime(now);
        portalOffShelf.setUpdateTime(now);
        portalOffShelfDao.insert(portalOffShelf);

        if(redisService.get(RedisConstant.HOT_GOODS_PREFIX + goodsId) != null)
            redisService.delete(RedisConstant.HOT_GOODS_PREFIX + goodsId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payForOffShelf(PortalOffShelfPayDto portalOffShelfPayDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        String merchantId = StpUtil.getLoginIdAsString();

        PortalOffShelf portalOffShelf = portalOffShelfDao.selectOne(new LambdaQueryWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getId, portalOffShelfPayDto.getOffShelfId())
                .eq(PortalOffShelf::getMerchantId, merchantId)
                .eq(PortalOffShelf::getStatus, (short) 1));
        if (portalOffShelf == null) {
            Assert.fail("下架申请不存在、未核定为待支付或已处理");
        }
        if (portalOffShelf.getFee() == null || portalOffShelf.getFee().compareTo(BigDecimal.ZERO) <= 0) {
            Assert.fail("费用未核定，请联系物流仓管");
        }
        if (portalOffShelfPayDto.getFee() == null
                || portalOffShelf.getFee().compareTo(portalOffShelfPayDto.getFee()) != 0) {
            Assert.fail("支付金额与核定费用不一致，请刷新页面");
        }
        if (redisService.get(RedisConstant.OFF_SHELF_PAY_PREFIX + portalOffShelfPayDto.getOffShelfId()) == null) {
            Assert.fail("支付已超时或窗口已关闭，请刷新后重试");
        }

        PortalGoods portalGoods = portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, portalOffShelf.getGoodsId())
                .eq(PortalGoods::getMerchantId, merchantId)
                .eq(PortalGoods::getStatus, (short) 1));
        if (portalGoods == null) {
            Assert.fail("商品不存在或当前状态不可下架");
        }

        Date now = new Date();
        int rows = portalOffShelfDao.update(null, new LambdaUpdateWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getId, portalOffShelfPayDto.getOffShelfId())
                .eq(PortalOffShelf::getStatus, (short) 1)
                .set(PortalOffShelf::getCity, portalOffShelfPayDto.getCity())
                .set(PortalOffShelf::getStatus, (short) 2)
                .set(PortalOffShelf::getUpdateTime, now));
        if (rows == 0) {
            Assert.fail("下架申请支付状态更新失败");
        }

        WmsOutboundCreateDto wmsOutboundCreateDto = new WmsOutboundCreateDto();
        wmsOutboundCreateDto.setOutboundId(String.valueOf(snowflakeIdGenerator.nextId()));
        wmsOutboundCreateDto.setOrderId("OFF" + portalOffShelfPayDto.getOffShelfId());
        wmsOutboundCreateDto.setStockId(portalGoods.getGoodsId());
        wmsOutboundCreateDto.setWarehouseId(portalGoods.getWarehouseId());
        wmsOutboundCreateDto.setLocationId(portalGoods.getLocationId());
        wmsOutboundCreateDto.setSkuName(portalGoods.getSkuName());
        wmsOutboundCreateDto.setSkuCode(portalGoods.getSkuCode());
        wmsOutboundCreateDto.setUserPhone(portalOffShelfPayDto.getUserPhone());
        wmsOutboundCreateDto.setMerchantPhone(portalOffShelfPayDto.getMerchantPhone());
        wmsOutboundCreateDto.setCity(portalOffShelfPayDto.getCity());
        wmsOutboundCreateDto.setStatus((short) 0);
        wmsOutboundCreateDto.setPaidFee(portalOffShelf.getFee());
        // 避免在当前事务未提交时被 WMS 回调再次更新同一条 off-shelf 行，造成锁等待超时
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    wmsServiceClient.createOutbound(wmsOutboundCreateDto);
                }
            });
        } else {
            wmsServiceClient.createOutbound(wmsOutboundCreateDto);
        }

        portalGoodsDao.update(new LambdaUpdateWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, portalOffShelf.getGoodsId())
                .eq(PortalGoods::getMerchantId, merchantId)
                .set(PortalGoods::getStatus, (short) 2));
        redisService.delete(RedisConstant.OFF_SHELF_PAY_PREFIX + portalOffShelfPayDto.getOffShelfId());
    }

    @Override
    public boolean setOffShelfFeeByReviewer(Long offShelfId, BigDecimal fee) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        return setOffShelfFeeFromSys(offShelfId, fee);
    }

    @Override
    public boolean setOffShelfFeeFromSys(Long offShelfId, BigDecimal fee) {
        if (offShelfId == null) {
            Assert.fail("offShelfId不能为空");
        }
        if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
            Assert.fail("核定费用必须大于0");
        }
        Date now = new Date();
        int rows = portalOffShelfDao.update(null, new LambdaUpdateWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getId, offShelfId)
                .eq(PortalOffShelf::getStatus, (short) 0)
                .set(PortalOffShelf::getFee, fee)
                .set(PortalOffShelf::getStatus, (short) 1)
                .set(PortalOffShelf::getUpdateTime, now));
        if (rows > 0) {
            redisService.set(RedisConstant.OFF_SHELF_PAY_PREFIX + offShelfId,
                    "",
                    RedisConstant.OFF_SHELF_PAY_EXPIRE,
                    TimeUnit.HOURS);
        }
        return rows > 0;
    }

    @Override
    public List<PortalOffShelf> getOffShelfList(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        String merchantId = StpUtil.getLoginIdAsString();
        IPage<PortalOffShelf> page = new Page<>(pageNum, pageSize);
        portalOffShelfDao.selectPage(page, new LambdaQueryWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getMerchantId, merchantId)
                .orderByDesc(PortalOffShelf::getCreateTime));
        return page.getRecords();
    }

    @Override
    public boolean markOffShelfPaymentTimeout(Long offShelfId) {
        Date now = new Date();
        int rows = portalOffShelfDao.update(null, new LambdaUpdateWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getId, offShelfId)
                .eq(PortalOffShelf::getStatus, (short) 1)
                .set(PortalOffShelf::getStatus, (short) 3)
                .set(PortalOffShelf::getUpdateTime, now));
        if (rows > 0) {
            redisService.delete(RedisConstant.OFF_SHELF_PAY_PREFIX + offShelfId);
        }
        return rows > 0;
    }

    @Override
    public CommonResult<?> getGoodsApplication(int pageNum, int pageSize, String merchantId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        return adminServiceClient.getGoodsApplication(pageNum,pageSize,merchantId);
    }

    @Override
    public List<PortalGoodsDto> getPortalGoods(int pageNum, int pageSize, String merchantId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        IPage<PortalGoods> page = new Page<>(pageNum,pageSize);
        portalGoodsDao.selectPage(page,new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getMerchantId,merchantId));
        return page.convert(portalGoods -> {
            PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
            BeanUtils.copyProperties(portalGoods,portalGoodsDto);
            return portalGoodsDto;
        }).getRecords();
    }

    @Override
    public List<WmsWarehouseDto> getAvailableWarehouse(int pageNum, int pageSize) {
        return wmsServiceClient.getAvailableWarehouse(pageNum, pageSize);
    }

    @Override
    public void updateInfo(SysUserInfoDto sysUserInfoDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("merchant");
        systemServiceClient.updateInfo(sysUserInfoDto);
    }
}
