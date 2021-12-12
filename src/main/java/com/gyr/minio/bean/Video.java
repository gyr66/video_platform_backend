package com.gyr.minio.bean;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Video {
    Date uploadTime;
    private String id;
    private String name;
    private List<Integer> tags;
    private String uploader;
    private String videoUrl;
    private String thumbnailUrl;
    private int encrypt;
    private double size; // 以MB为单位

    public Video() {
    }

    public Video(String id, String name, String uploader, Date date, int encrypt, double size) {
        this.id = id;
        this.name = name;
        this.uploader = uploader;
        this.uploadTime = date;
        this.encrypt = encrypt;
        this.size = size;
    }
}
