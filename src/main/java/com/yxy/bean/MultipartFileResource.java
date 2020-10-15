package com.yxy.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @program: miniodemo
 * @description: MultipartFileResource
 * @author: yuxinyu
 * @create: 2020-08-28 09:44
 **/
public class MultipartFileResource extends InputStreamResource {

    private String fileName;

    private InputStream inputStream;

    public MultipartFileResource(InputStream inputStream, String fileName) {
        super(inputStream);
        this.inputStream = inputStream;
        this.fileName = fileName;
    }

    @Override
    public String getFilename() {
        return this.fileName;
    }

    @Override
    public long contentLength() throws IOException {
        return inputStream.available();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this || (obj instanceof MultipartFileResource && ((MultipartFileResource) obj).inputStream.equals(this.inputStream)));
    }
}
