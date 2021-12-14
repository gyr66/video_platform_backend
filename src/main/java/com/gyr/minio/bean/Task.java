package com.gyr.minio.bean;

import lombok.Data;

import java.util.Date;

@Data
public class Task {
    String id; // 任务ID
    Date createTime; // 创建时间
    String url; // 远程URL
    String status; // 任务状态（正在执行、成功执行、遭遇异常、用户中断）
}
