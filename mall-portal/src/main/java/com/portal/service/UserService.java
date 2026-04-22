package com.portal.service;

import com.common.api.CommonResult;
import com.portal.dto.OmsOrderDto;

public interface UserService {
    /**
     * 创建订单(user操作)
     */
    CommonResult<?> addOrder(OmsOrderDto omsOrderDto);

    /**
     * 支付订单(user操作)
     */
    CommonResult<?> payForOrder(String orderId, String userId);

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
}
