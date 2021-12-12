package com.gyr.minio.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CleanTempTask {
    @Value("${uploadpath}")
    String cleanPath;

    public void execute(String id) {
        File target = new File(cleanPath);
        File[] folders = target.listFiles((dir, name) -> name.equals(id));
        if (folders == null) return;
        for (File folder : folders) {
            File[] files = folder.listFiles();
            if (files == null) continue;
            for (File file : files) file.delete();
            folder.delete();
        }
    }
}
