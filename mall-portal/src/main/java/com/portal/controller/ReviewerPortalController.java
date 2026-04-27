package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.SeckillActivityReviewRequest;
import com.portal.service.SeckillActivityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ReviewerPortalController", description = "门户审核员接口：秒杀活动审核、取消与列表查询")
@RestController
@RequestMapping("/portal/reviewer")
public class ReviewerPortalController {
    @Resource
    private SeckillActivityService seckillActivityService;

    @GetMapping("/seckill/list")
    public CommonResult<?> seckillList(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) Short status) {
        return CommonResult.success(seckillActivityService.listForReviewer(pageNum, pageSize, status));
    }

    @PostMapping("/seckill/review")
    public CommonResult<?> seckillReview(@Valid @RequestBody SeckillActivityReviewRequest request) {
        return CommonResult.success(seckillActivityService.review(request));
    }

    @PostMapping("/seckill/cancel")
    public CommonResult<?> seckillCancel(@RequestParam String activityCode,
                                         @RequestParam(required = false) String reviewRemark) {
        return CommonResult.success(seckillActivityService.cancelByReviewer(activityCode, reviewRemark));
    }
}
