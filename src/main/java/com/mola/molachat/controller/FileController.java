package com.mola.molachat.controller;

import com.mola.molachat.Common.ResponseCode;
import com.mola.molachat.Common.ServerResponse;
import com.mola.molachat.Common.lock.FileUploadLock;
import com.mola.molachat.entity.FileMessage;
import com.mola.molachat.service.FileService;
import com.mola.molachat.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private FileUploadLock lock;

    @PostMapping("/upload")
    @ExceptionHandler(value = FileUploadBase.SizeLimitExceededException.class)
    private ServerResponse upload(@RequestParam("file") MultipartFile file, @RequestParam("chatterId") String chatterId,
                                  @RequestParam("sessionId") String sessionId,
                                  HttpServletRequest request, HttpServletResponse response){
        //session验证
        if (!checkSession(request, chatterId)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ERROR.getCode(),
                    "session验证错误");
        }
        //存储文件
        String url = null;
        try {
            //上锁
            lock.writeLock();
            url = fileService.save(file);
            //创建message
            FileMessage fileMessage = new FileMessage();
            fileMessage.setFileName(file.getOriginalFilename());
            fileMessage.setFileStorage(file.getBytes().toString());
            fileMessage.setUrl(url);
            log.info("chatterId:"+(String) request.getSession().getAttribute("id"));
            fileMessage.setChatterId((String) request.getSession().getAttribute("id"));
            sessionService.insertMessage(sessionId, fileMessage);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ServerResponse.createByErrorMessage(e.getMessage());
        } finally {
            //解锁
            lock.writeUnlock();
        }

        return ServerResponse.createBySuccess(url);
    }

    private Boolean checkSession(HttpServletRequest request, String chatterId){
        if (chatterId.equalsIgnoreCase((String) request.getSession().getAttribute("id"))){
            return true;
        }
        return false;
    }
}
