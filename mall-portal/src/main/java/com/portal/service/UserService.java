package com.portal.service;

import com.common.api.CommonResult;
import com.portal.dto.CreateOrderFromCartRequest;
import com.portal.dto.OmsOrderDto;
import com.portal.dto.SysUserInfoDto;

public interface UserService {
    /**
     * 创建订单(user操作)
     */
    CommonResult<?> addOrder(OmsOrderDto omsOrderDto);

    /**
     * 从购物车创建待支付订单(user操作)
     */
    CommonResult<?> createOrderFromCart(CreateOrderFromCartRequest request);

    /**
     * 支付订单(user操作)
     */
    CommonResult<?> payForOrder(String orderId);

    /**
     * 取消订单(user操作)
     */
    CommonResult<?> cancelOrder(String orderId);

    /**
     * 分页查询订单(user操作)
     */
    CommonResult<?> getOrder(int pageNum, int pageSize);

    /**
     * 查询单个订单(user操作)
     */
    CommonResult<?> getOrderById(String orderId);

    /**
     * 查询订单支付截止信息(user操作)
     */
    CommonResult<?> getOrderPayDeadline(String orderId);

    /**
     * 更新个人信息(user操作)
     */
    void updateInfo(SysUserInfoDto sysUserInfoDto);
}
