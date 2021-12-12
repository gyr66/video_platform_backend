package com.gyr.minio;

import com.gyr.minio.bean.Condition;
import com.gyr.minio.bean.User;
import com.gyr.minio.bean.Video;
import com.gyr.minio.mapper.TagMapper;
import com.gyr.minio.mapper.UserMapper;
import com.gyr.minio.mapper.VideoMapper;
import com.gyr.minio.service.MinioService;
import com.gyr.minio.service.VideoService;
import com.gyr.minio.task.CleanTempTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MinioApplicationTests {
    @Autowired
    MinioService service;
    @Autowired
    VideoService videoService;
    @Autowired
    TagMapper tagMapper;
    @Autowired
    VideoMapper videoMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    CleanTempTask cleanTempTask;

    @Test
    void contextLoads() {

    }

    @Test
    void videoSearchTest() {
        Condition condition = new Condition();
        condition.setName("te");
        int[] tags = {1, 2};
        condition.setTags(tags);
        condition.setOrder("asc");
        condition.setOrderProp("uploadTime");
        condition.setName("");
        List<Video> videoList = videoMapper.getByCondition(condition);
        System.out.println(videoList.size());
        for (Video video : videoList) {
            System.out.println(video);
        }
    }

    @Test
    void userMapperTest() {
        User gyr = userMapper.findUserByUsername("gyr");
        System.out.println(gyr);
    }

    @Test
    void cleanTaskTest() {
        cleanTempTask.execute("1");
    }


}
