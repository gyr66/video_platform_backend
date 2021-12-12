package com.gyr.minio.utils;

import com.gyr.minio.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class MinioUtil {
    @Autowired
    MinioConfig minioConfig;

    @Autowired
    MinioClient minioClient;

    public List<String> listObjects(String path) {
        List<String> list = new ArrayList<>();
        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .prefix(path)
                    .build();
            Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);
            for (Result<Item> result : results) {
                Item item = result.get();
                list.add(item.objectName());
            }
        } catch (Exception e) {
            log.error("错误：" + e.getMessage());
        }
        return list;
    }

    public void deleteObject(String objectName) {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("错误：" + e.getMessage());
        }
    }

    public void putObject(String prefix, InputStream is, String fileName, String contentType) {
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(prefix + "/" + fileName)
                    .contentType(contentType)
                    .stream(is, is.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);
            is.close();
        } catch (Exception e) {
            log.error("错误：" + e.getMessage());
        }
    }

    public void uploadObject(String prefix, File file, String contentType) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(prefix + "/" + file.getName())
                            .filename(file.getAbsolutePath())
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            log.error("错误：" + e.getMessage());
        }
    }

    public String getObjectUrl(String objectName) {
        try {
            GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(1, TimeUnit.DAYS)
                    .build();
            return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("错误：" + e.getMessage());
        }
        return "";
    }

    //下载minio服务的文件
    public InputStream getObject(String objectName) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build();
            return minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.error("错误：" + e.getMessage());
        }
        return null;
    }
}