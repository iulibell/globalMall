package com.common.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface RedisService {

    /*===== 字符串类型 ===== */
    /**
     * 设置键值对(字符串)
     * @param key 键
     * @param value 值
     * @param expireTime 过期时间
     */
    void set(String key, Object value, long expireTime, TimeUnit timeUnit);

    /**
     * 获取键值对(字符串)
     * @param key 键
     * @return 值
     */
    Object get(String key);

    /**
     * 删除键值对
     * @param key 键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 批量删除键值对
     * @param keys 键列表
     * @return 是否删除成功
     */
    Long delete(List<String> keys);

    /**
     * 设置键值对过期时间
     * @param key 键
     * @param expireTime 过期时间
     * @return 是否设置成功
     */
    boolean expire(String key, long expireTime);

    /**
     * 判断键值对是否存在
     * @param key 键
     * @return 是否存在
     */
    boolean hasKey(String key);

    /**
     * 增加键值对(字符串)
     * @param key 键
     * @param delta 增加值
     */
    void incr(String key, long delta);

    /**
     * 减少键值对(字符串)
     * @param key 键
     * @param delta 减少值
     */
    void decr(String key, long delta);

    /*===== 列表类型 ===== */
    /**
     * 设置列表(字符串)
     * @param key 键
     * @param value 值
     */
    void setList(String key, Object value);

    /**
     * 获取列表(字符串)
     * @param key 键
     * @return 列表
     */
    List<Object> getList(String key);

    /*===== 哈希类型 ===== */
    /**
     * 设置哈希(字符串)
     * @param key 键
     * @param field 字段
     * @param value 值
     */
    void setHash(String key, String field, Object value, long expireTime);

    /**
     * 获取哈希(字符串)
     * @param key 键
     * @param field 字段
     * @return 值
     */
    Object getHash(String key, String field);

    /**
     * 删除Hash结构中的属性
     * @param key
     * @param hashKey
     */
    void deleteHash(String key, Object... hashKey);

    /**
     * Hash结构中属性递增
     * @param key
     * @param hashKey
     * @param delta
     * @return 递增后的值
     */
    Long incrHash(String key, String hashKey, Long delta);

    /**
     * Hash结构中属性递减
     * @param key
     * @param hashKey
     * @param delta
     * @return 递减后的值
     */
    Long decrHash(String key, String hashKey, Long delta);

    /**
     * 判断Hash结构中是否有该属性
     * @param key
     * @param hashKey
     * @return 是否存在
     */
    boolean hasKeyInHash(String key, String hashKey);

    /*===== 有序集合类型 ===== */
    /**
     * 设置有序集合
     * @param key 键
     * @param value 值(自定义参数)
     */
    void setSortedSet(String key, Object value, double score);

    /**
     * 获取有序集合
     * @param key 键
     * @return 有序集合(自定义参数)
     */
    List<Object> getSortedSet(String key);
}