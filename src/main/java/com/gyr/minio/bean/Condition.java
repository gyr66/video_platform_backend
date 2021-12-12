package com.gyr.minio.bean;

import lombok.Data;

@Data
public class Condition {
    String name;
    int[] tags;
    String orderProp;
    String order;
}
