package com.gyr.minio.controller;

import com.gyr.minio.bean.Message;
import com.gyr.minio.bean.Video;
import com.gyr.minio.mapper.HashMapper;
import com.gyr.minio.redis.HashRedis;
import com.gyr.minio.service.MinioService;
import com.gyr.minio.service.VideoService;
import com.gyr.minio.task.CleanTempTask;
import com.gyr.minio.utils.HLSUtil;
import com.gyr.minio.utils.MinioUtil;
import com.gyr.minio.utils.ThumbnailGenerateUtil;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
public class UploadController {
    @Autowired
    MinioUtil minioUtil;

    @Autowired
    MinioService minioService;

    @Autowired
    TusFileUploadService tusFileUploadService;

    @Autowired
    VideoService videoService;

    @Autowired
    ThumbnailGenerateUtil thumbnailGenerateUtil;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CleanTempTask cleaner;

    @Autowired
    HLSUtil hlsUtil;

    @Autowired
    HashMapper hashMapper;

    @Autowired
    HashRedis hashRedis;

    @Value("${uploadpath}")
    private String workingPath;

    Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = {"/upload", "/upload/**"}, method = {RequestMethod.POST, RequestMethod.PATCH, RequestMethod.HEAD,
            RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.GET})
    public void processUpload(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws IOException {
        tusFileUploadService.process(servletRequest, servletResponse);
        String uploadURI = servletRequest.getRequestURI();
        UploadInfo uploadInfo = null;
        try {
            uploadInfo = tusFileUploadService.getUploadInfo(uploadURI);
        } catch (IOException | TusException e) {
            logger.error("get upload info", e);
        }
        final UploadInfo info = uploadInfo;
        if (info != null && !info.isUploadInProgress()) {
            new Thread(() -> {
                // TODO 将一下步骤统一到VideoProcessingService中
                try (InputStream is = tusFileUploadService.getUploadedBytes(uploadURI)) {
                    String originalFileName = info.getMetadata().get("fileName"); // 视频名称
                    String originalSuffix = originalFileName.substring(originalFileName.lastIndexOf(".")); // 文件后缀
                    String id = info.getId().toString(); // 上传id，作为视频id
                    String uploader = info.getMetadata().get("username"); // 上传者
                    String section = info.getMetadata().get("section"); // 是否分片
                    String hash = info.getMetadata().get("hash");
                    Date now = new Date(); // 上传时间
                    if (hash != null)
                        hashRedis.insert(hash);
                    File thumbnail = thumbnailGenerateUtil.getThumbnail(id, "data");
                    minioUtil.uploadObject(id, thumbnail, "image/jpeg");
                    if (section.equals("true")) {
                        List<File> fileList = hlsUtil.section(id, "data");
                        for (File file : fileList) {
                            String fileName = file.getName();
                            String suffix = fileName.substring(fileName.lastIndexOf('.') + 1);
                            String contentType;
                            if (suffix.equals("m3u8")) contentType = "audio/x-mpegurl";
                            else contentType = "application/x-linguist";
                            minioUtil.uploadObject(id, file, contentType);
                        }
                    } else {
                        minioUtil.putObject(id, is, id + originalSuffix, info.getFileMimeType());
                    }
                    int encrypt = section.equals("true") ? 1 : 0;
                    File workingDir = new File(workingPath);
                    File targetDir = Objects.requireNonNull(workingDir.listFiles(((dir, name) -> name.equals(id))))[0];
                    File target = Objects.requireNonNull(targetDir.listFiles(((dir, name) -> name.equals("data"))))[0];
                    double videoSize = (double) target.length() / 1024.0 / 1014.0;
                    videoService.upload(new Video(id, originalFileName, uploader, now, encrypt, videoSize), hash);
                    if (hash != null)
                        hashRedis.remove(hash);
                    cleaner.execute(id);
                } catch (IOException | TusException e) {
                    logger.error("get uploaded error", e);
                }
            }).start();
        }
    }

    @GetMapping("/checkExists")
    public Message checkExists(String hash) {
        boolean res;
        int exist = hashMapper.checkExists(hash);
        res = exist != 0;
        res |= hashRedis.checkExists(hash);
        return Message.success("查询成功").put("exist", res).put("hash", hash);
    }
}
