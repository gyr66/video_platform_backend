package com.gyr.minio.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SecurityController {
    @RequestMapping("/session/invalid")
    public Map<String, Object> sessionInvalid() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 401);
        map.put("msg", "登录失效，请重新登录");
        return map;
    }
}
