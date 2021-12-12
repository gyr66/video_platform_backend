package com.gyr.minio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class HLSUtil {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${uploadpath}")
    private String workingPath;
    @Value("${ffmpegtoolpath}")
    private String toolPath;

    public List<File> section(String id, String fileName) {
        final File workingDir = new File(workingPath);
        File targetDir = Objects.requireNonNull(workingDir.listFiles((dir, name) -> name.equals(id)))[0];
        File encrypt = Objects.requireNonNull(workingDir.listFiles((dir, name) -> name.equals("encrypt.keyinfo")))[0];
        File data = Objects.requireNonNull(targetDir.listFiles((dir, name) -> name.equals(fileName)))[0];
        File index = new File(targetDir.getAbsolutePath() + "\\" + id + ".m3u8");
        String prefix = targetDir.getAbsolutePath() + "\\" + id;
        List<String> commands = new ArrayList<>();
        commands.add(toolPath);
        commands.add("-y");
        commands.add("-i");
        commands.add(data.getAbsolutePath());
        commands.add("-hls_time");
        commands.add("300");
        commands.add("-hls_key_info_file");
        commands.add(encrypt.getAbsolutePath());
        commands.add("-hls_playlist_type");
        commands.add("vod");
        commands.add("-hls_segment_filename");
        commands.add("\"" + prefix + "_%4d.ts" + "\"");
        commands.add(index.getAbsolutePath());
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commands);
            Process process = builder.start();
            InputStream errorStream = process.getErrorStream();
            int temp;
            while ((temp = errorStream.read()) != -1) System.out.print((char) temp);
            process.waitFor();
            int result = process.exitValue();
            logger.info(result == 0 ? "视频分片成功" : "视频分片失败");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList(Objects.requireNonNull(targetDir.listFiles((dir, name) -> {
            if (name.lastIndexOf('.') == -1) return false;
            String suffix = name.substring(name.lastIndexOf(".") + 1);
            return suffix.equals("ts") || suffix.equals("m3u8");
        })));
    }
}
