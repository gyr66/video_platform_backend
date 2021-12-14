package com.gyr.minio.utils;

import com.gyr.minio.bean.Task;
import com.gyr.minio.mapper.TaskMapper;
import com.gyr.minio.redis.TaskRedis;
import com.gyr.minio.service.VideoProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VideoFetcher {
    @Value("${downloadtoolpath}")
    private String downLoadToolPath;

    @Value("${uploadpath}")
    private String workingPath;

    @Autowired
    TaskRedis taskRedis;

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    VideoProcessingService videoProcessingService;

    Logger logger = LoggerFactory.getLogger(getClass());

    public Task fetch(String url) {
        Task task = new Task();
        String taskId = UUID.randomUUID().toString();
        task.setId(taskId);
        task.setCreateTime(new Date());
        task.setStatus("正在执行");
        task.setUrl(url);
        File downLoadTool = new File(downLoadToolPath);
        List<String> commands = new ArrayList<>();
        commands.add(downLoadTool.getAbsolutePath());
        commands.add(url);
        commands.add("-o");
        String uuid = UUID.randomUUID().toString();
        commands.add("\"" + workingPath + "\\" + uuid + "\\%(title)s.%(ext)s\"");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commands);
        new Thread(() -> {
            try {
                Process process = builder.start();
                new Thread(() -> {
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    try {
                        while ((line = in.readLine()) != null) {
                            taskRedis.refreshTask(taskId, line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                new Thread(() -> {
                    BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    try {
                        while ((line = err.readLine()) != null) {
                            taskRedis.refreshTask(taskId, line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            err.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                process.waitFor();
                int res = process.exitValue();
                if (res == 0) {
                    logger.info("任务" + taskId + ": 顺利完成了!");
                    taskRedis.removeTask(taskId);
                    taskMapper.updateStatus(taskId, "已完成");
                    videoProcessingService.process(workingPath + "\\" + uuid, "网络上传", 0);
                }
                else {
                    logger.warn("任务" + taskId + ": 执行失败!");
                    taskRedis.removeTask(taskId);
                    taskMapper.updateStatus(taskId, "执行失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return task;
    }

}
