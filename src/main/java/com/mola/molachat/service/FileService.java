package com.mola.molachat.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author: molamola
 * @Date: 19-9-12 上午10:39
 * @Version 1.0
 */
public interface FileService {

    /**
     * 存储文件
     * @param file
     * @return
     */
    String save(MultipartFile file) throws IOException;
}
