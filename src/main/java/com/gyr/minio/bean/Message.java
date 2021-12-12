package com.gyr.minio.bean;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Message {
    int code;
    String msg;
    Map<String, Object> content = new HashMap<>();

    public static Message success(String msg) {
        Message message = new Message();
        message.setCode(200);
        message.setMsg(msg);
        return message;
    }

    public static Message fail(String msg) {
        Message message = new Message();
        message.setCode(300);
        message.setMsg(msg);
        return message;
    }

    public Message put(String key, Object value) {
        content.put(key, value);
        return this;
    }
}