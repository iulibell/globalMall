package com.system.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.system.dto.LoginParamDto;

import java.util.Map;

public interface LoginAndLogoutService {
    SaTokenInfo login(LoginParamDto loginParamDto, String requiredRoleKey);
    Map<String, Object> buildLoginResponse(SaTokenInfo saTokenInfo, String tokenPrefix);
    void logout();
}
