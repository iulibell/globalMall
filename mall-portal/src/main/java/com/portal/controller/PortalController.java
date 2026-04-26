package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.PortalGoodsDto;
import com.portal.dto.PortalGoodsNeededDto;
import com.portal.dto.PortalOffShelfSysDto;
import com.portal.service.MerchantService;
import com.portal.service.PortalService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/portal")
public class PortalController {
    @Resource
    private PortalService portalService;
    @Resource
    private MerchantService merchantService;

    @PostMapping("/goodsOnShelf")
    public void goodsOnShelf(@RequestBody PortalGoodsNeededDto portalGoodsNeededDto){
        portalService.goodsOnShelf(portalGoodsNeededDto);
    }

    /**
     * 物流侧（如 WMS 确认入库）在写库后拉取门户商品快照，用于同步 ES；走 {@code /portal/sys/**} 内部回调路径。
     */
    @GetMapping("/sys/portalGoods/{goodsId}")
    public CommonResult<PortalGoodsDto> sysPortalGoodsByGoodsId(@PathVariable String goodsId) {
        return CommonResult.success(portalService.getPortalGoodsById(goodsId));
    }

    @GetMapping("/manager/getGoodsByCategory")
    public List<PortalGoodsDto> getPortalGoods(@RequestParam(defaultValue = "1")int pageNum,
                                                      @RequestParam(defaultValue = "10")int pageSize,
                                                      @RequestParam Short category){
        return portalService.getPortalGoodsByCategory(pageNum, pageSize, category);
    }

    @GetMapping("/manager/getPortalGoodsById")
    public PortalGoodsDto getPortalGoodsById(@RequestParam String goodsId){
        return portalService.getPortalGoodsById(goodsId);
    }

    @GetMapping("/hotGoods")
    public CommonResult<List<PortalGoodsDto>> getHotPortalGoods(@RequestParam(defaultValue = "1") int pageNum,
                                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return CommonResult.success(portalService.getHotPortalGoods(pageNum, pageSize));
    }

    @PostMapping("/sys/markOffShelfCompleted")
    public CommonResult<Boolean> markOffShelfCompleted(@RequestParam Long offShelfId){
        return CommonResult.success(portalService.markOffShelfCompleted(offShelfId));
    }

    @GetMapping("/sys/offShelfPendingList")
    public CommonResult<List<PortalOffShelfSysDto>> sysOffShelfPendingList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return CommonResult.success(portalService.listOffShelfPendingForSys(pageNum, pageSize));
    }

    @PostMapping("/sys/setOffShelfFee")
    public CommonResult<Boolean> sysSetOffShelfFee(@RequestParam Long offShelfId, @RequestParam BigDecimal fee) {
        return CommonResult.success(merchantService.setOffShelfFeeFromSys(offShelfId, fee));
    }

    @PostMapping("/sys/bindOffShelfTransportOrderId")
    public CommonResult<Boolean> bindOffShelfTransportOrderId(@RequestParam Long offShelfId,
                                                               @RequestParam String transportOrderId) {
        return CommonResult.success(portalService.bindOffShelfTransportOrderId(offShelfId, transportOrderId));
    }

    @GetMapping("/getGoodsDetail")
    public CommonResult<?> getGoodsDetail(@RequestParam String goodsId){
        portalService.clickGoods(goodsId);
        return CommonResult.success(portalService.getGoodsDetail(goodsId));
    }

    @PostMapping("/getGoodsDetailBatch")
    public CommonResult<List<PortalGoodsDto>> getGoodsDetailBatch(@RequestBody List<String> goodsIds){
        return CommonResult.success(portalService.getGoodsDetailBatch(goodsIds));
    }
}
