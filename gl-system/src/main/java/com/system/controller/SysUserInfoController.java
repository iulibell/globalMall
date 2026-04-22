package com.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
@Tag(name = "SysUserInfoController", description = "预留：门户用户自助维护资料等接口（当前无对外端点）。")
public class SysUserInfoController {}
