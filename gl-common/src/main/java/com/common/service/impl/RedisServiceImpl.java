package com.common.service.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.common.service.RedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
@SuppressWarnings("null")
public class RedisServiceImpl implements RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /*===== 字符串类型 ===== */

    @Override
    public void set(String key, Object value, long expireTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Long delete(List<String> keys) {
        return redisTemplate.delete(keys);
    }

    @Override
    public boolean expire(String key, long expireTime) {
        return redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
    }

    @Override
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void incr(String key, long delta) {
        redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public void decr(String key, long delta) {
        redisTemplate.opsForValue().decrement(key, delta);
    }

    /*===== 列表类型 ===== */

    @Override
    public void setList(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    @Override
    public List<Object> getList(String key) {
        return (List<Object>) redisTemplate.opsForList().range(key, 0, -1);
    }

    /*===== 哈希类型 ===== */

    @Override
    public void setHash(String key, String field, Object value, long expireTime) {
        redisTemplate.opsForHash().put(key, field, value);
        expire(key, expireTime);
    }

    @Override
    public Object getHash(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    @Override
    public void deleteHash(String key, Object... hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    @Override
    public boolean hasKeyInHash(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Long incrHash(String key, String hashKey, Long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    @Override
    public Long decrHash(String key, String hashKey, Long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, -delta);
    }

    /*===== 有序集合类型 ===== */

    @Override
    public void setSortedSet(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    @Override
    public List<Object> getSortedSet(String key) {
        Set<Object> set = (Set<Object>) redisTemplate.opsForZSet().range(key, 0, -1);
        return set.stream().toList();
    }
}
