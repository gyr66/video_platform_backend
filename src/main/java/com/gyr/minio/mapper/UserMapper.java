package com.gyr.minio.mapper;

import com.gyr.minio.bean.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findUserByUsername(String username);
}
