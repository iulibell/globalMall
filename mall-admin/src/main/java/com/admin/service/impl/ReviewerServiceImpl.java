package com.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.admin.dao.GoodsApplicationDao;
import com.admin.dao.RegisterApplicationDao;
import com.admin.dto.PortalGoodsApplicationDto;
import com.admin.dto.PortalGoodsDto;
import com.admin.dto.WmsInboundApplyDto;
import com.admin.entity.PortalGoodsApplication;
import com.admin.entity.RegisterApplication;
import com.admin.service.ReviewerService;
import com.admin.service.client.WmsServiceClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.constant.RabbitConstant;
import com.common.constant.RedisConstant;
import com.common.dto.RegisterParamDto;
import com.common.exception.Assert;
import com.common.service.RedisService;
import com.common.util.PictureUtil;
import com.system.dao.SysUserDao;
import com.system.entity.SysUser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ReviewerServiceImpl implements ReviewerService {
    @Resource
    private RegisterApplicationDao registerApplicationDao;
    @Resource
    private RedisService redisService;
    @Resource
    private ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private GoodsApplicationDao goodsApplicationDao;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Resource
    private WmsServiceClient wmsServiceClient;

    /**
     * 由 gl-system 注册流程经 Feign 回调写入待审核记录；路由已在 {@link com.common.security.SaTokenRouteChecks} 中匿名放行，
     * 此处不得再要求登录/审核员权限，否则 Feign 无 token 会失败。
     */
    @Transactional
    public void getRegisterFromSys(RegisterParamDto registerParamDto){
        if (registerParamDto == null
                || !StringUtils.hasText(registerParamDto.getUsername())
                || !StringUtils.hasText(registerParamDto.getPassword())
                || !StringUtils.hasText(registerParamDto.getPhone())) {
            log.warn("忽略无效注册回调，参数缺失: {}", registerParamDto);
            return;
        }
        RegisterApplication exists = registerApplicationDao.selectOne(new LambdaQueryWrapper<RegisterApplication>()
                .eq(RegisterApplication::getPhone, registerParamDto.getPhone())
                .eq(RegisterApplication::getStatus, (short) 0));
        if (exists != null) {
            log.info("注册回调重复，手机号待审核记录已存在，phone={}", registerParamDto.getPhone());
            return;
        }
        RegisterApplication app = new RegisterApplication();
        app.setUsername(registerParamDto.getUsername());
        app.setPassword(BCrypt.hashpw(registerParamDto.getPassword(), BCrypt.gensalt()));
        app.setNickname(registerParamDto.getNickname());
        app.setUserType(parseUserType(registerParamDto.getUserType()));
        app.setPhone(registerParamDto.getPhone());
        app.setStatus(registerParamDto.getStatus() == null ? (short) 0 : registerParamDto.getStatus());
        registerApplicationDao.insert(app);
    }

    @Override
    @Transactional
    public void addGoodsApplication(PortalGoodsApplicationDto portalGoodsApplicationDto){
        PortalGoodsApplication portalGoodsApplication = new PortalGoodsApplication();
        BeanUtils.copyProperties(portalGoodsApplicationDto, portalGoodsApplication);
        if (portalGoodsApplication.getMallStatus() == null) {
            portalGoodsApplication.setMallStatus((short) 0);
        }
        if (portalGoodsApplication.getLogisticStatus() == null) {
            portalGoodsApplication.setLogisticStatus((short) 0);
        }
        if (portalGoodsApplication.getIsPay() == null) {
            portalGoodsApplication.setIsPay((short) 0);
        }
        goodsApplicationDao.insert(portalGoodsApplication);
    }

    @Transactional
    public void accessRegister(RegisterParamDto registerParamDto){
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(registerParamDto,sysUser);
        sysUser.setUserId(String.valueOf(snowflakeIdGenerator.nextId()));
        redisService.delete(RedisConstant.REGIS_KEY_PREFIX
                + registerParamDto.getPhone());
        sysUserDao.insert(sysUser);
        confirmReviewed(registerParamDto,(short) 1);
    }

    @Override
    public List<RegisterApplication> fetchRegisterApplication(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        IPage<RegisterApplication> page = new Page<>(pageNum, pageSize);
        return registerApplicationDao.selectPage(page,
                new LambdaQueryWrapper<>()).getRecords();
    }

    @Transactional
    public void rejectRegister(RegisterParamDto registerParamDto){
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        confirmReviewed(registerParamDto,(short) 2);
    }

    @Override
    public List<PortalGoodsApplicationDto> getGoodsApplication(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        IPage<PortalGoodsApplication> page = new Page<>(pageNum,pageSize);
        goodsApplicationDao.selectPage(page, new LambdaQueryWrapper<PortalGoodsApplication>()
                .nested(w -> w.eq(PortalGoodsApplication::getMallStatus, (short) 0)
                        .or()
                        .isNull(PortalGoodsApplication::getMallStatus)));
        return page.convert(portalGoodsApplication -> {
            PortalGoodsApplicationDto portalGoodsApplicationDto = new PortalGoodsApplicationDto();
            BeanUtils.copyProperties(portalGoodsApplication, portalGoodsApplicationDto);
            return portalGoodsApplicationDto;
        }).getRecords();
    }

    @Override
    public void accessGoodsApply(String applyId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        PortalGoodsApplication app = goodsApplicationDao.selectOne(new LambdaQueryWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId, applyId));
        if (app == null) {
            Assert.fail("上架申请不存在或已删除");
        }
        WmsInboundApplyDto wmsInboundApplyDto = new WmsInboundApplyDto();
        BeanUtils.copyProperties(app, wmsInboundApplyDto);
        String goodsId = buildGoodsIdFromApplyId(applyId);
        wmsInboundApplyDto.setGoodsId(goodsId);
        wmsInboundApplyDto.setMerchantRemark(app.getRemark());
        wmsInboundApplyDto.setStatus((short) 0);
        if (wmsInboundApplyDto.getApplyQuantity() == null || wmsInboundApplyDto.getApplyQuantity() < 1) {
            Assert.fail("上架申请数量非法");
        }
        wmsServiceClient.addInboundApply(wmsInboundApplyDto);
        confirmMallReviewed(applyId, (short) 1);
    }

    @Override
    public void accessFromWms(String applyId, BigDecimal fee) {
        goodsApplicationDao.update(new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId, applyId)
                .set(PortalGoodsApplication::getFee, fee)
                .set(PortalGoodsApplication::getLogisticStatus, (short) 1));
        redisService.set(RedisConstant.GOODS_APPLY_PAY_PREFIX + applyId,
                "",
                RedisConstant.GOODS_APPLY_PAY_EXPIRE,
                TimeUnit.HOURS);
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate != null) {
            rabbitTemplate.convertAndSend(RabbitConstant.GOODS_TTL_EXCHANGE,
                    RabbitConstant.GOODS_TTL_ROUTING_KEY,
                    applyId);
        } else {
            log.warn("RabbitTemplate 不可用，跳过 goodsApply 超时消息投递，applyId={}", applyId);
        }
        //TODO 邮件提醒商家支付
    }

    @Override
    public void rejectGoodsApply(String applyId, String remark) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        confirmMallReviewed(applyId,(short)2);
        deleteApplyPictureIfExists(applyId);
    }

    @Override
    public void rejectFromWms(String applyId) {
        goodsApplicationDao.update(new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId, applyId)
                .set(PortalGoodsApplication::getLogisticStatus, (short) 2));
        deleteApplyPictureIfExists(applyId);
    }

    @Override
    public List<PortalGoodsApplicationDto> getGoodsApplication(int pageNum, int pageSize, String merchantId) {
        IPage<PortalGoodsApplication> page = new Page<>(pageNum,pageSize);
        goodsApplicationDao.selectPage(page,new LambdaQueryWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getMerchantId,merchantId));
        return page.convert(portalGoodsApplication -> {
            PortalGoodsApplicationDto portalGoodsApplicationDto = new PortalGoodsApplicationDto();
            BeanUtils.copyProperties(portalGoodsApplication, portalGoodsApplicationDto);
            return portalGoodsApplicationDto;
        }).getRecords();
    }

    @Override
    public void cancelGoodsApplyByMerchant(String applyId, String merchantId) {
        int rows = goodsApplicationDao.update(null, new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId, applyId)
                .eq(PortalGoodsApplication::getMerchantId, merchantId)
                .eq(PortalGoodsApplication::getMallStatus, (short) 1)
                .eq(PortalGoodsApplication::getLogisticStatus, (short) 1)
                .eq(PortalGoodsApplication::getIsPay, (short) 0)
                .set(PortalGoodsApplication::getMallStatus, (short) 3));
        if (rows == 0) {
            Assert.fail("申请不存在或当前状态不可取消");
        }
        redisService.delete(RedisConstant.GOODS_APPLY_PAY_PREFIX + applyId);
        deleteApplyPictureIfExists(applyId);
    }

    @Override
    public void bindTransportOrderId(String applyId, String merchantId, String transportOrderId) {
        if (!StringUtils.hasText(transportOrderId)) {
            Assert.fail("运输单号不能为空");
        }
        int rows = goodsApplicationDao.update(null, new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId, applyId)
                .eq(PortalGoodsApplication::getMerchantId, merchantId)
                .eq(PortalGoodsApplication::getMallStatus, (short) 1)
                .eq(PortalGoodsApplication::getLogisticStatus, (short) 1)
                .set(PortalGoodsApplication::getTransportOrderId, transportOrderId.trim())
                .set(PortalGoodsApplication::getIsPay, (short) 1));
        if (rows == 0) {
            Assert.fail("申请不存在、无权限或当前状态不可更新支付");
        }
    }

    @Override
    public boolean markGoodsApplyPaymentTimeout(String applyId) {
        int rows = goodsApplicationDao.update(null, new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId, applyId)
                .eq(PortalGoodsApplication::getMallStatus, (short) 1)
                .eq(PortalGoodsApplication::getLogisticStatus, (short) 1)
                .set(PortalGoodsApplication::getIsPay, (short) 2));
        if (rows > 0) {
            redisService.delete(RedisConstant.GOODS_APPLY_PAY_PREFIX + applyId);
        }
        return rows > 0;
    }

    @Override
    public PortalGoodsDto getPortalDtoById(String applyId) {
        String goodsId = buildGoodsIdFromApplyId(applyId);
        PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
        BeanUtils.copyProperties(goodsApplicationDao.selectOne(new LambdaQueryWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId,applyId)),portalGoodsDto);
        portalGoodsDto.setGoodsId(goodsId);
        return portalGoodsDto;
    }

    public void confirmReviewed(RegisterParamDto registerParamDto,short status){
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        registerApplicationDao.update(new LambdaUpdateWrapper<RegisterApplication>()
                .eq(RegisterApplication::getPhone,registerParamDto.getPhone())
                .set(RegisterApplication::getStatus,status));
    }

    public void confirmMallReviewed(String applyId,short status){
        StpUtil.checkLogin();
        StpUtil.checkPermission("reviewer");
        goodsApplicationDao.update(new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId,applyId)
                .set(PortalGoodsApplication::getMallStatus,status));
    }

    private short parseUserType(String rawUserType) {
        if (!StringUtils.hasText(rawUserType)) {
            return 4;
        }
        String t = rawUserType.trim().toLowerCase();
        return switch (t) {
            case "1", "super" -> 1;
            case "2", "manager" -> 2;
            case "3", "merchant", "keeper" -> 3;
            case "4", "user", "driver" -> 4;
            case "5", "reviewer" -> 5;
            default -> 4;
        };
    }

    private String buildGoodsIdFromApplyId(String applyId) {
        return "GAP-" + applyId;
    }

    /**
     * 上架申请被驳回或取消支付后，清理申请上传图片，避免无效文件长期堆积。
     */
    private void deleteApplyPictureIfExists(String applyId) {
        try {
            PortalGoodsApplication app = goodsApplicationDao.selectOne(new LambdaQueryWrapper<PortalGoodsApplication>()
                    .eq(PortalGoodsApplication::getApplyId, applyId));
            if (app == null || !StringUtils.hasText(app.getPicture())) {
                return;
            }
            String picture = app.getPicture().trim();
            if (picture.startsWith("http://") || picture.startsWith("https://") || picture.startsWith("data:")) {
                return;
            }
            PictureUtil.deletePictureByName(picture);
        } catch (IllegalArgumentException e) {
            log.warn("图片名非法，跳过删除 applyId={} err={}", applyId, e.getMessage());
        } catch (IOException e) {
            log.warn("删除申请图片失败 applyId={} err={}", applyId, e.getMessage());
        } catch (Exception e) {
            log.warn("清理申请图片异常 applyId={} err={}", applyId, e.getMessage());
        }
    }
}
