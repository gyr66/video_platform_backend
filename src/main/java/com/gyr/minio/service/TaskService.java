package com.gyr.minio.service;

import com.gyr.minio.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    @Autowired
    TaskMapper taskMapper;



}
