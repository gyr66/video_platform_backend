package com.gyr.minio.controller;

import com.github.pagehelper.PageInfo;
import com.gyr.minio.bean.Condition;
import com.gyr.minio.bean.Message;
import com.gyr.minio.bean.Video;
import com.gyr.minio.service.MinioService;
import com.gyr.minio.service.VideoService;
import com.gyr.minio.task.CleanTempTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@PreAuthorize("hasRole('admin')")
@RestController
@RequestMapping("/video")
public class VideoController {
    @Autowired
    VideoService videoService;

    @Autowired
    MinioService minioService;

    @RequestMapping("/updateThumbnail")
    public Message updateThumbnail(@RequestParam Map<String, String> map, @RequestParam("file") MultipartFile file) throws IOException {
        String id = map.get("id");
        minioService.updateThumbnailById(id, file.getInputStream());
        return Message.success("修改缩略图成功").put("id", id);
    }

    @PostMapping("/updateInfo")
    public Message updateInfo(@RequestBody Video video) {
        videoService.update(video);
        return Message.success("修改信息成功");
    }

    @DeleteMapping("/delete/{id}")
    public Message delete(@PathVariable("id") String id) {
        videoService.delete(id);
        minioService.deleteVideoById(id);
        return Message.success("删除成功");
    }

    @GetMapping("/getByCondition")
    public Message getByCondition(@RequestParam(value = "page", defaultValue = "1") int page, Condition condition) {
        PageInfo<Video> pageInfo = videoService.getByCondition(page, condition);
        List<Video> videoList = pageInfo.getList();
        for (Video video : videoList) {
            String videoId = video.getId();
            String videoName = video.getName();
            String suffix = videoName.substring(videoName.lastIndexOf("."));
            if (video.getEncrypt() != 0) suffix = ".m3u8";
            String name = videoId + suffix;
            video.setThumbnailUrl(minioService.getThumbnailUrlById(videoId));
            video.setVideoUrl(minioService.getVideoUrlByName(name));
        }
        return Message.success("查询成功!").put("pageInfo", pageInfo);
    }

}
