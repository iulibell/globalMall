package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.SeckillActivityCreateRequest;
import com.portal.service.SeckillActivityService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/merchant/seckill")
public class SeckillActivityMerchantController {
    @Resource
    private SeckillActivityService seckillActivityService;

    @PostMapping("/apply")
    public CommonResult<?> apply(@Valid @RequestBody SeckillActivityCreateRequest request) {
        return CommonResult.success(seckillActivityService.applyActivity(request));
    }

    @GetMapping("/myList")
    public CommonResult<?> myList(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        return CommonResult.success(seckillActivityService.listMyActivities(pageNum, pageSize));
    }
}
