package com.system.controller;

import com.common.api.CommonResult;
import com.system.dto.SysUserDto;
import com.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system")
@Tag(
        name = "AdminOperationController",
        description = "供管理端 Feign 调用的系统用户维护接口：分页查询、按身份/ID 查询、更新与删除（需相应服务间鉴权）。")
public class AdminOperationController {

    @Resource
    private SysUserService sysUserService;

    @GetMapping("/admin/fetchSysUserInfo")
    @Operation(summary = "分页查询系统用户", description = "按页返回 sys_user 列表，供超管/管理端同步用户数据。")
    public CommonResult<?> fetchSysUserInfo(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return CommonResult.success(sysUserService.fetchSysUserInfo(pageNum, pageSize));
    }

    @GetMapping("/admin/fetchSysUserByUserType")
    @Operation(summary = "按用户身份分页查询", description = "userType 为数字或角色别名，筛选对应类型的系统用户。")
    public CommonResult<?> fetchSysUserByUserType(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam String userType) {
        return CommonResult.success(sysUserService.fetchSysUserByUserType(pageNum, pageSize, userType));
    }

    @GetMapping("/admin/fetchSysUserByUserId")
    @Operation(summary = "按业务用户 ID 查询", description = "根据 snowflake userId 精确查询单条 sys_user。")
    public CommonResult<?> fetchSysUserByUserId(@RequestParam String userId) {
        return CommonResult.success(sysUserService.fetchSysUserByUserId(userId));
    }

    @PostMapping("/admin/updateSysUserInfo")
    @Operation(summary = "更新系统用户", description = "按 DTO 更新用户资料（昵称、手机、身份等），由管理端发起。")
    public CommonResult<?> updateSysUserInfo(@RequestBody SysUserDto sysUserDto) {
        sysUserService.updateSysUserInfo(sysUserDto);
        return CommonResult.success("admin_user_updated");
    }

    @PostMapping("/admin/deleteSysUser")
    @Operation(summary = "删除系统用户", description = "按业务 userId 删除账号，请谨慎调用。")
    public CommonResult<?> deleteSysUser(@RequestParam String userId) {
        sysUserService.deleteSysUser(userId);
        return CommonResult.success("admin_user_deleted");
    }
}
