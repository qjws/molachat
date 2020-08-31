package com.mola.molachat.service.impl;

import com.mola.molachat.annotation.AddPoint;
import com.mola.molachat.config.SelfConfig;
import com.mola.molachat.enumeration.ChatterPointEnum;
import com.mola.molachat.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
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
        String fileName = RandomStringUtils.randomAlphabetic(5) + "_" + file.getOriginalFilename();
        String url = config.getUploadFilePath() + File.separator + fileName;
        log.info(url);
        File localTmpFile = new File(url);
        //写入文件，此处不能直接getByte，否则会导致内存溢出
        file.transferTo(localTmpFile);
        return "files/"+fileName;
    }

    @Override
    @AddPoint(action = ChatterPointEnum.SEND_FILE, key = "#chatterId")
    public void extend(String chatterId) {
    }
}
