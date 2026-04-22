package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.OmsCartDto;
import com.portal.dto.OmsOrderDto;
import com.portal.service.OmsCartService;
import com.portal.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/user")
public class UserController {
    @Resource
    private OmsCartService omsCartService;
    @Resource
    private UserService userService;

    @PostMapping("/addCart")
    public CommonResult<?> addCart(@Valid @RequestBody OmsCartDto omsCartDto){
        omsCartService.addCart(omsCartDto);
        return CommonResult.success("添加成功!");
    }

    @PostMapping("/updateQuantity")
    public CommonResult<?> updateQuantity(@RequestParam Long id,
                                          @RequestParam String userId,
                                          @RequestParam Integer quantity){
        omsCartService.updateQuantity(id, userId, quantity);
        return CommonResult.success("数量更新成功!");
    }

    @PostMapping("/checkCart")
    public CommonResult<?> checkCart(@RequestParam Long id,
                                     @RequestParam String userId,
                                     @RequestParam Short checked){
        omsCartService.checkCart(id, userId, checked);
        return CommonResult.success("勾选状态更新成功!");
    }

    @PostMapping("/checkAllCart")
    public CommonResult<?> checkAllCart(@RequestParam String userId,
                                        @RequestParam Short checked){
        omsCartService.checkAll(userId, checked);
        return CommonResult.success("全选状态更新成功!");
    }

    @PostMapping("/deleteCart")
    public CommonResult<?> deleteCart(@RequestParam Long id,
                                      @RequestParam String userId){
        omsCartService.deleteCart(id, userId);
        return CommonResult.success("删除成功!");
    }

    @GetMapping("/getCartList")
    public CommonResult<?> getCartList(@RequestParam String userId){
        return CommonResult.success(omsCartService.listCart(userId));
    }

    @GetMapping("/settlePreview")
    public CommonResult<?> settlePreview(@RequestParam String userId){
        return CommonResult.success(omsCartService.settlePreview(userId));
    }

    @PostMapping("/addOrder")
    public CommonResult<?> addOrder(@Valid @RequestBody OmsOrderDto omsOrderDto){
        return userService.addOrder(omsOrderDto);
    }

    @PostMapping("/payForOrder")
    public CommonResult<?> payForOrder(@RequestParam String orderId,
                                       @RequestParam String userId){
        return userService.payForOrder(orderId, userId);
    }

    @PostMapping("/cancelOrder")
    public CommonResult<?> cancelOrder(@RequestParam String orderId){
        return userService.cancelOrder(orderId);
    }

    @GetMapping("/getOrder")
    public CommonResult<?> getOrder(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize){
        return userService.getOrder(pageNum, pageSize);
    }

    @GetMapping("/getOrderById")
    public CommonResult<?> getOrderById(@RequestParam String orderId){
        return userService.getOrderById(orderId);
    }
}
