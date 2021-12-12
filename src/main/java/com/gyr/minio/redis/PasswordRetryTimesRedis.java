package com.gyr.minio.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PasswordRetryTimesRedis {
    @Autowired
    StringRedisTemplate redisTemplate;

    public void addRetryTimes(String userName) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(userName)))
            redisTemplate.expire(userName, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().increment(userName);
    }

    public int getRetryTimes(String userName) {
        String times = redisTemplate.opsForValue().get(userName);
        if (times == null) return 0;
        return Integer.parseInt(times);
    }

    public void removeKey(String userName) {
        redisTemplate.delete(userName);
    }
}
