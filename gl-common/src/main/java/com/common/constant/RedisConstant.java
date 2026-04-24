package com.common.constant;

public class RedisConstant {
    public static final String REGIS_KEY_PREFIX = "register:";
    public static final int REGIS_EXPIRE_TIME = 24;

    /** 注册短信/验证码：手机号 -> 验证码，5 分钟有效 */
    public static final String REGIS_CAPTCHA_PREFIX = "register:captcha:";
    public static final int REGIS_CAPTCHA_EXPIRE_MINUTES = 5;

    /** 同一手机号发送验证码最小间隔（秒） */
    public static final String REGIS_CAPTCHA_SEND_PREFIX = "register:captcha:send:";
    public static final int REGIS_CAPTCHA_SEND_INTERVAL_SECONDS = 60;

    /** 商家每隔十二个小时才能申请上架商品 */
    public  static final String GOODS_APPLY_PREFIX = "goods:apply:";
    public static final  int GOODS_APPLY_EXPIRE = 12;

    /** 商家为申请审核成功的商品付款(上架费用、运费)*/
    public static final String GOODS_APPLY_PAY_PREFIX = "goods:apply:pay:";
    public static final int GOODS_APPLY_PAY_EXPIRE = 6;

    /** 商品下架申请支付超时标记 */
    public static final String OFF_SHELF_PAY_PREFIX = "portal:offShelf:pay:";
    public static final int OFF_SHELF_PAY_EXPIRE = 6;

    /** 热门商品(浏览超过1w)初始化*/
    public static final String HOT_GOODS_PREFIX = "hot:goods:";
    public static final int HOT_GOODS_EXPIRE = 12;

    /** 统计商品浏览次数*/
    public static final String VISIT_COUNT_PREFIX = "visit:count:";
    public static final String VISIT_COUNT_LOCK = "visit:count:lock";

    /** 浏览锁的重试时间*/
    public static final int COUNT_LOCK_WAIT_TIME = 5;
    public static final int COUNT_LOCK_LEASE_TIME = 10;
}
