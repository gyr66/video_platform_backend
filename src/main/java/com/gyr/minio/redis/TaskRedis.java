package com.gyr.minio.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 在内存中维护正在执行的任务的信息
 */
@Component
public class TaskRedis {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    final String KEY = "task";

    // 更新taskId的执行信息
    public void refreshTask(String taskId, String message) {
        stringRedisTemplate.opsForHash().put(KEY, taskId, message);
    }

    // 移除taskId的执行信息，意味着此任务正常结束或者遭遇异常或者被用户中断
    public void removeTask(String taskId) {
        stringRedisTemplate.opsForHash().delete(KEY, taskId);
    }

    // 返回指定taskId的执行信息
    public String getMessage(String taskId) {
        return (String) stringRedisTemplate.opsForHash().get(KEY, taskId);
    }

}
