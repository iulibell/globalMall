package com.portal.controller;

import com.common.api.CommonResult;
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
@RequestMapping("/portal/reviewer/seckill")
public class SeckillActivityReviewerController {
    @Resource
    private SeckillActivityService seckillActivityService;

    @GetMapping("/list")
    public CommonResult<?> list(@RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize,
                                @RequestParam(required = false) Short status) {
        return CommonResult.success(seckillActivityService.listForReviewer(pageNum, pageSize, status));
    }

    @PostMapping("/review")
    public CommonResult<?> review(@Valid @RequestBody SeckillActivityReviewRequest request) {
        return CommonResult.success(seckillActivityService.review(request));
    }

    @PostMapping("/cancel")
    public CommonResult<?> cancel(@RequestParam String activityCode,
                                  @RequestParam(required = false) String reviewRemark) {
        return CommonResult.success(seckillActivityService.cancelByReviewer(activityCode, reviewRemark));
    }
}
