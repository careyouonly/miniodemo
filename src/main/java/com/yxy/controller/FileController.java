package com.yxy.controller;

import com.yxy.bean.MultipartFileResource;
import com.yxy.constant.MinioConstants;
import com.yxy.util.MinioUtil;
import io.minio.*;
import io.minio.http.Method;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @program: miniodemo
 * @description: 文件上传接口
 * @author: yuxinyu
 * @create: 2020-08-25 09:49
 **/
@RestController
public class FileController {

    @Autowired
    public MinioClient minioClient;

    /**
     * 新增bucket
     * @param bucketName
     */
    @RequestMapping("add_bucket")
    public void addBucket (String bucketName) {
        try {
            boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (isExit) {
                System.out.println("1111111111");
            } else {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 新建文件夹
     * @param bucketName
     * @param dirPath
     * @return
     */
    @RequestMapping("create_dir")
    public boolean creatDir(String bucketName, String dirPath) {
        boolean isSuccess = false;
        try {
            boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExit) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            ObjectWriteResponse object = minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(dirPath).stream(new ByteArrayInputStream(new byte[] {}), 0, -1).build());
            if (object != null) {
                isSuccess = true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isSuccess;
    }

    /**
     * 上传文件
     * @param bucketName
     * @param file
     */
    @RequestMapping("files")
    public void uploadfiles(String bucketName, MultipartFile file, String objectName) {
        try {
            boolean isExit = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExit) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.println("111111111111");
            }
            InputStream inputStream = file.getInputStream();
            System.out.println("222222222 : " + file.getContentType());
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(inputStream, file.getSize(), -1).contentType(file.getContentType()).build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 文件下载
     * @param response
     * @param bucketName
     * @param filePath
     */
    @RequestMapping("download")
    public void download(HttpServletResponse response, String bucketName, String filePath) {
        OutputStream outputStream = null;
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
            System.out.println("inputStream : " + inputStream.available());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            byte[] bytes = output.toByteArray();
            output.close();
            response.setContentType(new MimetypesFileTypeMap().getContentType(filePath));
            File file = new File(MinioConstants.BASE_PATH + bucketName + "/" + filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            FileChannel fc = fileInputStream.getChannel();
            int filesize = (int) fc.size();
            response.addHeader("Content-Length", "" + filesize);
            response.addHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(FilenameUtils.getBaseName(filePath) + "." + MinioUtil.getExtension(filePath), "utf-8"));
            outputStream = response.getOutputStream();
            outputStream.write(bytes);
            inputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                System.out.println("stream close error : " + e.getMessage());
            }
        }
    }

    @RequestMapping("rename")
    public void rename (String bucketName, String filePath, String newName) {
        InputStream inputStream = null;
        InputStream stream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            byte[] bytes = output.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            System.out.println("inputStream.available()" + inputStream.available());
            String newFilePath = filePath.substring(0, (filePath.lastIndexOf("/") + 1)) + newName;
            MultipartFileResource multipartFileResource = new MultipartFileResource(byteArrayInputStream, newFilePath);
            stream = multipartFileResource.getInputStream();
            System.out.println("stream.available() : " + stream.available());
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(newFilePath).stream(stream, stream.available(), -1).contentType("image/jpeg").build());
            output.close();
            byteArrayInputStream.close();
        } catch (Exception e) {
            System.out.println("rename error : " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                System.out.println("rename error : " + e.getMessage());
            }
        }
    }

    /**
     * 文件下载，文本、图片类型文件直接预览
     * @param response
     * @param bucketName
     * @param filePath
     */
    @RequestMapping("rander")
    public void rander (HttpServletResponse response, String bucketName, String filePath) {
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
            byte[] buffer = new byte[6384];
            int byteRead;
            int n = 0;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            byte[] bytes = output.toByteArray();
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            inputStream.close();
            output.close();
            outputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 获取文件详情
     * @param bucketName
     * @param objectName
     */
    @RequestMapping("state")
    public void  stateObject(String bucketName, String objectName) {
        try {
            ObjectStat objectStat = minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            System.out.println(objectStat);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     * @param bucketName
     * @param objectName
     * @param destBucketName
     * @param destObjectName
     */
    @RequestMapping("copy")
    public void copyObject(String bucketName, String objectName, String destBucketName, String destObjectName) throws Exception {
        try {
            if (destObjectName.endsWith("/") && !objectName.endsWith("/")) {
                destObjectName = destObjectName + objectName.substring(objectName.lastIndexOf("/"));
            }
        minioClient.copyObject(CopyObjectArgs.builder().bucket(destBucketName).object(destObjectName).source(CopySource.builder().bucket(bucketName).object(objectName).build()).build());
        } catch (Exception e) {
            System.out.println("copyObject : " + e);
        }
    }

    /**
     * 删除文件或者目录，若删除的是目录时，则需先判断目录下是否有文件存在
     * @param bucketName
     * @param objectName
     */
    @RequestMapping("remove")
    public void removeObject(String bucketName, String objectName) {
        try {

            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 生成一个下载地址，文本、图片类型的文件直接预览，其余文件则下载
     * @param bucketName
     * @param objectName
     * @param expires 有效期
     * @return
     */
    @RequestMapping("get_url")
    public String presignedGetObject (String bucketName, String objectName, Integer expires) {
        String url = "";
        try {
            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(objectName).expiry(60*60*24*expires).build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return url;
    }

    @Deprecated
    @RequestMapping("put_url")
    public String presignedPutObject (String bucketName, String objectName, Integer expires) {
        String url = "";
        try {
            url = minioClient.presignedPutObject(bucketName, objectName, 60*60*24*expires);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return url;
    }

    @RequestMapping("policy")
    public Map<String, String> presignedPostPolicy(String bucketName, String objectName, String contentTppe) {
        Map<String, String> map = new HashMap<>();
        try {
            PostPolicy postPolicy = new PostPolicy(bucketName, objectName, ZonedDateTime.now().plusDays(7));
            postPolicy.setContentType("image/png");
            map = minioClient.presignedPostPolicy(postPolicy);
            System.out.print("curl -X POST ");
            for (Map.Entry<String,String> entry : map.entrySet()) {
                System.out.print(" -F " + entry.getKey() + "=" + entry.getValue());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return map;
    }

    @RequestMapping("set_policy")
    public void setBucketPolicy (String bucketName) {
        try {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config("Read and Write").build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @RequestMapping("get_policy")
    public String getBucketPolicy (String bucketName) {
        String bucketPolicy = "";
        try {
            bucketPolicy = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
            System.out.println("11111111111111 : " + bucketPolicy);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return bucketPolicy;
    }

    @RequestMapping("read")
    public String read (String bucketName, String objectName) {
        String result = "";
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
            byte[] buffer = new byte[16384];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                result = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                System.out.println(result);
            }
        } catch (Exception e) {
            System.out.println("read error : " + e);
        }
        return result;
    }

    /**
     * 将文件下载到本地
     * @param bucketName
     * @param objectName
     * @param localPath
     */
    @RequestMapping("down_local")
    public void downlocal (String bucketName, String objectName, String localPath) {
        try {
            minioClient.downloadObject(DownloadObjectArgs.builder().bucket(bucketName).object(objectName).filename(localPath).build());
        } catch (Exception e) {
            System.out.println("getObject error : " + e);
        }
    }

    /**
     * 获取文件路径
     * @param bucketName
     * @param objectName
     * @return
     */
    @RequestMapping("object_url")
    public String getObjectUrl(String bucketName, String objectName) {
        String url = "";
        try {
            url = minioClient.getObjectUrl(bucketName, objectName);
        } catch (Exception e) {
            System.out.println("getObjectUrl() error : " + e);
        }
        return url;
    }

    /**
     * 获取文件访问地址
     * @param bucketName
     * @param objectName
     * @param exoires
     * @return
     * @throws Exception
     */
    @RequestMapping("get_presigned_url")
    public String getPresignedObjectUrl(String bucketName, String objectName, Integer exoires) throws Exception {
        String url = "";
        try {
            GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(objectName).method(Method.GET).expiry(exoires).build();
            url = minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
        } catch (Exception e) {
            System.out.println("getPresignedObjectUrl error : " + e);
            throw new Exception(e);
        }
        return url;
    }
}
