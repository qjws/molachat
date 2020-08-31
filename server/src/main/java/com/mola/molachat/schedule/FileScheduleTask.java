package com.mola.molachat.schedule;

import com.mola.molachat.Common.lock.FileUploadLock;
import com.mola.molachat.config.SelfConfig;
import com.mola.molachat.entity.FileMessage;
import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.dto.SessionDTO;
import com.mola.molachat.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-04-30 10:22
 **/
@Configuration
@EnableScheduling
@Slf4j
public class FileScheduleTask {

    @Autowired
    private FileUploadLock lock;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SelfConfig config;

    /**
     * 检查文件是否被消息持有
     */
    @Scheduled(fixedRate = 120000)
    private void cleanUselessCacheFile(){
        log.info("check:开始检查服务器文件有效性");

        Set<String> fileNameSet = new HashSet<>();
        //1.调出所有session的filemessage对象
        List<SessionDTO> sessionList = sessionService.list();
        for (SessionDTO sess : sessionList){
            for (Message message : sess.getMessageList()){
                if (message instanceof FileMessage){
                    //2.将文件名存入HashSet
                    String url = ((FileMessage) message).getUrl();
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    fileNameSet.add(fileName);
                }
            }
        }
        lock.writeLock();
        //3.判断是否存在,过滤存在文件
        File file = new File(config.getUploadFilePath());
        //如果不存在文件夹，则创建
        if (!file.exists()){
            file.mkdir();
            log.info("创建文件夹成功");
        }
        lock.writeUnlock();

        lock.readLock();
        if(file.isDirectory()){
            for (File f : file.listFiles(pathname -> {
                if (!fileNameSet.contains(pathname.getName()))
                    return true;
                else
                    return false;
            })){
                f.delete();
            }
        }else {
            log.error("配置路径可能存在错误");
        }
        lock.readUnlock();
    }
}
