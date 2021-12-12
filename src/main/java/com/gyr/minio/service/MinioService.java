package com.gyr.minio.service;

import com.gyr.minio.utils.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class MinioService {
    @Autowired
    MinioUtil util;

    public String getThumbnailUrlById(String id) {
        return util.getObjectUrl(id + "/" + id + ".jpg");
    }

    public String getVideoUrlByName(String name) {
        String id = name.substring(0, name.lastIndexOf("."));
        return util.getObjectUrl(id + "/" + name);
    }

    public void deleteVideoById(String id) {
        List<String> list = util.listObjects(id + "/");
        list.forEach(item -> util.deleteObject(item));
    }

    public void updateThumbnailById(String id, InputStream in) {
        util.putObject(id, in, id + ".jpg", "image/jpg");
    }
}
