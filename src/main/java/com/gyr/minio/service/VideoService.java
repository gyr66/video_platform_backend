package com.gyr.minio.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gyr.minio.bean.Condition;
import com.gyr.minio.bean.Video;
import com.gyr.minio.mapper.HashMapper;
import com.gyr.minio.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VideoService {
    @Autowired
    VideoMapper videoMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    HashMapper hashMapper;

    // 上传视频，默认都是未分类(1)
    @Transactional
    public void upload(Video video, String hash) {
        videoMapper.upload(video);
        jdbcTemplate.update("INSERT INTO video_tag VALUES (?,?)", video.getId(), 1);
        hashMapper.append(video.getId(), hash);
    }

    // 更新视频名称和标签
    @Transactional
    public void update(Video video) {
        videoMapper.update(video);
        // 首先删除所有之前与此视频相关的标签信息
        jdbcTemplate.update("DELETE FROM video_tag WHERE vid = ?", video.getId());
        // 插入新的标签信息
        List<Integer> tags = video.getTags();
        for (int tag : tags) {
            jdbcTemplate.update("INSERT INTO video_tag VALUES (?, ?)", video.getId(), tag);
        }
    }

    // 删除视频，由于设置了删除级联，当删除视频时，video_tag表中的与此视频相关的记录自动删除
    public void delete(String id) {
        videoMapper.delete(id);
    }

    public PageInfo<Video> getByCondition(int page, Condition condition) {
        PageHelper.startPage(page, 5);
        return new PageInfo<>(videoMapper.getByCondition(condition));
    }
}
