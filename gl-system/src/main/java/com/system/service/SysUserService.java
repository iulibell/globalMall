package com.system.service;

import com.system.dto.SysUserDto;

import java.util.List;

public interface SysUserService {
    /**
     * 获取系统用户信息(super操作)
     * @return 系统用户信息
     */
    List<SysUserDto> fetchSysUserInfo(int pageNum,int pageSize);

    /**
     * 根据用户类型返回符合用户类型的用户列表
     * @param pageNum 页数
     * @param pageSize 页大小
     * @param userType 用户类型
     * @return 用户列表
     */
    List<SysUserDto> fetchSysUserByUserType(int pageNum, int pageSize, String userType);

    /**
     * 根据用户id返回单个用户(super操作)
     * @param userId 用户id
     * @return 单个用户；不存在时返回 {@code null}
     */
    SysUserDto fetchSysUserByUserId(String userId);

    /**
     * 对系统用户进行更新操作(super操作)
     * @param sysUserDto 用户dto
     */
    void updateSysUserInfo(SysUserDto sysUserDto);

    /**
     * 根据用户id进行用户删除
     * @param userId 用户id
     */
    void deleteSysUser(String userId);
}
