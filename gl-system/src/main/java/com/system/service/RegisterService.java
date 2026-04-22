package com.system.service;

import com.common.api.CommonResult;
import com.common.dto.RegisterParamDto;

public interface RegisterService {
    CommonResult<String> register(RegisterParamDto registerParam);

    /**
     * 向手机号发送注册验证码（写入 Redis；生产环境可在此处对接短信网关）。
     */
    CommonResult<?> sendRegisterCaptcha(String phone);
}
