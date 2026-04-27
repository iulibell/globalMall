package com.common.constant;

/**
 * 门户秒杀与订单落库约定（非 Spring Bean，仅常量）。
 */
public final class SeckillPortalConstants {
    /** 写入 OMS 订单 remark，用于取消时回补秒杀活动库存与 Redis 用户计数 */
    public static final String ORDER_REMARK_SECKILL_PREFIX = "SECKILL_AG:";
}
