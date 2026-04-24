package com.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.admin.service.SuperService;
import com.admin.service.client.SystemServiceClient;
import com.common.api.CommonResult;
import com.admin.dto.SysUserDto;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class SuperServiceImpl implements SuperService {
    @Resource
    private SystemServiceClient systemServiceClient;

    @Override
    public CommonResult<?> fetchSysUserInfo(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("super");
        return systemServiceClient.fetchSysUserInfo(pageNum,pageSize);
    }

    @Override
    public CommonResult<?> updateSysUserInfo(SysUserDto sysUserDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("super");
        return systemServiceClient.updateSysUserInfo(sysUserDto);
    }

    @Override
    public CommonResult<?> deleteSysUserInfo(String userId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("super");
        return systemServiceClient.deleteSysUser(userId);
    }

    @Override
    public CommonResult<?> fetchSysUserByUserType(int pageNum, int pageSize, String userType) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("super");
        return systemServiceClient.fetchSysUserByUserType(pageNum,pageSize,userType);
    }

    @Override
    public CommonResult<?> fetchSysUserByUserId(String userId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("super");
        return systemServiceClient.fetchSysUserByUserId(userId);
    }
}
