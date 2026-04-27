package com.portal.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.api.CommonResult;
import com.common.exception.Assert;
import com.common.api.ResultCode;
import com.common.constant.PortalConstant;
import com.common.constant.RedisConstant;
import com.common.constant.SeckillPortalConstants;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    @Qualifier("seckillUserQtyReserveScript")
    private DefaultRedisScript<Long> seckillUserQtyReserveScript;

    /**
     * 普通下单与秒杀下单统一入口。
     * <p>
     * 主要流程：
     * 1) 校验登录用户、参数与商品状态；
     * 2) 查询仓库信息并回填订单必要字段；
     * 3) 命中秒杀活动时先进行限购/库存预占；
     * 4) 调用 OMS 创建订单；
     * 5) 失败时执行库存与用户限购计数回滚。
     *
     * @param omsOrderDto 订单请求对象（包含用户、商品及收货信息）
     * @return 统一响应结果
     */
    @Override
    public CommonResult<?> addOrder(OmsOrderDto omsOrderDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        if (omsOrderDto.getGoodsId() == null || omsOrderDto.getGoodsId().isBlank()) {
            Assert.fail("商品不能为空");
        }
        if (omsOrderDto.getQuantity() == null) {
            Assert.fail("购买数量不能为空");
        }
        if (omsOrderDto.getQuantity() <= 0) {
            Assert.fail("购买数量必须大于0");
        }
        String loginUserId = StpUtil.getLoginIdAsString();
        if (!loginUserId.equals(omsOrderDto.getUserId())) {
            Assert.fail("无权创建该订单");
        }
        PortalGoods goods = portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, omsOrderDto.getGoodsId().trim())
                .eq(PortalGoods::getStatus, (short) 1));
        if (goods == null) {
            Assert.fail("商品不存在或不可下单");
        }
        if (goods.getWarehouseId() == null || goods.getLocationId() == null) {
            Assert.fail("商品仓储信息不完整，暂无法下单");
        }
        if (!Objects.equals(goods.getMerchantId(), omsOrderDto.getMerchantId())
                || !Objects.equals(goods.getWarehouseId(), omsOrderDto.getWarehouseId())
                || !Objects.equals(goods.getLocationId(), omsOrderDto.getLocationId())) {
            Assert.fail("商品信息与订单不一致");
        }
        CommonResult<com.portal.dto.WmsWarehouseDto> warehouseRes = executeWithRetry(
                () -> wmsServiceClient.getWarehouseById(goods.getWarehouseId()),
                "查询仓库信息失败，请稍后重试");
        String warehouseCity = warehouseRes == null || warehouseRes.getData() == null
                ? null
                : warehouseRes.getData().getCity();
        if (warehouseCity == null || warehouseCity.trim().isEmpty()) {
            Assert.fail("仓库城市信息缺失，暂无法创建订单");
        }
        omsOrderDto.setMerchantId(goods.getMerchantId());
        omsOrderDto.setWarehouseId(goods.getWarehouseId());
        omsOrderDto.setLocationId(goods.getLocationId());
        omsOrderDto.setSkuName(goods.getSkuName());
        omsOrderDto.setSkuCode((goods.getSkuCode() == null || goods.getSkuCode().isBlank()) ? "DEFAULT" : goods.getSkuCode());
        omsOrderDto.setCategory(goods.getCategory() == null ? (short) 0 : goods.getCategory());
        omsOrderDto.setType((goods.getType() == null || goods.getType().isBlank()) ? "normal" : goods.getType());
        omsOrderDto.setWarehouseCity(warehouseCity.trim());
        int buyQty = omsOrderDto.getQuantity();
        SeckillOrderMatch seckillMatch = findActiveSeckillMatch(goods.getGoodsId());
        BigDecimal unitPrice;
        boolean seckillReserved = false;
        boolean seckillRedisUserQtyReserved = false;
        Long seckillAgId = null;
        if (seckillMatch != null && seckillMatch.ag.getSeckillPrice() != null) {
            unitPrice = seckillMatch.ag.getSeckillPrice();
            seckillRedisUserQtyReserved = reserveSeckillForOrder(loginUserId, seckillMatch, buyQty);
            seckillReserved = true;
            seckillAgId = seckillMatch.ag.getId();
            omsOrderDto.setRemark(SeckillPortalConstants.ORDER_REMARK_SECKILL_PREFIX + seckillAgId);
        } else {
            unitPrice = goods.getPrice() == null ? BigDecimal.ZERO : goods.getPrice();
        }
        omsOrderDto.setPrice(unitPrice);
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(buyQty));
        omsOrderDto.setPrice(totalAmount);
        CommonResult<?> result = executeWithRetry(() -> omsServiceClient
                .addOrder(omsOrderDto), "创建订单失败，请稍后重试");
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode()) {
            if (seckillRedisUserQtyReserved && seckillAgId != null) {
                rollbackSeckillUserQuantityRedis(loginUserId, seckillAgId, buyQty);
            }
            if (seckillReserved && seckillAgId != null) {
                seckillActivityGoodsDao.increaseAvailableStock(seckillAgId, buyQty);
            }
            return result;
        }
        return result;
    }

    /**
     * 从购物车创建订单。
     * <p>
     * 该方法会复用 {@link #addOrder(OmsOrderDto)} 完成核心下单逻辑，
     * 并在成功后补充支付截止时间信息。
     *
     * @param request 购物车下单请求
     * @return 订单创建结果及支付截止时间
     */
    @Override
    public CommonResult<?> createOrderFromCart(CreateOrderFromCartRequest request) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        OmsCart cart = omsCartDao.selectOne(new LambdaQueryWrapper<OmsCart>()
                .eq(OmsCart::getId, request.getCartId())
                .eq(OmsCart::getUserId, loginUserId)
                .eq(OmsCart::getDeleted, (short) 0));
        if (cart == null)
            Assert.fail("购物车商品不存在");
        PortalGoods goods = portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, cart.getGoodsId())
                .eq(PortalGoods::getStatus, (short) 1));
        if (goods == null)
            Assert.fail("商品不存在或不可下单");
        if (goods.getWarehouseId() == null || goods.getLocationId() == null)
            Assert.fail("商品仓储信息不完整，暂无法下单");
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
        if (warehouseCity == null || warehouseCity.trim().isEmpty())
            Assert.fail("仓库城市信息缺失，暂无法创建订单");
        order.setWarehouseCity(warehouseCity.trim());
        order.setCategory(goods.getCategory() == null ? (short) 0 : goods.getCategory());
        order.setQuantity(cart.getQuantity() == null || cart.getQuantity() <= 0 ? 1 : cart.getQuantity());
        order.setType((goods.getType() == null || goods.getType().isBlank()) ? "normal" : goods.getType());
        CommonResult<?> result = addOrder(order);
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode())
            return result;
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

    /**
     * 直接购买创建订单（不经过购物车）。
     * <p>
     * 与购物车下单一致，核心下单流程同样委托到 {@link #addOrder(OmsOrderDto)}。
     *
     * @param request 直接购买请求
     * @return 订单创建结果及支付截止时间
     */
    @Override
    public CommonResult<?> createOrderDirect(CreateDirectOrderRequest request) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        PortalGoods goods = portalGoodsDao.selectOne(new LambdaQueryWrapper<PortalGoods>()
                .eq(PortalGoods::getGoodsId, request.getGoodsId().trim())
                .eq(PortalGoods::getStatus, (short) 1));
        if (goods == null)
            Assert.fail("商品不存在或不可下单");
        if (goods.getWarehouseId() == null || goods.getLocationId() == null)
            Assert.fail("商品仓储信息不完整，暂无法下单");
        int buyQty = request.getQuantity() == null ? 1 : request.getQuantity();
        if (buyQty <= 0)
            Assert.fail("购买数量必须大于0");
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

    /**
     * 用户支付订单。
     * <p>
     * 支付成功后会清理对应已购买购物车项，避免重复展示可购买状态。
     *
     * @param orderId 订单号
     * @return 支付结果
     */
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

    /**
     * 取消订单并回补秒杀资源。
     * <p>
     * 若订单为秒杀单，会按备注中的活动商品 ID 回补可用库存，
     * 同时回退用户秒杀计数，保证限购口径可恢复。
     *
     * @param orderId 订单号
     * @return 取消结果
     */
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
        restoreSeckillStockFromOrder(order);
        return omsServiceClient.cancelOrder(orderId);
    }

    /**
     * 查询当前用户订单分页列表。
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 订单分页结果
     */
    @Override
    public CommonResult<?> getOrder(int pageNum, int pageSize) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        return omsServiceClient.getOrder(loginUserId, pageNum, pageSize);
    }

    /**
     * 查询当前用户订单详情。
     *
     * @param orderId 订单号
     * @return 订单详情
     */
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

    /**
     * 查询订单支付截止时间，且校验订单归属为当前登录用户。
     *
     * @param orderId 订单号
     * @return 支付截止时间结果
     */
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

    /**
     * 更新当前登录用户资料。
     *
     * @param sysUserInfoDto 用户资料
     */
    @Override
    public void updateInfo(SysUserInfoDto sysUserInfoDto) {
        StpUtil.checkLogin();
        StpUtil.checkPermission("user");
        String loginUserId = StpUtil.getLoginIdAsString();
        sysUserInfoDto.setUserId(loginUserId);
        systemServiceClient.updateInfo(sysUserInfoDto);
    }

    /**
     * 统一远程调用重试模板。
     * <p>
     * 当远程调用抛出异常时按固定次数重试，重试仍失败则抛出业务异常。
     *
     * @param remoteCall  远程调用函数
     * @param failMessage 失败提示
     * @param <T>         返回数据类型
     * @return 远程调用结果
     */
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

    /**
     * 远程调用函数式接口，便于将不同的 Feign/HTTP 调用接入重试模板。
     *
     * @param <T> 返回数据类型
     */
    @FunctionalInterface
    private interface RemoteCall<T> {
        CommonResult<T> call();
    }

    /**
     * 解析订单单价。
     * <p>
     * 若命中有效秒杀活动，优先返回秒杀价；否则返回商品原价或 0。
     *
     * @param goodsId       商品 ID
     * @param fallbackPrice 普通售价
     * @return 订单单价
     */
    private BigDecimal resolveOrderUnitPrice(String goodsId, BigDecimal fallbackPrice) {
        SeckillOrderMatch m = findActiveSeckillMatch(goodsId);
        if (m != null && m.ag.getSeckillPrice() != null) {
            return m.ag.getSeckillPrice();
        }
        return fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice;
    }

    /**
     * 查找商品对应的当前可用秒杀活动与活动商品记录。
     * <p>
     * 仅返回处于活动窗口内、已审核通过且有秒杀价的匹配项。
     *
     * @param goodsId 商品 ID
     * @return 秒杀匹配结果，不存在时返回 null
     */
    private SeckillOrderMatch findActiveSeckillMatch(String goodsId) {
        if (goodsId == null || goodsId.isBlank()) {
            return null;
        }
        Date now = new Date();
        List<SeckillActivityGoods> approved = seckillActivityGoodsDao.selectList(
                new LambdaQueryWrapper<SeckillActivityGoods>()
                        .eq(SeckillActivityGoods::getGoodsId, goodsId)
                        .eq(SeckillActivityGoods::getStatus, (short) 3)
                        .orderByDesc(SeckillActivityGoods::getUpdateTime)
                        .last("limit 5"));
        if (approved.isEmpty()) {
            return null;
        }
        List<String> launchCodes = approved.stream()
                .map(SeckillActivityGoods::getLaunchActivityCode)
                .filter(code -> code != null && !code.isBlank())
                .distinct()
                .toList();
        if (launchCodes.isEmpty()) {
            return null;
        }
        List<SeckillActivity> activeLaunches = seckillActivityDao.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .in(SeckillActivity::getActivityCode, launchCodes)
                        .eq(SeckillActivity::getStatus, (short) 2)
                        .le(SeckillActivity::getStartTime, now)
                        .gt(SeckillActivity::getEndTime, now));
        if (activeLaunches.isEmpty()) {
            return null;
        }
        Map<String, SeckillActivity> launchByCode = activeLaunches.stream()
                .collect(Collectors.toMap(SeckillActivity::getActivityCode, a -> a, (a, b) -> a));
        Set<String> activeCodes = launchByCode.keySet();
        for (SeckillActivityGoods row : approved) {
            if (activeCodes.contains(row.getLaunchActivityCode()) && row.getSeckillPrice() != null) {
                SeckillActivity launch = launchByCode.get(row.getLaunchActivityCode());
                return new SeckillOrderMatch(row, launch);
            }
        }
        return null;
    }

    /**
     * 预占秒杀资源：先处理用户限购，再原子扣减可用库存。
     * <p>
     * 并发语义：
     * 1) 用户限购依赖 Redis Lua 保证校验与扣减原子性；
     * 2) 库存扣减依赖 SQL 条件更新（available_stock >= qty）避免超卖；
     * 3) 若库存扣减失败，会回滚已预占的用户限购计数。
     *
     * @param userId 用户 ID
     * @param m      秒杀匹配信息
     * @param buyQty 购买数量
     * @return 是否在 Redis 中扣减了用户秒杀件数计数（下单失败时需回滚）
     */
    private boolean reserveSeckillForOrder(String userId, SeckillOrderMatch m, int buyQty) {
        SeckillActivityGoods ag = m.ag;
        SeckillActivity launch = m.launch;
        if (launch == null || launch.getStartTime() == null || launch.getEndTime() == null) {
            Assert.fail("秒杀活动信息不完整");
        }
        if (ag.getAvailableStock() == null || ag.getAvailableStock() < buyQty) {
            Assert.fail("秒杀库存不足");
        }
        int limit = ag.getPerUserLimit() == null ? Integer.MAX_VALUE : ag.getPerUserLimit();
        if (limit < buyQty) {
            Assert.fail("超出单用户秒杀限购");
        }
        boolean touchedRedisUserQty = false;
        if (limit < Integer.MAX_VALUE) {
            CommonResult<Integer> sumRes = omsServiceClient.sumUserSeckillGoodsQuantity(
                    userId,
                    ag.getGoodsId(),
                    launch.getStartTime().getTime(),
                    launch.getEndTime().getTime());
            int omsBaseline = extractSumQuantity(sumRes);
            long ttlSec = seckillUserQtyKeyTtlSeconds(launch);
            long reserved = reserveSeckillUserQuantityInRedis(
                    userId, ag.getId(), buyQty, limit, omsBaseline, ttlSec);
            if (reserved < 0) {
                Assert.fail("超出单用户秒杀限购");
            }
            touchedRedisUserQty = true;
        }
        int rows = seckillActivityGoodsDao.decreaseAvailableStock(ag.getId(), buyQty);
        if (rows == 0) {
            if (touchedRedisUserQty) {
                rollbackSeckillUserQuantityRedis(userId, ag.getId(), buyQty);
            }
            Assert.fail("秒杀库存不足");
        }
        return touchedRedisUserQty;
    }

    /**
     * 计算用户秒杀计数 Redis Key 的 TTL。
     * <p>
     * 取活动结束时间再附加一天缓冲，减少活动边界时钟漂移带来的误差。
     *
     * @param launch 秒杀活动
     * @return TTL（秒）
     */
    private static long seckillUserQtyKeyTtlSeconds(SeckillActivity launch) {
        long endMs = launch.getEndTime().getTime();
        long sec = (endMs - System.currentTimeMillis()) / 1000 + 86400;
        return Math.max(60L, sec);
    }

    /**
     * 基于 Lua 脚本原子预占用户秒杀可购数量。
     *
     * @param userId      用户 ID
     * @param agId        活动商品 ID
     * @param buyQty      本次购买数量
     * @param limit       用户限购上限
     * @param omsBaseline OMS 侧历史已购基线
     * @param ttlSeconds  Redis Key 过期秒数
     * @return 预占后的累计值，失败返回负数
     */
    private long reserveSeckillUserQuantityInRedis(
            String userId, Long agId, int buyQty, int limit, int omsBaseline, long ttlSeconds) {
        if (agId == null) {
            return -1;
        }
        String key = RedisConstant.SEC_KILL_USER_QTY_PREFIX + agId + ":" + userId.trim();
        Long r = redisTemplate.execute(
                seckillUserQtyReserveScript,
                Collections.singletonList(key),
                String.valueOf(buyQty),
                String.valueOf(limit),
                String.valueOf(omsBaseline),
                String.valueOf(ttlSeconds));
        return r == null ? -1L : r;
    }

    /**
     * 回滚用户秒杀计数（仅回滚本次预占量）。
     *
     * @param userId 用户 ID
     * @param agId   活动商品 ID
     * @param qty    回滚数量
     */
    private void rollbackSeckillUserQuantityRedis(String userId, long agId, int qty) {
        if (qty <= 0 || userId == null || userId.isBlank()) {
            return;
        }
        String key = RedisConstant.SEC_KILL_USER_QTY_PREFIX + agId + ":" + userId.trim();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key, -qty);
        }
    }

    /**
     * 提取已购数量聚合结果，空值按 0 处理。
     *
     * @param res 远程聚合结果
     * @return 已购件数
     */
    private static int extractSumQuantity(CommonResult<Integer> res) {
        if (res == null || res.getData() == null) {
            return 0;
        }
        Number d = res.getData();
        return d.intValue();
    }

    /**
     * 根据订单信息回补秒杀资源。
     * <p>
     * 仅当备注中携带秒杀活动商品标识时执行回补，避免误回补普通单。
     *
     * @param order 订单详情
     */
    private void restoreSeckillStockFromOrder(OmsOrderDto order) {
        if (order == null) {
            return;
        }
        String remark = order.getRemark();
        if (remark == null || !remark.startsWith(SeckillPortalConstants.ORDER_REMARK_SECKILL_PREFIX)) {
            return;
        }
        String idPart = remark.substring(SeckillPortalConstants.ORDER_REMARK_SECKILL_PREFIX.length()).trim();
        if (idPart.isEmpty()) {
            return;
        }
        try {
            long agId = Long.parseLong(idPart);
            int qty = order.getQuantity() == null || order.getQuantity() <= 0 ? 0 : order.getQuantity();
            if (qty == 0) {
                return;
            }
            seckillActivityGoodsDao.increaseAvailableStock(agId, qty);
            String uid = order.getUserId();
            if (uid != null && !uid.isBlank()) {
                rollbackSeckillUserQuantityRedis(uid.trim(), agId, qty);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    /**
     * 秒杀匹配结果：活动商品记录 + 活动主记录。
     *
     * @param ag     活动商品
     * @param launch 秒杀活动
     */
    private record SeckillOrderMatch(SeckillActivityGoods ag, SeckillActivity launch) {
    }
}
