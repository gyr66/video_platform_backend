package com.gyr.minio.config;

import me.desair.tus.server.TusFileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TusConfig {
    @Value("${tus.server.data.directory}")
    private String tusDataPath;

    @Bean
    public TusFileUploadService tusFileUploadService() {
        return new TusFileUploadService()
                .withStoragePath(tusDataPath)
                .withUploadExpirationPeriod(24 * 3600 * 1000L)
                .withUploadURI("/upload")
                .withThreadLocalCache(true);
    }
}
