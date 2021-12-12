package com.gyr.minio.service;

import com.gyr.minio.bean.Video;
import com.gyr.minio.task.CleanTempTask;
import com.gyr.minio.utils.HLSUtil;
import com.gyr.minio.utils.MD5Util;
import com.gyr.minio.utils.MinioUtil;
import com.gyr.minio.utils.ThumbnailGenerateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 视频处理
 * 提供生成视频缩略图，计算视频MD5码并入库，分片加密视频（可选）
 * 最终上传视频到服务器
 */
@Service
public class VideoProcessingService {

    @Autowired
    ThumbnailGenerateUtil thumbnailGenerateUtil;

    @Autowired
    MinioUtil minioUtil;

    @Autowired
    HLSUtil hlsUtil;

    @Autowired
    VideoService videoService;

    @Autowired
    CleanTempTask cleaner;

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param path 上传视频的外层目录
     * @param encrypt 视频是否加密
     * @param uploader 视频上传者
     * URL获得: 视频名称，上传者，视频格式
     */
    public void process(String path, String uploader, int encrypt) throws IOException {
        String id = path.substring(path.lastIndexOf('\\') + 1);
        File dir = new File((path)); // 上传的目录
        File target = Objects.requireNonNull(dir.listFiles())[0]; // 目标文件
        // 获取视频大小
        double videoSize = (double)target.length() / 1024.0 / 1014.0;
        // 获取视频MD5
        String hash = MD5Util.computeMD5(target);
        // 得到视频名称和后缀
        String originalFileName = target.getName();
        String originalSuffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        Date now = new Date(); // 上传时间
        // 生成缩略图并上传服务器
        File thumbnail = thumbnailGenerateUtil.getThumbnail(id, originalFileName);
        FileInputStream is = new FileInputStream(target);
        minioUtil.uploadObject(id, thumbnail, "image/jpeg");
        // 上传视频文件到服务器
        if (encrypt != 0) { // 分片加密
            List<File> fileList = hlsUtil.section(id, originalFileName);
            for (File file : fileList) {
                String fileName = file.getName();
                String suffix = fileName.substring(fileName.lastIndexOf('.') + 1);
                String contentType;
                if (suffix.equals("m3u8")) contentType = "audio/x-mpegurl";
                else contentType = "application/x-linguist";
                minioUtil.uploadObject(id, file, contentType);
            }
        } else { // 直接上传
            minioUtil.putObject(id, is, id + originalSuffix, "video/" + typeConvert(originalSuffix));
        }
        // 同步视频信息到数据库
        videoService.upload(new Video(id, originalFileName, uploader, now, encrypt, videoSize), hash);
        cleaner.execute(id);
    }

    private String typeConvert(String suffix) {
        switch (suffix) {
            case ".wmv":
                return "x-ms-wmv";
            case ".mp4":
                return "mpeg4";
            case ".avi":
                return "avi";
            case ".wma":
                return "wma";
            case ".w4a":
            case ".w4v":
                return "mp4";
            case ".wov":
                return "quicktime";
            case ".3gp":
                return "3gpp";
            case ".webm":
                return "webm";
            case ".flv":
                return "x-flv";
            case ".mpeg":
                return "mpg";
            case ".mts":
                return "vnd.dlna.mpeg-tts";
            case ".vob":
                return "vob";
            case ".mkv":
                return "x-matroska";
            default:
                return "";
        }
    }
}
