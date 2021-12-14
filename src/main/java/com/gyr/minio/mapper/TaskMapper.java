package com.gyr.minio.mapper;

import com.gyr.minio.bean.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    // 添加一个任务
    void insert(Task task);

    // 获取所有任务
    List<Task> getAll();

    void updateStatus(@Param("taskId") String taskId, @Param("status") String status);
}
