package com.gyr.minio.mapper;

import com.gyr.minio.bean.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {
    List<Tag> getAll();

    void addTag(Tag tag);

    void removeTag(int id);
}
