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
import com.common.service.RedisService;
import com.system.dao.SysUserDao;
import com.system.entity.SysUser;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ReviewerServiceImpl implements ReviewerService {
    @Resource
    private RegisterApplicationDao registerApplicationDao;
    @Resource
    private RedisService redisService;
    @Resource
    private RabbitTemplate rabbitTemplate;
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
        RegisterApplication app = new RegisterApplication();
        app.setUsername(registerParamDto.getUsername());
        app.setPassword(BCrypt.hashpw(registerParamDto.getPassword(), BCrypt.gensalt()));
        app.setNickname(registerParamDto.getNickname());
        if (StringUtils.hasText(registerParamDto.getUserType())) {
            app.setUserType(Short.parseShort(registerParamDto.getUserType().trim()));
        }
        app.setPhone(registerParamDto.getPhone());
        app.setStatus(registerParamDto.getStatus());
        registerApplicationDao.insert(app);
    }

    @Override
    @Transactional
    public void addGoodsApplication(PortalGoodsApplicationDto portalGoodsApplicationDto){
        PortalGoodsApplication portalGoodsApplication = new PortalGoodsApplication();
        BeanUtils.copyProperties(portalGoodsApplicationDto, portalGoodsApplicationDto);
        goodsApplicationDao.insert(portalGoodsApplication);
    }

    @Transactional
    public void accessRegister(RegisterParamDto registerParamDto){
        StpUtil.checkPermission("reviewer");
        StpUtil.checkLogin();
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
        StpUtil.checkPermission("reviewer");
        StpUtil.checkLogin();
        IPage<RegisterApplication> page = new Page<>(pageNum, pageSize);
        return registerApplicationDao.selectPage(page,
                new LambdaQueryWrapper<>()).getRecords();
    }

    @Transactional
    public void rejectRegister(RegisterParamDto registerParamDto){
        StpUtil.checkPermission("reviewer");
        StpUtil.checkLogin();
        confirmReviewed(registerParamDto,(short) 2);
    }

    @Override
    public List<PortalGoodsApplicationDto> getGoodsApplication(int pageNum, int pageSize) {
        StpUtil.checkPermission("reviewer");
        StpUtil.checkLogin();
        IPage<PortalGoodsApplication> page = new Page<>(pageNum,pageSize);
        goodsApplicationDao.selectPage(page,new LambdaQueryWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getStatus,(short)0));
        return page.convert(portalGoodsApplication -> {
            PortalGoodsApplicationDto portalGoodsApplicationDto = new PortalGoodsApplicationDto();
            BeanUtils.copyProperties(portalGoodsApplication, portalGoodsApplicationDto);
            return portalGoodsApplicationDto;
        }).getRecords();
    }

    @Override
    public void accessGoodsApply(String applyId) {
        StpUtil.checkPermission("reviewer");
        StpUtil.checkLogin();
        WmsInboundApplyDto wmsInboundApplyDto = new WmsInboundApplyDto();
        BeanUtils.copyProperties(goodsApplicationDao.selectOne(new LambdaQueryWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId,applyId)),wmsInboundApplyDto);
        wmsInboundApplyDto.setStatus((short)0);
        wmsServiceClient.addInboundApply(wmsInboundApplyDto);
    }

    @Override
    public void accessFromWms(String applyId, BigDecimal fee) {
        goodsApplicationDao.update(new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getFee,fee));
        confirmReviewed(applyId,(short) 1);
        redisService.set(RedisConstant.GOODS_APPLY_PREFIX + applyId,
                "",
                RedisConstant.GOODS_APPLY_PAY_EXPIRE,
                TimeUnit.HOURS);
        rabbitTemplate.convertAndSend(RabbitConstant.GOODS_TTL_EXCHANGE,
                RabbitConstant.GOODS_TTL_ROUTING_KEY,
                applyId);
        //TODO 邮件提醒商家支付
    }

    @Override
    public void rejectGoodsApply(String applyId, String remark) {
        StpUtil.checkPermission("reviewer");
        StpUtil.checkLogin();
        confirmReviewed(applyId,(short)2);
    }

    @Override
    public void rejectFromWms(String applyId) {
        confirmReviewed(applyId,(short) 2);
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
    public boolean markGoodsApplyPaymentTimeout(String applyId) {
        int rows = goodsApplicationDao.update(null, new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId, applyId)
                .eq(PortalGoodsApplication::getStatus, (short) 1)
                .set(PortalGoodsApplication::getIsPay, (short) 2));
        if (rows > 0) {
            redisService.delete(RedisConstant.GOODS_APPLY_PAY_PREFIX + applyId);
        }
        return rows > 0;
    }

    @Override
    public PortalGoodsDto getPortalDtoById(String applyId) {
        String goodsId = String.valueOf(snowflakeIdGenerator.nextId() + applyId.indexOf(1,5));
        PortalGoodsDto portalGoodsDto = new PortalGoodsDto();
        BeanUtils.copyProperties(goodsApplicationDao.selectOne(new LambdaQueryWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId,applyId)),portalGoodsDto);
        portalGoodsDto.setGoodsId(goodsId);
        return portalGoodsDto;
    }

    public void confirmReviewed(RegisterParamDto registerParamDto,short status){
        StpUtil.checkPermission("reviewer");
        StpUtil.checkLogin();
        registerApplicationDao.update(new LambdaUpdateWrapper<RegisterApplication>()
                .eq(RegisterApplication::getPhone,registerParamDto.getPhone())
                .set(RegisterApplication::getStatus,status));
    }

    public void confirmReviewed(String applyId,short status){
        StpUtil.checkPermission("reviewer");
        StpUtil.checkLogin();
        goodsApplicationDao.update(new LambdaUpdateWrapper<PortalGoodsApplication>()
                .eq(PortalGoodsApplication::getApplyId,applyId)
                .set(PortalGoodsApplication::getStatus,status));
    }
}
