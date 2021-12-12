package com.gyr.minio.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class HashRedis {
    @Autowired
    StringRedisTemplate redisTemplate;

    final String KEY = "hash";

    public void insert(String hash) {
        redisTemplate.opsForSet().add(KEY, hash);
    }

    public Boolean checkExists(String hash) {
        return redisTemplate.opsForSet().isMember(KEY, hash);
    }

    public void remove(String hash) {
        redisTemplate.opsForSet().remove(KEY, hash);
    }

}
