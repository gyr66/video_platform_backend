package com.gyr.minio.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HashMapper {
    int checkExists(String md5);

    void append(@Param("vid") String vid, @Param("md5") String md5);

    void remove(String id);
}
