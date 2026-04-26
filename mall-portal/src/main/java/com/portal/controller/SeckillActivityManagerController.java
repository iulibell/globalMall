package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.SeckillActivityLaunchRequest;
import com.portal.dto.SeckillActivityReviewRequest;
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
@RequestMapping("/portal/seckill")
public class SeckillActivityManagerController {
    @Resource
    private SeckillActivityService seckillActivityService;

    @PostMapping("/launch")
    public CommonResult<?> launch(@Valid @RequestBody SeckillActivityLaunchRequest request) {
        return CommonResult.success(seckillActivityService.launchActivity(request));
    }

    @GetMapping("/manager/list")
    public CommonResult<?> list(@RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize,
                                @RequestParam(required = false) Short status) {
        return CommonResult.success(seckillActivityService.listForReviewer(pageNum, pageSize, status));
    }

    @PostMapping("/manager/review")
    public CommonResult<?> review(@Valid @RequestBody SeckillActivityReviewRequest request) {
        return CommonResult.success(seckillActivityService.review(request));
    }

    @PostMapping("/manager/cancel")
    public CommonResult<?> cancel(@RequestParam String activityCode,
                                  @RequestParam(required = false) String reviewRemark) {
        return CommonResult.success(seckillActivityService.cancelByReviewer(activityCode, reviewRemark));
    }
}
