package com.gyr.minio.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class VideoFetcher {
    @Value("${downloadtoolpath}")
    private String downLoadToolPath;

    @Value("${uploadpath}")
    private String workingPath;

    public Map<String, Object> fetch(String url) {
        Map<String, Object> result = new HashMap<>();
        File downLoadTool = new File(downLoadToolPath);
        List<String> commands = new ArrayList<>();
        commands.add(downLoadTool.getAbsolutePath());
        commands.add(url);
        commands.add("-o");
        String uuid = UUID.randomUUID().toString();
        commands.add("\"" + workingPath + "\\" + uuid + "\\%(title)s.%(ext)s\"");
        result.put("path", workingPath + "\\" + uuid);
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commands);
            result.put("process", builder.start());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
