package com.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.common.service.RedisService;
import com.system.entity.SysUser;
import com.system.service.CacheService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录用户缓存实现；值类型保持 {@link SysUser} 以便与现有 Redis 序列化及商城快照 {@code mallToLogiCacheSnapshot} 兼容。
 */
@Service
public class CacheServiceImpl implements CacheService {

    @Resource
    private RedisService redisService;
    @Resource
    private ObjectMapper objectMapper;
    @Value("${spring.data.redis.key}")
    private String keyPrefix;
    @Value("${spring.data.redis.expire}")
    private int expire;

    @Override
    public void setLoginStatus(String userId, SysUser sysUser, TimeUnit timeUnit) {
        redisService.set(keyPrefix + userId, sysUser, expire, timeUnit);
    }

    @Override
    public SysUser getById(long userId) {
        String cacheKey = keyPrefix + userId;
        Object raw = redisService.get(cacheKey);
        if (raw == null) {
            return null;
        }
        if (raw instanceof SysUser) {
            return (SysUser) raw;
        }
        try {
            return objectMapper.convertValue(raw, SysUser.class);
        } catch (IllegalArgumentException e) {
            redisService.delete(cacheKey);
            return null;
        }
    }

    @Override
    public void removeLoginStatus(long userId) {
        redisService.delete(keyPrefix + userId);
    }
}
