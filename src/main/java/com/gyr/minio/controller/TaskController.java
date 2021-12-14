package com.gyr.minio.controller;

import com.gyr.minio.bean.Message;
import com.gyr.minio.bean.MsgQuery;
import com.gyr.minio.bean.Task;
import com.gyr.minio.mapper.TaskMapper;
import com.gyr.minio.redis.TaskRedis;
import com.gyr.minio.service.VideoProcessingService;
import com.gyr.minio.utils.VideoFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@PreAuthorize("hasRole('admin')")
@RestController
public class TaskController {
    @Autowired
    VideoFetcher fetcher;

    @Autowired
    TaskRedis taskRedis;

    @Autowired
    VideoProcessingService videoProcessingService;

    @Autowired
    TaskMapper taskMapper;

    @GetMapping("/task/create")
    public Message createTask(String url) {
        Task task = fetcher.fetch(url);
        taskMapper.insert(task);
        return Message.success("创建任务成功").put("task", task);
    }

    @GetMapping("/task/getAll")
    public Message getTasks() {
        List<Task> taskList = taskMapper.getAll();
        return Message.success("查询成功").put("tasks", taskList);
    }

    @PostMapping("/task/getMsg")
    public Message getMsg(@RequestBody MsgQuery msgQuery) {
        List<String> msgList = new ArrayList<>();
        String[] taskIds = msgQuery.getTaskIds();
        for (String taskId : taskIds) {
            String message = taskRedis.getMessage(taskId);
            msgList.add(message);
        }
        return Message.success("查询成功").put("msgList", msgList);
    }
}
