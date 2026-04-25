package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.CreateOrderFromCartRequest;
import com.portal.dto.OmsOrderDto;
import com.portal.dto.SysUserInfoDto;
import com.portal.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/order/add")
    public CommonResult<?> addOrder(@Valid @RequestBody OmsOrderDto omsOrderDto){
        return userService.addOrder(omsOrderDto);
    }

    @PostMapping("/order/createFromCart")
    public CommonResult<?> createOrderFromCart(@Valid @RequestBody CreateOrderFromCartRequest request){
        return userService.createOrderFromCart(request);
    }

    @PostMapping("/order/pay")
    public CommonResult<?> payForOrder(@RequestParam String orderId){
        return userService.payForOrder(orderId);
    }

    @PostMapping("/order/cancel")
    public CommonResult<?> cancelOrder(@RequestParam String orderId){
        return userService.cancelOrder(orderId);
    }

    @GetMapping("/order/list")
    public CommonResult<?> getOrder(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize){
        return userService.getOrder(pageNum, pageSize);
    }

    @GetMapping("/order/detail")
    public CommonResult<?> getOrderById(@RequestParam String orderId){
        return userService.getOrderById(orderId);
    }

    @GetMapping("/order/payDeadline")
    public CommonResult<?> getOrderPayDeadline(@RequestParam String orderId){
        return userService.getOrderPayDeadline(orderId);
    }

    @PostMapping("/updateInfo")
    public CommonResult<?> updateInfo(@RequestBody SysUserInfoDto sysUserInfoDto){
        userService.updateInfo(sysUserInfoDto);
        return CommonResult.success("修改成功!");
    }
}
