package com.gyr.minio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ConvertUtil {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${uploadpath}")
    private String workingPath;
    @Value("${ffmpegtoolpath}")
    private String toolPath;

    public void convert(String id, String fileName) {
        // 工作目录
        final File workingDir = new File(workingPath);
        // 上传文件目录
        File targetDir = Objects.requireNonNull(workingDir.listFiles((dir, name) -> name.equals(id)))[0];
        // 上传文件
        File data = Objects.requireNonNull(targetDir.listFiles((dir, name) -> name.equals(fileName)))[0];
        // 目标文件
        File target = new File(targetDir.getAbsolutePath() + "//" + fileName + ".mp4");

        List<String> commands = new ArrayList<>();
        commands.add(toolPath);
        commands.add("-y"); // Overwrite output files without asking.
        commands.add("-i");
        commands.add(data.getAbsolutePath());
        commands.add(target.getAbsolutePath());

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commands);
            Process process = builder.start();
            new Thread(() -> {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(() -> {
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                try {
                    while ((line = err.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        err.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            process.waitFor();
            int result = process.exitValue();
            logger.info(result == 0 ? "视频格式转换成功" : "视频格式转换失败");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
