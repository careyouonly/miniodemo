package com.yxy.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: miniodemo
 * @description: 封装minio初始化
 * @author: yuxinyu
 * @create: 2020-08-25 09:50
 **/
@Configuration
public class AppConfig {

    private static final String END_POINT = "http://localhost:9000";
    private static final String ACCESSS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient =MinioClient.builder().endpoint(END_POINT).credentials(ACCESSS_KEY, SECRET_KEY).build();
        return minioClient;
    }
}
