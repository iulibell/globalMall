package com.common.constant;

public class RabbitConstant {
    /**
     * 商品上架申请订单延时队列名称
     */
    public static final String GOODS_TTL_QUEUE = "goods_ttl_queue";
    public static final String GOODS_TTL_EXCHANGE = "goods_ttl_exchange";
    public static final String GOODS_TTL_ROUTING_KEY = "goods_ttl_routing_key";
    /**
     * 商品上架申请订单死信队列名称
     */
    public static final String GOODS_DEAD_QUEUE = "goods_dead_queue";
    public static final String GOODS_DEAD_EXCHANGE = "goods_dead_exchange";
    public static final String GOODS_DEAD_ROUTING_KEY = "goods_dead_routing_key";
    /**
     * 商品上架申请订单队列过期时间
     */
    public static final int GOODS_TTL_EXPIRE = 1000 * 60 * 361;
}
