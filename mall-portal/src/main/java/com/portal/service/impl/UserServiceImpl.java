package com.portal.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.api.CommonResult;
import com.common.exception.Assert;
import com.common.api.ResultCode;
import com.common.constant.PortalConstant;
import com.portal.dao.OmsCartDao;
import com.portal.dao.PortalGoodsDao;
import com.portal.dao.SeckillActivityDao;
import com.portal.dao.SeckillActivityGoodsDao;
import com.portal.dto.CreateDirectOrderRequest;
import com.portal.dto.CreateOrderFromCartRequest;
import com.portal.dto.OmsOrderDto;
import com.portal.dto.SysUserInfoDto;
import com.portal.entity.OmsCart;
import com.portal.entity.PortalGoods;
import com.portal.entity.SeckillActivity;
import com.portal.entity.SeckillActivityGoods;
import com.portal.service.OmsCartService;
import com.portal.service.UserService;
import com.portal.service.client.OmsServiceClient;
import com.portal.service.client.SystemServiceClient;
import com.portal.service.client.WmsServiceClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private OmsServiceClient omsServiceClient;
    @Resource
    private OmsCartService omsCartService;
    @Resource
    private OmsCartDao omsCartDao;
    @Resource
    private PortalGoodsDao portalGoodsDao;
    @Resource
    private SystemServiceClient systemServiceClient;
    @Resource
    private WmsServiceClient wmsServiceClient;
    @Resource
    private SeckillActivityDao seckillActivityDao;
    @Resource
    private SeckillActivityGoodsDao seckillActivityGoodsDao;

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
    public CommonResult<?> createOrderFromCart(CreateOrderFromCartRequest request) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        OmsCart cart = omsCartDao.selectOne(new LambdaQueryWrapper<OmsCart>()
                .eq(OmsCart::getId, request.getCartId())
                .eq(OmsCart::getUserId, loginUserId)
                .eq(OmsCart::getDeleted, (short) 0));
        if (cart == null) {
            Assert.fail("购物车商品不存在");
        }
        PortalGoods goods = portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, cart.getGoodsId())
                .eq(PortalGoods::getStatus, (short) 1));
        if (goods == null) {
            Assert.fail("商品不存在或不可下单");
        }
        if (goods.getWarehouseId() == null || goods.getLocationId() == null) {
            Assert.fail("商品仓储信息不完整，暂无法下单");
        }
        OmsOrderDto order = new OmsOrderDto();
        order.setOrderId(UUID.randomUUID().toString().replace("-", ""));
        order.setMerchantId(cart.getMerchantId());
        order.setUserId(loginUserId);
        order.setGoodsId(cart.getGoodsId());
        order.setWarehouseId(goods.getWarehouseId());
        order.setLocationId(goods.getLocationId());
        order.setSkuName(cart.getSkuName());
        order.setSkuCode((cart.getSkuCode() == null || cart.getSkuCode().isBlank()) ? "DEFAULT" : cart.getSkuCode());
        order.setPrice(resolveOrderUnitPrice(goods.getGoodsId(), goods.getPrice()));
        order.setUserPhone(request.getUserPhone().trim());
        // 当前门户侧无商家手机号来源，先使用兜底值避免阻断下单。
        order.setMerchantPhone(request.getUserPhone().trim());
        order.setCity(request.getCity().trim());
        CommonResult<com.portal.dto.WmsWarehouseDto> warehouseRes = executeWithRetry(
                () -> wmsServiceClient.getWarehouseById(goods.getWarehouseId()),
                "查询仓库信息失败，请稍后重试");
        String warehouseCity = warehouseRes == null || warehouseRes.getData() == null
                ? null
                : warehouseRes.getData().getCity();
        if (warehouseCity == null || warehouseCity.trim().isEmpty()) {
            Assert.fail("仓库城市信息缺失，暂无法创建订单");
        }
        order.setWarehouseCity(warehouseCity.trim());
        order.setCategory(goods.getCategory() == null ? (short) 0 : goods.getCategory());
        order.setQuantity(cart.getQuantity() == null || cart.getQuantity() <= 0 ? 1 : cart.getQuantity());
        order.setType((goods.getType() == null || goods.getType().isBlank()) ? "normal" : goods.getType());
        CommonResult<?> result = addOrder(order);
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode()) {
            return result;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", order.getOrderId());
        CommonResult<?> deadlineResult = executeWithRetry(
                () -> omsServiceClient.getOrderPayDeadline(order.getOrderId()),
                "查询订单支付截止时间失败，请稍后重试");
        Object deadlinePayload = deadlineResult == null ? null : deadlineResult.getData();
        if (deadlinePayload instanceof Map<?, ?> deadlineMap) {
            payload.put("expireAt", deadlineMap.get("expireAt"));
            payload.put("remainingSeconds", deadlineMap.get("remainingSeconds"));
        }
        payload.put("message", result.getMessage());
        return CommonResult.success(payload);
    }

    @Override
    public CommonResult<?> createOrderDirect(CreateDirectOrderRequest request) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        PortalGoods goods = portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, request.getGoodsId().trim())
                .eq(PortalGoods::getStatus, (short) 1));
        if (goods == null) {
            Assert.fail("商品不存在或不可下单");
        }
        if (goods.getWarehouseId() == null || goods.getLocationId() == null) {
            Assert.fail("商品仓储信息不完整，暂无法下单");
        }
        int buyQty = request.getQuantity() == null ? 1 : request.getQuantity();
        if (buyQty <= 0) {
            Assert.fail("购买数量必须大于0");
        }
        OmsOrderDto order = new OmsOrderDto();
        order.setOrderId(UUID.randomUUID().toString().replace("-", ""));
        order.setMerchantId(goods.getMerchantId());
        order.setUserId(loginUserId);
        order.setGoodsId(goods.getGoodsId());
        order.setWarehouseId(goods.getWarehouseId());
        order.setLocationId(goods.getLocationId());
        order.setSkuName(goods.getSkuName());
        order.setSkuCode((goods.getSkuCode() == null || goods.getSkuCode().isBlank()) ? "DEFAULT" : goods.getSkuCode());
        order.setPrice(resolveOrderUnitPrice(goods.getGoodsId(), goods.getPrice()));
        order.setUserPhone(request.getUserPhone().trim());
        // 当前门户侧无商家手机号来源，先使用兜底值避免阻断下单。
        order.setMerchantPhone(request.getUserPhone().trim());
        order.setCity(request.getCity().trim());
        CommonResult<com.portal.dto.WmsWarehouseDto> warehouseRes = executeWithRetry(
                () -> wmsServiceClient.getWarehouseById(goods.getWarehouseId()),
                "查询仓库信息失败，请稍后重试");
        String warehouseCity = warehouseRes == null || warehouseRes.getData() == null
                ? null
                : warehouseRes.getData().getCity();
        if (warehouseCity == null || warehouseCity.trim().isEmpty()) {
            Assert.fail("仓库城市信息缺失，暂无法创建订单");
        }
        order.setWarehouseCity(warehouseCity.trim());
        order.setCategory(goods.getCategory() == null ? (short) 0 : goods.getCategory());
        order.setQuantity(buyQty);
        order.setType((goods.getType() == null || goods.getType().isBlank()) ? "normal" : goods.getType());
        CommonResult<?> result = addOrder(order);
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode()) {
            return result;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", order.getOrderId());
        CommonResult<?> deadlineResult = executeWithRetry(
                () -> omsServiceClient.getOrderPayDeadline(order.getOrderId()),
                "查询订单支付截止时间失败，请稍后重试");
        Object deadlinePayload = deadlineResult == null ? null : deadlineResult.getData();
        if (deadlinePayload instanceof Map<?, ?> deadlineMap) {
            payload.put("expireAt", deadlineMap.get("expireAt"));
            payload.put("remainingSeconds", deadlineMap.get("remainingSeconds"));
        }
        payload.put("message", result.getMessage());
        return CommonResult.success(payload);
    }

    @Override
    public CommonResult<?> payForOrder(String orderId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        CommonResult<OmsOrderDto> orderDetail = executeWithRetry(
                () -> omsServiceClient.getOrderById(orderId),
                "查询订单失败，请稍后重试");
        if (orderDetail == null || orderDetail.getData() == null) {
            Assert.fail("订单不存在");
        }
        OmsOrderDto order = orderDetail.getData();
        if (order.getUserId() == null || !loginUserId.equals(order.getUserId())) {
            Assert.fail("无权操作该订单");
        }
        CommonResult<?> result = executeWithRetry(
                () -> omsServiceClient.payForOrder(orderId),
                "支付处理失败，请稍后重试");
        if (result != null && result.getCode() == ResultCode.SUCCESS.getCode()) {
            String targetUserId = order.getUserId() == null ? loginUserId : order.getUserId();
            omsCartService.clearBoughtCart(targetUserId, order.getGoodsId(), order.getSkuCode());
        }
        return result;
    }

    @Override
    public CommonResult<?> cancelOrder(String orderId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        CommonResult<OmsOrderDto> orderDetail = executeWithRetry(
                () -> omsServiceClient.getOrderById(orderId),
                "查询订单失败，请稍后重试");
        if (orderDetail == null || orderDetail.getData() == null) {
            Assert.fail("订单不存在");
        }
        OmsOrderDto order = orderDetail.getData();
        if (order.getUserId() == null || !loginUserId.equals(order.getUserId())) {
            Assert.fail("无权操作该订单");
        }
        return omsServiceClient.cancelOrder(orderId);
    }

    @Override
    public CommonResult<?> getOrder(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        return omsServiceClient.getOrder(loginUserId, pageNum, pageSize);
    }

    @Override
    public CommonResult<?> getOrderById(String orderId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        CommonResult<OmsOrderDto> result = omsServiceClient.getOrderById(orderId);
        OmsOrderDto order = result == null ? null : result.getData();
        if (order == null) {
            Assert.fail("订单不存在");
        }
        String loginUserId = StpUtil.getLoginIdAsString();
        if (order.getUserId() == null || !loginUserId.equals(order.getUserId())) {
            Assert.fail("无权查看该订单");
        }
        return result;
    }

    @Override
    public CommonResult<?> getOrderPayDeadline(String orderId) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        CommonResult<OmsOrderDto> orderDetail = executeWithRetry(
                () -> omsServiceClient.getOrderById(orderId),
                "查询订单失败，请稍后重试");
        if (orderDetail == null || orderDetail.getData() == null) {
            Assert.fail("订单不存在");
        }
        OmsOrderDto order = orderDetail.getData();
        if (order.getUserId() == null || !loginUserId.equals(order.getUserId())) {
            Assert.fail("无权操作该订单");
        }
        return executeWithRetry(
                () -> omsServiceClient.getOrderPayDeadline(orderId),
                "查询订单支付截止时间失败，请稍后重试");
    }

    @Override
    public void updateInfo(SysUserInfoDto sysUserInfoDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        sysUserInfoDto.setUserId(loginUserId);
        systemServiceClient.updateInfo(sysUserInfoDto);
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

    private BigDecimal resolveOrderUnitPrice(String goodsId, BigDecimal fallbackPrice) {
        if (goodsId == null || goodsId.isBlank()) {
            return fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice;
        }
        Date now = new Date();
        List<SeckillActivityGoods> approved = seckillActivityGoodsDao.selectList(
                new LambdaQueryWrapper<SeckillActivityGoods>()
                        .eq(SeckillActivityGoods::getGoodsId, goodsId)
                        .eq(SeckillActivityGoods::getStatus, (short) 3)
                        .orderByDesc(SeckillActivityGoods::getUpdateTime)
                        .last("limit 5"));
        if (approved.isEmpty()) {
            return fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice;
        }
        List<String> launchCodes = approved.stream()
                .map(SeckillActivityGoods::getLaunchActivityCode)
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();
        if (launchCodes.isEmpty()) {
            return fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice;
        }
        List<SeckillActivity> activeLaunches = seckillActivityDao.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .in(SeckillActivity::getActivityCode, launchCodes)
                        .eq(SeckillActivity::getStatus, (short) 2)
                        .le(SeckillActivity::getStartTime, now)
                        .gt(SeckillActivity::getEndTime, now));
        if (activeLaunches.isEmpty()) {
            return fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice;
        }
        Set<String> activeCodes = activeLaunches.stream().map(SeckillActivity::getActivityCode).collect(Collectors.toSet());
        for (SeckillActivityGoods row : approved) {
            if (activeCodes.contains(row.getLaunchActivityCode()) && row.getSeckillPrice() != null) {
                return row.getSeckillPrice();
            }
        }
        return fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice;
    }
}
