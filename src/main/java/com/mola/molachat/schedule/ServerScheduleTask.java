package com.mola.molachat.schedule;

import com.mola.molachat.Common.lock.FileUploadLock;
import com.mola.molachat.config.SelfConfig;
import com.mola.molachat.entity.FileMessage;
import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.entity.dto.SessionDTO;
import com.mola.molachat.enumeration.ChatterStatusEnum;
import com.mola.molachat.server.ChatServer;
import com.mola.molachat.service.ChatterService;
import com.mola.molachat.service.ServerService;
import com.mola.molachat.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.EncodeException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: molamola
 * @Date: 19-8-18 下午10:53
 * @Version 1.0
 * 用于ws的定时任务
 */
@Component
@Configuration
@EnableScheduling
@Slf4j
public class ServerScheduleTask {

    @Autowired
    private SelfConfig config;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ChatterService chatterService;

    @Autowired
    private SessionService sessionService;

    /**
     * 检查所有服务器的最后心跳时间,大于15秒当做连接失败
     */
    @Scheduled(fixedRate = 30000)
    private void checkServersStatus(){
        log.info("check:开始检查所有连接状态");
        List<ChatServer> chatServerList = serverService.list();
        for (ChatServer server : chatServerList){
            Long lastHeartBeat = server.getLastHeartBeat();

            if (null == lastHeartBeat){
                continue;
            }
            //距离上次心跳{自定义}分钟以上，将chatter状态设置为离线
            if (System.currentTimeMillis() - lastHeartBeat > config.getCONNECT_TIMEOUT()){
                log.error("超时为"+(System.currentTimeMillis() - lastHeartBeat)+",chatter离线");
                chatterService.setChatterStatus(server.getChatterId(),
                        ChatterStatusEnum.OFFLINE.getCode());
            }

            //若距离上次心跳{自定义}分钟以上，删除服务器
            if (System.currentTimeMillis() - lastHeartBeat > config.getCLOSE_TIMEOUT()){
                log.error("超时为"+(System.currentTimeMillis() - lastHeartBeat)+",chatter关闭服务器");
                try {
                    //设置为离线
                    server.onClose();
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 扫描删除未一一对应的server或chatter
     */
    @Scheduled(fixedRate = 20000)
    private void checkChatterAlive(){
        log.info("开始检查chatter是否持有server");
        for (ChatterDTO chatter : chatterService.list()){
            String chatterId = chatter.getId();
            if (null == serverService.selectByChatterId(chatterId)){
                ChatterDTO dto = new ChatterDTO();
                dto.setId(chatterId);
                chatterService.remove(dto);
            }
        }
        log.info("开始检查server是否持有chatter");
        for (ChatServer server : serverService.list()){
            String serverId = server.getChatterId();
            if (null == chatterService.selectById(serverId)){
                try {
                    //关闭服务器
                    server.onClose();
                } catch (IOException | EncodeException e) {
                }
            }
        }
    }

    /**
     * 检查文件是否被消息持有
     */
    @Scheduled(fixedRate = 45000)
    private void cleanUselessCacheFile(){
        log.info("check:开始检查服务器文件有效性");
        // todo 如果有锁，等待锁释放
        while (FileUploadLock.catLock()){
        }
        Set<String> fileNameSet = new HashSet<>();
        //1.调出所有session的filemessage对象
        List<SessionDTO> sessionList = sessionService.list();
        for (SessionDTO sess : sessionList){
            for (Message message :sess.getMessageList()){
                if (message instanceof FileMessage){
                    //2.将文件名存入HashSet
                    fileNameSet.add(((FileMessage) message).getFileName());
                }
            }
        }
        //3.判断是否存在,过滤存在文件
        File file = new File(config.getUploadFilePath());
        //如果不存在文件夹，则创建
        if (!file.exists()){
            file.mkdir();
            log.info("创建文件夹成功");
        }
        if(file.isDirectory()){
            for (File f : file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (!fileNameSet.contains(pathname.getName()))
                        return true;
                    else
                        return false;
                }
            })){
                f.delete();
            }
        }else {
            log.error("配置路径可能存在错误");
        }

    }

}
