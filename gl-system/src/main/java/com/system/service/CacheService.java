package com.system.service;

import com.system.entity.SysUser;

import java.util.concurrent.TimeUnit;

public interface CacheService {
    /**
     * 写入登录快照（供踢线、扩展读取等；key 为配置的 redis.key + userId）
     */
    void setLoginStatus(String userId, SysUser sysUser, TimeUnit timeUnit);

    /**
     * 按登录主键读取快照（与 Sa-Token loginId 一致的长整型）
     */
    SysUser getById(long userId);
    /** 移除登录快照（改密、禁用等场景与 token 一并失效） */
    void removeLoginStatus(long userId);
}
