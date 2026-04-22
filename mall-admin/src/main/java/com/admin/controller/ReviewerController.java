package com.admin.controller;

import com.admin.dto.PortalGoodsApplicationDto;
import com.admin.dto.PortalGoodsDto;
import com.admin.service.ReviewerService;
import com.common.api.CommonResult;
import com.common.dto.RegisterParamDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin")
public class ReviewerController {
    @Resource
    private ReviewerService reviewerService;

    @PostMapping("/sys/markGoodsApplyPaymentTimeout")
    @Operation(summary = "标记申请上架订单支付超时", description = "由调度或消息服务回调，记录订单支付超时状态。")
    public CommonResult<Boolean> markGoodsApplyPaymentTimeout(@RequestParam String applyId) {
        return CommonResult.success(reviewerService.markGoodsApplyPaymentTimeout(applyId));
    }

    @PostMapping("/sys/getRegisterFromSys")
    @Operation(
            summary = "接收注册申请（内部回调）",
            description = "由 gl-system 注册流程经 Feign 调用，写入待审核注册记录；路由匿名放行，无用户 token。")
    public void getRegisterFromSys(@Valid @RequestBody RegisterParamDto registerParamDto) {
        reviewerService.getRegisterFromSys(registerParamDto);
    }

    @PostMapping("/sys/addGoodsApplication")
    public void addGoodsApplication(@Valid @RequestBody PortalGoodsApplicationDto portalGoodsApplicationDto){
        reviewerService.addGoodsApplication(portalGoodsApplicationDto);
    }

    @PostMapping("/sys/accessFromWms")
    public void accessFromWms(@RequestParam String applyId,
                              @RequestParam BigDecimal fee){
        reviewerService.accessFromWms(applyId,fee);
    }

    @GetMapping("/sys/getPortalDtoById")
    public PortalGoodsDto getPortalDtoById(@RequestParam String applyId){
        return reviewerService.getPortalDtoById(applyId);
    }

    @GetMapping("/reviewer/getGoodsApplication")
    public CommonResult<?> getGoodsApplication(@RequestParam(defaultValue = "1")int pageNum,
                                               @RequestParam(defaultValue = "10")int pageSize){
        return CommonResult.success(reviewerService.getGoodsApplication(pageNum,pageSize));
    }

    @GetMapping("/merchant/getGoodsApplication")
    public CommonResult<?> getGoodsApplication(@RequestParam(defaultValue = "1")int pageNum,
                                               @RequestParam(defaultValue = "10")int pageSize,
                                               @RequestParam String merchantId){
        return CommonResult.success(reviewerService.getGoodsApplication(pageNum,pageSize,merchantId));
    }

    @GetMapping("/reviewer/fetchRegisterApplication")
    @Operation(summary = "分页查询注册申请", description = "审核员权限；返回待处理及历史注册申请列表。")
    public CommonResult<?> fetchRegisterApplication(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return CommonResult.success(reviewerService.fetchRegisterApplication(pageNum, pageSize));
    }

    @PostMapping("/reviewer/accessRegister")
    @Operation(
            summary = "通过注册申请",
            description = "审核员通过申请：写入 sys_user、清理注册 Redis 并将申请单状态置为已通过。")
    public CommonResult<?> accessRegister(@Valid @RequestBody RegisterParamDto registerParamDto) {
        reviewerService.accessRegister(registerParamDto);
        return CommonResult.success("reviewer_register_approved");
    }

    @PostMapping("/reviewer/rejectRegister")
    @Operation(summary = "拒绝注册申请", description = "审核员驳回申请，仅更新申请单状态为未通过。")
    public CommonResult<?> rejectRegister(@Valid @RequestBody RegisterParamDto registerParamDto) {
        reviewerService.rejectRegister(registerParamDto);
        return CommonResult.success("reviewer_register_rejected");
    }

    @PostMapping("/reviewer/accessGoodsApply")
    public CommonResult<?> accessGoodsApply(@RequestParam String applyId){
        reviewerService.accessGoodsApply(applyId);
        return CommonResult.success("该商品已通过审核");
    }

    @PostMapping("/reviewer/rejectGoodsApply")
    public CommonResult<?> rejectGoodsApply(@RequestParam String applyId,
                                            @RequestParam String remark){
        reviewerService.rejectGoodsApply(applyId,remark);
        return CommonResult.success("该商品已被退回");
    }
}
