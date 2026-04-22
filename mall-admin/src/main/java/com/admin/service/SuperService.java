package com.admin.service;

import com.common.api.CommonResult;
import com.admin.dto.SysUserDto;

public interface SuperService {
    /**
     * 获取用户信息列表(超管操作)
     * @param pageNum 页数
     * @param pageSize 页大小
     * @return 用户信息列表
     */
    CommonResult<?> fetchSysUserInfo(int pageNum, int pageSize);

    /**
     * 更新用户用户信息(超管操作)
     * @param sysUserDto 更新的用户信息
     * @return 提示信息
     */
    CommonResult<?> updateSysUserInfo(SysUserDto sysUserDto);

    /**
     * 根据用户id进行删除用户id(超管操作)
     * @param userId 用户id
     * @return 提示信息
     */
    CommonResult<?> deleteSysUserInfo(String userId);

    /**
     * 根据用户类型返回用户列表
     * @param userType 用户类型
     * @return 用户列表
     */
    CommonResult<?> fetchSysUserByUserType(int pageNum,int pageSize,String userType);

    /**
     * 根据用户id返回单个用户
     * @param userId 用户id
     * @return 单个用户
     */
    CommonResult<?> fetchSysUserByUserId(String userId);
}
