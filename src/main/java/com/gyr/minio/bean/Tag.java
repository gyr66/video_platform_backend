package com.gyr.minio.bean;

import lombok.Data;

@Data
public class Tag {
    String name;
    int id;

    public Tag() {
    }

    ;

    public Tag(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
