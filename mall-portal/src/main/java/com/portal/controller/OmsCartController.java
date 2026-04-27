package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.OmsCartDto;
import com.portal.service.OmsCartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OmsCartController", description = "门户购物车接口：购物车增删改查、勾选与结算预览")
@RestController
@RequestMapping("/portal/user/cart")
public class OmsCartController {
    @Resource
    private OmsCartService omsCartService;

    @PostMapping("/add")
    public CommonResult<?> add(@Valid @RequestBody OmsCartDto omsCartDto) {
        omsCartService.addCart(omsCartDto);
        return CommonResult.success("加入购物车成功");
    }

    @PostMapping("/updateQuantity")
    public CommonResult<?> updateQuantity(@RequestParam Long id,
                                          @RequestParam Integer quantity) {
        omsCartService.updateQuantity(id, quantity);
        return CommonResult.success("数量更新成功");
    }

    @PostMapping("/check")
    public CommonResult<?> check(@RequestParam Long id,
                                 @RequestParam Short checked) {
        omsCartService.checkCart(id, checked);
        return CommonResult.success("勾选状态更新成功");
    }

    @PostMapping("/checkAll")
    public CommonResult<?> checkAll(@RequestParam Short checked) {
        omsCartService.checkAll(checked);
        return CommonResult.success("全选状态更新成功");
    }

    @PostMapping("/delete")
    public CommonResult<?> delete(@RequestParam Long id) {
        omsCartService.deleteCart(id);
        return CommonResult.success("删除成功");
    }

    @GetMapping("/list")
    public CommonResult<?> list() {
        return CommonResult.success(omsCartService.listCart());
    }

    @GetMapping("/settlePreview")
    public CommonResult<?> settlePreview() {
        return CommonResult.success(omsCartService.settlePreview());
    }
}
