package com.portal.controller;

import com.common.api.CommonResult;
import com.portal.dto.CreateDirectOrderRequest;
import com.portal.dto.CreateOrderFromCartRequest;
import com.portal.dto.OmsOrderDto;
import com.portal.dto.SysUserInfoDto;
import com.portal.service.UserService;
import com.portal.service.client.TmsServiceClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "UserController", description = "门户用户接口：下单、支付、取消订单与个人信息维护")
@RestController
@RequestMapping("/portal/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private TmsServiceClient tmsServiceClient;

    @PostMapping("/order/add")
    public CommonResult<?> addOrder(@Valid @RequestBody OmsOrderDto omsOrderDto){
        return userService.addOrder(omsOrderDto);
    }

    @PostMapping("/order/createFromCart")
    public CommonResult<?> createOrderFromCart(@Valid @RequestBody CreateOrderFromCartRequest request){
        return userService.createOrderFromCart(request);
    }

    @PostMapping("/order/createDirect")
    public CommonResult<?> createOrderDirect(@Valid @RequestBody CreateDirectOrderRequest request){
        return userService.createOrderDirect(request);
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

    @PostMapping("/order/confirmReceived")
    public CommonResult<?> confirmReceived(@RequestParam String transportOrderId){
        return tmsServiceClient.consigneeSign(transportOrderId);
    }
}
