package com.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 秒杀相关 Redis Lua 脚本 Bean，避免在 Service 中定义静态脚本对象。
 */
@Configuration
public class SeckillRedisScriptConfig {

    @Bean("seckillUserQtyReserveScript")
    public DefaultRedisScript<Long> seckillUserQtyReserveScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        String lua = """
                local add = tonumber(ARGV[1])
                local limit = tonumber(ARGV[2])
                local base = tonumber(ARGV[3])
                local ttl = tonumber(ARGV[4])
                local cur = redis.call('GET', KEYS[1])
                if cur == false then
                  cur = base
                  redis.call('SET', KEYS[1], tostring(cur))
                else
                  cur = tonumber(cur)
                  if cur < base then
                    cur = base
                    redis.call('SET', KEYS[1], tostring(cur))
                  end
                end
                if cur + add > limit then
                  return -1
                end
                local n = redis.call('INCRBY', KEYS[1], add)
                if ttl > 0 then
                  redis.call('EXPIRE', KEYS[1], ttl)
                end
                return n
                """;
        script.setScriptText(lua);
        script.setResultType(Long.class);
        return script;
    }
}
