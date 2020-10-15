package com.yxy.util;

import io.minio.MinioClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.util.StringUtils;

/**
 * @program: miniodemo
 * @description: 工具类
 * @author: yuxinyu
 * @create: 2020-08-26 12:05
 **/
public class MinioUtil {

    @Autowired
    public static MinioClient minioClient;

    public static String getExtension(String path) {
        String extension = null;
        if (!StringUtils.isEmpty(path)) {
            if (path.contains("?")) {
                String[] path1 =path.split("\\?");
                if (path1.length > 0) {
                    path = path1[0];
                }
            }
            extension = FilenameUtils.getExtension(path);
            if (!StringUtils.isEmpty(extension)) {
                extension = extension.toLowerCase();
            }
        }
        return extension;
    }
}
