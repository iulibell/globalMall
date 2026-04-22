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
import com.portal.dto.PortalOffShelfPayDto;
import com.portal.dto.PortalGoodsApplicationDto;
import com.portal.dto.PortalGoodsDto;
import com.portal.dto.WmsOutboundCreateDto;
import com.portal.entity.PortalGoods;
import com.portal.entity.PortalOffShelf;
import com.portal.service.MerchantService;
import com.portal.service.client.AdminServiceClient;
import com.portal.service.client.WmsServiceClient;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public void applyGoods(PortalGoodsApplicationDto portalGoodsApplicationDto) {
        StpUtil.checkPermission("merchant");
        StpUtil.checkLogin();
        if (StrUtil.isEmpty(portalGoodsApplicationDto.getSkuName()) || StrUtil.isEmpty(portalGoodsApplicationDto.getPicture())
        || portalGoodsApplicationDto.getPrice() == null) {
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
    public void applyForOffShelf(String goodsId) {
        StpUtil.checkPermission("merchant");
        StpUtil.checkLogin();
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
        portalOffShelf.setCity(null);
        portalOffShelf.setStatus((short) 0);
        portalOffShelf.setCreateTime(now);
        portalOffShelf.setUpdateTime(now);
        portalOffShelfDao.insert(portalOffShelf);
        redisService.set(RedisConstant.OFF_SHELF_PAY_PREFIX + portalOffShelf.getId(),
                "",
                RedisConstant.OFF_SHELF_PAY_EXPIRE,
                TimeUnit.HOURS);

        if(redisService.get(RedisConstant.HOT_GOODS_PREFIX + goodsId) != null)
            redisService.delete(RedisConstant.HOT_GOODS_PREFIX + goodsId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payForOffShelf(PortalOffShelfPayDto portalOffShelfPayDto) {
        StpUtil.checkPermission("merchant");
        StpUtil.checkLogin();
        String merchantId = StpUtil.getLoginIdAsString();

        PortalOffShelf portalOffShelf = portalOffShelfDao.selectOne(new LambdaQueryWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getId, portalOffShelfPayDto.getOffShelfId())
                .eq(PortalOffShelf::getMerchantId, merchantId)
                .eq(PortalOffShelf::getStatus, (short) 0));
        if (portalOffShelf == null) {
            Assert.fail("下架申请不存在或已支付/已超时");
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
                .eq(PortalOffShelf::getStatus, (short) 0)
                .set(PortalOffShelf::getFee, portalOffShelfPayDto.getFee())
                .set(PortalOffShelf::getCity, portalOffShelfPayDto.getCity())
                .set(PortalOffShelf::getStatus, (short) 1)
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
        wmsServiceClient.createOutbound(wmsOutboundCreateDto);

        portalGoodsDao.update(new LambdaUpdateWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, portalOffShelf.getGoodsId())
                .eq(PortalGoods::getMerchantId, merchantId)
                .set(PortalGoods::getStatus, (short) 0));
        redisService.delete(RedisConstant.OFF_SHELF_PAY_PREFIX + portalOffShelfPayDto.getOffShelfId());
    }

    @Override
    public boolean markOffShelfPaymentTimeout(Long offShelfId) {
        Date now = new Date();
        int rows = portalOffShelfDao.update(null, new LambdaUpdateWrapper<PortalOffShelf>()
                .eq(PortalOffShelf::getId, offShelfId)
                .eq(PortalOffShelf::getStatus, (short) 0)
                .set(PortalOffShelf::getStatus, (short) 2)
                .set(PortalOffShelf::getUpdateTime, now));
        if (rows > 0) {
            redisService.delete(RedisConstant.OFF_SHELF_PAY_PREFIX + offShelfId);
        }
        return rows > 0;
    }

    @Override
    public CommonResult<?> getGoodsApplication(int pageNum, int pageSize, String merchantId) {
        return adminServiceClient.getGoodsApplication(pageNum,pageSize,merchantId);
    }

    @Override
    public List<PortalGoodsDto> getPortalGoods(int pageNum, int pageSize, String merchantId) {
        IPage<PortalGoods> page = new Page<>(pageNum,pageSize);
        portalGoodsDao.selectPage(page,new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getMerchantId,merchantId));
        return page.convert(portalGoods -> {
            PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
            BeanUtils.copyProperties(portalGoods,portalGoodsDto);
            return portalGoodsDto;
        }).getRecords();
    }
}
