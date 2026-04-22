package com.system.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.common.api.CommonResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.constant.RedisConstant;
import com.common.exception.Assert;
import com.common.service.RedisService;
import com.system.dao.SysUserDao;
import com.common.dto.RegisterParamDto;
import com.system.entity.SysUser;
import com.system.service.RegisterService;
import com.system.service.client.AdminServiceClient;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RegisterServiceImpl implements RegisterService {

    private static final Logger log = LoggerFactory.getLogger(RegisterServiceImpl.class);

    @Resource
    private AdminServiceClient adminServiceClient;
    @Resource
    private RedisService redisService;
    @Resource
    private SysUserDao sysUserDao;

    /**
     * 为 true 时，接口 {@code data} 中返回 {@code debugCode}，便于本地无短信网关联调；生产务必为 false。
     */
    @Value("${app.register.captcha.debug-return:false}")
    private boolean registerCaptchaDebugReturn;

    @Override
    public CommonResult<?> sendRegisterCaptcha(String phone) {
        if (StrUtil.isBlank(phone) || !phone.matches("^1\\d{10}$")) {
            return CommonResult.failed("register_phone_invalid");
        }
        String sendKey = RedisConstant.REGIS_CAPTCHA_SEND_PREFIX + phone;
        if (redisService.hasKey(sendKey)) {
            return CommonResult.failed("register_captcha_rate_limit");
        }
        String code = RandomUtil.randomNumbers(6);
        String captchaKey = RedisConstant.REGIS_CAPTCHA_PREFIX + phone;
        redisService.set(
                captchaKey,
                code,
                RedisConstant.REGIS_CAPTCHA_EXPIRE_MINUTES,
                TimeUnit.MINUTES);
        redisService.set(sendKey, "1", RedisConstant.REGIS_CAPTCHA_SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        log.info("[注册验证码] phone={} code={} (生产环境应通过短信发送，勿依赖日志)", phone, code);

        if (registerCaptchaDebugReturn) {
            return CommonResult.success(Map.of("debugCode", code));
        }
        return CommonResult.successMsg("register_captcha_sent");
    }

    @Override
    public CommonResult<String> register(RegisterParamDto registerParamDto) {
        if (StrUtil.isEmpty(registerParamDto.getUsername()) || StrUtil.isEmpty(registerParamDto.getPassword())
                || StrUtil.isEmpty(registerParamDto.getPhone())) {
            Assert.fail("register_required_fields_incomplete");
        }
        if (StrUtil.isEmpty(registerParamDto.getPhone())) {
            Assert.fail("validation_phone_required");
        }
        if (StrUtil.isBlank(registerParamDto.getVerifyCode()) || !registerParamDto.getVerifyCode().matches("\\d{6}")) {
            return CommonResult.failed("register_verify_required");
        }

        String captchaKey = RedisConstant.REGIS_CAPTCHA_PREFIX + registerParamDto.getPhone();
        Object cached = redisService.get(captchaKey);
        if (cached == null || !registerParamDto.getVerifyCode().equals(String.valueOf(cached).trim())) {
            return CommonResult.failed("register_verify_wrong");
        }
        redisService.delete(captchaKey);

        if (sysUserDao.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, registerParamDto.getPhone())) != null) {
            return CommonResult.failed("register_user_exists");
        }
        if (redisService.get(RedisConstant.REGIS_KEY_PREFIX + registerParamDto.getPhone()) == null) {
            registerParamDto.setStatus((short) 0);
            redisService.set(
                    RedisConstant.REGIS_KEY_PREFIX + registerParamDto.getPhone(),
                    registerParamDto,
                    RedisConstant.REGIS_EXPIRE_TIME,
                    TimeUnit.HOURS);
            adminServiceClient.applyRegister(registerParamDto);
            return CommonResult.success("register_apply_success");
        } else {
            return CommonResult.failed("register_duplicate_apply");
        }
    }
}
