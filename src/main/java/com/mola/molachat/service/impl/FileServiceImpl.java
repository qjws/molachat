package com.mola.molachat.service.impl;

import com.mola.molachat.config.SelfConfig;
import com.mola.molachat.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @Author: molamola
 * @Date: 19-9-12 上午10:39
 * @Version 1.0
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private SelfConfig config;

    @Override
    public String save(MultipartFile file) throws IOException{
        String url = config.getUploadFilePath() + File.separator + file.getOriginalFilename();
        log.info(url);
        File localTmpFile = new File(url);
        //写入文件夹
        FileUtils.writeByteArrayToFile(localTmpFile, file.getBytes());

        return "files/"+file.getOriginalFilename();
    }
}
