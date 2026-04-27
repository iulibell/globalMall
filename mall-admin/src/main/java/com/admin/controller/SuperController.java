package com.admin.controller;

import com.admin.dto.SeckillActivityLaunchRequest;
import com.admin.dto.SysUserDto;
import com.admin.service.SuperService;
import com.common.api.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SuperController", description = "商城管理超管接口：用户全局管理与秒杀活动发起")
@RestController
@RequestMapping("/admin/super")
public class SuperController {
    @Resource
    private SuperService superService;

    @GetMapping("/fetchSysUserInfo")
    @Operation(summary = "分页查询全部用户", description = "超管权限；分页返回系统用户列表。")
    public CommonResult<?> fetchSysUserInfo(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return superService.fetchSysUserInfo(pageNum, pageSize);
    }

    @GetMapping("/fetchSysUserByUserType")
    @Operation(summary = "按身份分页查询用户", description = "超管权限；按 userType 筛选用户。")
    public CommonResult<?> fetchSysUserByUserType(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam String userType) {
        return superService.fetchSysUserByUserType(pageNum, pageSize, userType);
    }

    @GetMapping("/fetchSysUserByUserId")
    @Operation(summary = "按用户 ID 查询", description = "超管权限；根据业务 userId 查询单用户。")
    public CommonResult<?> fetchSysUserByUserId(@RequestParam String userId) {
        return superService.fetchSysUserByUserId(userId);
    }

    @PostMapping("/updateSysUserInfo")
    @Operation(summary = "更新用户信息", description = "超管权限；修改用户资料并同步至 gl-system。")
    public CommonResult<?> updateSysUserInfo(@RequestBody SysUserDto sysUserDto) {
        return superService.updateSysUserInfo(sysUserDto);
    }

    @PostMapping("/deleteSysUser")
    @Operation(summary = "删除用户", description = "超管权限；按 userId 删除系统用户。")
    public CommonResult<?> deleteSysUser(@RequestParam String userId) {
        return superService.deleteSysUserInfo(userId);
    }

    @PostMapping("/seckill/launch")
    @Operation(summary = "发起秒杀活动", description = "超管权限；经 mall-portal 写入活动主信息。")
    public CommonResult<?> launchSeckillActivity(@Valid @RequestBody SeckillActivityLaunchRequest request) {
        return superService.launchSeckillActivity(request);
    }
}
