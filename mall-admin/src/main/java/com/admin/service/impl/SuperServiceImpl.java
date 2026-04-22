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
        StpUtil.checkPermission("super");
        StpUtil.checkLogin();
        return systemServiceClient.fetchSysUserInfo(pageNum,pageSize);
    }

    @Override
    public CommonResult<?> updateSysUserInfo(SysUserDto sysUserDto) {
        StpUtil.checkPermission("super");
        StpUtil.checkLogin();
        return systemServiceClient.updateSysUserInfo(sysUserDto);
    }

    @Override
    public CommonResult<?> deleteSysUserInfo(String userId) {
        StpUtil.checkPermission("super");
        StpUtil.checkLogin();
        return systemServiceClient.deleteSysUser(userId);
    }

    @Override
    public CommonResult<?> fetchSysUserByUserType(int pageNum, int pageSize, String userType) {
        StpUtil.checkPermission("super");
        StpUtil.checkLogin();
        return systemServiceClient.fetchSysUserByUserType(pageNum,pageSize,userType);
    }

    @Override
    public CommonResult<?> fetchSysUserByUserId(String userId) {
        StpUtil.checkPermission("super");
        StpUtil.checkLogin();
        return systemServiceClient.fetchSysUserByUserId(userId);
    }
}
