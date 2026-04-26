package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.PortalOffShelfPayDto;
import com.portal.dto.PortalGoodsApplicationDto;
import com.portal.dto.SysUserInfoDto;
import com.portal.service.MerchantService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/portal/merchant")
public class MerchantController {
    @Resource
    private MerchantService merchantService;

    @PostMapping("/payForInbound")
    public CommonResult<?> payForInbound(@RequestParam String applyId){
        merchantService.payForInbound(applyId);
        return CommonResult.success("支付成功,请待司机上门取件");
    }

    @PostMapping("/cancelGoodsApply")
    public CommonResult<?> cancelGoodsApply(@RequestParam String applyId){
        merchantService.cancelGoodsApply(applyId);
        return CommonResult.success("申请已取消");
    }

    @PostMapping("/applyGoods")
    public CommonResult<?> applyGoods(@RequestBody PortalGoodsApplicationDto portalGoodsApplicationDto){
        merchantService.applyGoods(portalGoodsApplicationDto);
        return CommonResult.success("申请成功，请等待审核");
    }

    @PostMapping("/applyForOffShelf")
    public CommonResult<?> applyForOffShelf(@RequestParam String goodsId,
                                            @RequestParam String city){
        merchantService.applyForOffShelf(goodsId, city);
        return CommonResult.success("下架申请已提交，状态为待审核；审核员核定费用后可支付");
    }

    /**
     * 兼容：商城审核员核定费用。主流程为物流仓管在 logi-wms「商城下架待审核」中核定（经 /portal/sys/setOffShelfFee）。
     */
    @PostMapping("/reviewer/setOffShelfFee")
    public CommonResult<?> setOffShelfFee(@RequestParam Long offShelfId,
                                          @RequestParam BigDecimal fee){
        return CommonResult.success(merchantService.setOffShelfFeeByReviewer(offShelfId, fee));
    }

    @PostMapping("/payForOffShelf")
    public CommonResult<?> payForOffShelf(@Valid @RequestBody PortalOffShelfPayDto portalOffShelfPayDto){
        merchantService.payForOffShelf(portalOffShelfPayDto);
        return CommonResult.successMsg("下架费用支付成功，物流出库单已创建");
    }

    @GetMapping("/getOffShelfList")
    public CommonResult<?> getOffShelfList(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize){
        return CommonResult.success(merchantService.getOffShelfList(pageNum, pageSize));
    }

    @PostMapping("/sys/markOffShelfPaymentTimeout")
    public CommonResult<Boolean> markOffShelfPaymentTimeout(@RequestParam Long offShelfId){
        return CommonResult.success(merchantService.markOffShelfPaymentTimeout(offShelfId));
    }

    @GetMapping("/getGoodsApplication")
    public CommonResult<?> getGoodsApplication(@RequestParam(defaultValue = "1")int pageNum,
                                               @RequestParam(defaultValue = "10")int pageSize,
                                               @RequestParam String merchantId){
        return CommonResult.success(merchantService.getGoodsApplication(pageNum, pageSize, merchantId));
    }

    @GetMapping("/getPortalGoods")
    public CommonResult<?> getPortalGoods(@RequestParam(defaultValue = "1")int pageNum,
                                          @RequestParam(defaultValue = "10")int pageSize,
                                          @RequestParam String merchantId){
        return CommonResult.success(merchantService.getPortalGoods(pageNum, pageSize, merchantId));
    }

    @PostMapping("/updateInfo")
    public CommonResult<?> updateInfo(@RequestBody SysUserInfoDto sysUserInfoDto){
        merchantService.updateInfo(sysUserInfoDto);
        return CommonResult.success("修改成功!");
    }

    /**
     * 申请商品上架时选择仓库：返回 logi-wms 中状态为启用的仓库分页列表（前端下拉用）。
     */
    @GetMapping("/getAvailableWarehouse")
    public CommonResult<?> getAvailableWarehouse(@RequestParam(defaultValue = "1")int pageNum,
                                                 @RequestParam(defaultValue = "10")int pageSize){
        return CommonResult.success(merchantService.getAvailableWarehouse(pageNum, pageSize));
    }
}
