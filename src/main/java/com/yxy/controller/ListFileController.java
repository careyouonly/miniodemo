package com.yxy.controller;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: miniodemo
 * @description: 列表页查询
 * @author: yuxinyu
 * @create: 2020-08-25 13:57
 **/
@RestController
public class ListFileController {

    @Autowired
    MinioClient minioClient;

    /**
     * 获取所有的bucket
     * @return
     */
    @PostMapping("list_bucket")
    public List<Bucket> getBucketList() {
        List<Bucket> bucketList = new ArrayList<>();
        try {
            bucketList = minioClient.listBuckets();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return bucketList;
    }

    /**
     * 获取bucket下所有文件文件夹
     * @param bucketName 存储桶名称
     * @param prefix 对象名称的前缀 可以目录路径
     * @param recursive 是否递归查找，如果是false,就模拟文件夹结构查找 为true时则遍历当前bucket下所有的文件，为false则遍历bucket下一层中所有的文件、目录，不遍历文件夹
     * @param useVersion1 如果是true, 使用版本1 REST API
     * @return
     */
    @PostMapping("list")
    public List<Item> getObjects (String bucketName, String prefix, boolean recursive, boolean useVersion1) {
        List<Item> list = new ArrayList<>();
        try {
            Iterable<Result<Item>> iterable = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(recursive).useApiVersion1(useVersion1).build());
            for (Result<Item> itemResult : iterable) {
                Item item = itemResult.get();
                list.add(item);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    @PostMapping("url")
    public String updateObject (String bucket, String object) {
        String url = "";
        try {
            url = minioClient.getObjectUrl(bucket, object);
            String url1 = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket).object(object).method(Method.GET).build());
            System.out.println(url1);
        } catch (Exception e) {
            System.out.println("updateObject error : " + e);
        }
        return url;
    }
}
