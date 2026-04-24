package com.system.controller;

import com.system.dto.SysUserInfoDto;
import com.system.service.SysUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
@Tag(name = "SysUserInfoController", description = "预留：门户用户自助维护资料等接口（当前无对外端点）。")
public class SysUserInfoController {
    @Resource
    private SysUserService sysUserService;

    @PostMapping("/updateInfo")
    public void updateInfo(@RequestBody SysUserInfoDto sysUserInfoDto) {
        sysUserService.updateInfo(sysUserInfoDto);
    }
}
