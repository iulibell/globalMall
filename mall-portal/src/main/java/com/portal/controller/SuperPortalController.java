package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.SeckillActivityLaunchRequest;
import com.portal.service.SeckillActivityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 超管经 mall-admin Feign 调用的门户能力；鉴权在 SeckillActivityService.launchActivity（需 super）。 */
@Tag(name = "SuperPortalController", description = "门户超管接口：发起秒杀活动")
@RestController
@RequestMapping("/portal/super")
public class SuperPortalController {
    @Resource
    private SeckillActivityService seckillActivityService;

    @PostMapping("/seckill/launch")
    public CommonResult<?> seckillLaunch(@Valid @RequestBody SeckillActivityLaunchRequest request) {
        return CommonResult.success(seckillActivityService.launchActivity(request));
    }
}
