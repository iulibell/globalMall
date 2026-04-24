package com.portal.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.common.api.CommonResult;
import com.common.constant.RedisConstant;
import com.common.exception.Assert;
import com.common.api.ResultCode;
import com.common.constant.PortalConstant;
import com.portal.dto.OmsOrderDto;
import com.portal.service.OmsCartService;
import com.portal.service.UserService;
import com.portal.service.client.OmsServiceClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private OmsServiceClient omsServiceClient;
    @Resource
    private OmsCartService omsCartService;

    @Override
    public CommonResult<?> addOrder(OmsOrderDto omsOrderDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        if (omsOrderDto.getPrice() == null || omsOrderDto.getQuantity() == null) {
            Assert.fail("订单金额或数量不能为空");
        }
        if (omsOrderDto.getQuantity() <= 0) {
            Assert.fail("购买数量必须大于0");
        }
        BigDecimal totalAmount = omsOrderDto.getPrice()
                .multiply(BigDecimal.valueOf(omsOrderDto.getQuantity()));
        omsOrderDto.setPrice(totalAmount);
        return executeWithRetry(() -> omsServiceClient.addOrder(omsOrderDto), "创建订单失败，请稍后重试");
    }

    @Override
    public CommonResult<?> payForOrder(String orderId, String userId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        CommonResult<OmsOrderDto> orderDetail = executeWithRetry(
                () -> omsServiceClient.getOrderById(orderId),
                "查询订单失败，请稍后重试");
        CommonResult<?> result = executeWithRetry(
                () -> omsServiceClient.payForOrder(orderId),
                "支付处理失败，请稍后重试");
        if (result != null
                && result.getCode() == ResultCode.SUCCESS.getCode()
                && orderDetail != null
                && orderDetail.getData() != null) {
            OmsOrderDto order = orderDetail.getData();
            String targetUserId = order.getUserId() == null ? userId : order.getUserId();
            omsCartService.clearBoughtCart(targetUserId, order.getGoodsId(), order.getSkuCode());
        }
        return result;
    }

    @Override
    public CommonResult<?> cancelOrder(String orderId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        return omsServiceClient.cancelOrder(orderId);
    }

    @Override
    public CommonResult<?> getOrder(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        return omsServiceClient.getOrder(pageNum, pageSize);
    }

    @Override
    public CommonResult<?> getOrderById(String orderId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        return omsServiceClient.getOrderById(orderId);
    }

    private <T> CommonResult<T> executeWithRetry(RemoteCall<T> remoteCall, String failMessage) {
        for (int i = 0; i < PortalConstant.OMS_RETRY_TIMES; i++) {
            try {
                return remoteCall.call();
            } catch (Exception ignored) {
            }
        }
        Assert.fail(failMessage);
        Assert.fail(failMessage);
        return null;
    }

    @FunctionalInterface
    private interface RemoteCall<T> {
        CommonResult<T> call();
    }
}
