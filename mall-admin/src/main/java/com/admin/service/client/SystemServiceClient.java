package com.admin.service.client;

import com.common.api.CommonResult;
import com.admin.dto.SysUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gl-system")
public interface SystemServiceClient {
    @GetMapping("/system/admin/fetchSysUserInfo")
    CommonResult<?> fetchSysUserInfo(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "10") int pageSize);
    @GetMapping("/system/admin/fetchSysUserByUserType")
    CommonResult<?> fetchSysUserByUserType(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam String userType);
    @GetMapping("/system/admin/fetchSysUserByUserId")
    CommonResult<?> fetchSysUserByUserId(@RequestParam("userId") String userId);
    @PostMapping("/system/admin/updateSysUserInfo")
    CommonResult<?> updateSysUserInfo(@RequestBody SysUserDto sysUserDto);
    @PostMapping("/system/admin/deleteSysUser")
    CommonResult<?> deleteSysUser(@RequestParam String userId);
}
