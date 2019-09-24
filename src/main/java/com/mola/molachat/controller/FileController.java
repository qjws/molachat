package com.mola.molachat.controller;

import com.mola.molachat.Common.ServerResponse;
import com.mola.molachat.Common.lock.FileUploadLock;
import com.mola.molachat.entity.FileMessage;
import com.mola.molachat.service.FileService;
import com.mola.molachat.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: molamola
 * @Date: 19-9-12 上午10:04
 * @Version 1.0
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private SessionService sessionService;

    @PostMapping("/upload")
    private ServerResponse upload(@RequestParam("file") MultipartFile file, @RequestParam("sessionId") String sessionId,
                                  HttpServletRequest request, HttpServletResponse response){
        //存储文件
        String url = null;
        try {
            //上锁
            FileUploadLock.lock();
            url = fileService.save(file);
            //创建message
            FileMessage fileMessage = new FileMessage();
            fileMessage.setFileName(file.getOriginalFilename());
            fileMessage.setFileStorage(file.getBytes().toString());
            fileMessage.setUrl(url);
            log.info("chatterId:"+(String) request.getSession().getAttribute("id"));
            fileMessage.setChatterId((String) request.getSession().getAttribute("id"));
            sessionService.insertMessage(sessionId, fileMessage);
            //解锁
            FileUploadLock.unLock();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ServerResponse.createByErrorMessage(e.getMessage());
        }

        return ServerResponse.createBySuccess(url);
    }

    /**
     * todo 取消上传
     */
    @PostMapping("/cancel")
    private ServerResponse cancel(){
        return ServerResponse.createBySuccess();
    }
}
