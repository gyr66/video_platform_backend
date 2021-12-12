package com.gyr.minio.mapper;

import com.gyr.minio.bean.Condition;
import com.gyr.minio.bean.Video;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoMapper {
    void upload(Video video);

    void update(Video video);

    void delete(String id);

    List<Video> getByCondition(Condition condition);
}
