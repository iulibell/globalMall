package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.PortalGoodsDto;
import com.portal.dto.PortalGoodsNeededDto;
import com.portal.service.PortalService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal")
public class PortalController {
    @Resource
    private PortalService portalService;

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

    @GetMapping("/manager/getPortalGoodsByCategory")
    public List<PortalGoodsDto> getRegularPortalGoods(@RequestParam(defaultValue = "1")int pageNum,
                                                      @RequestParam(defaultValue = "10")int pageSize,
                                                      @RequestParam Short category){
        return portalService.getPortalGoodsByCategory(pageNum, pageSize, category);
    }

    @GetMapping("/manager/getPortalGoodsById")
    public PortalGoodsDto getPortalGoodsById(@RequestParam String goodsId){
        return portalService.getPortalGoodsById(goodsId);
    }

    @PostMapping("/sys/markOffShelfCompleted")
    public CommonResult<Boolean> markOffShelfCompleted(@RequestParam Long offShelfId){
        return CommonResult.success(portalService.markOffShelfCompleted(offShelfId));
    }

    @GetMapping("/getGoodsDetail")
    public CommonResult<?> getGoodsDetail(@RequestParam String goodsId){
        return CommonResult.success(portalService.getGoodsDetail(goodsId));
    }
}
