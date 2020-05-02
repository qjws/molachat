package com.mola.molachat.aspect;

import com.mola.molachat.Common.websocket.WSResponse;
import com.mola.molachat.annotation.RefreshChatterList;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.enumeration.ChatterStatusEnum;
import com.mola.molachat.server.ChatServer;
import com.mola.molachat.service.ChatterService;
import com.mola.molachat.service.ServerService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.EncodeException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: molamola
 * @Date: 19-8-8 下午4:06
 * @Version 1.0
 */
@Component
@Aspect
@Slf4j
public class RefreshChattersAspect {

    @Autowired
    private ServerService serverService;

    @Autowired
    private ChatterService chatterService;

    @Pointcut("@annotation(com.mola.molachat.annotation.RefreshChatterList)")
    public void pointCut(){}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws IOException, EncodeException{

        Object obj = null;
        try {
            obj = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RefreshChatterList annotation = method.getAnnotation(RefreshChatterList.class);
        if (null != annotation){
            log.info("chatterList变动，发送");
            //向客户端发送更新消息
            List<ChatterDTO> chatterList = chatterService.list()
                    .stream()
                    // 过滤逻辑删除的chatter
                    .filter(chatterDTO -> chatterDTO.getStatus() != ChatterStatusEnum.LOGICAL_DELETE.getCode())
                    .collect(Collectors.toList());
            // 按照状态排序
            chatterList.sort((o1, o2) -> {
                Integer s1 = o1.getStatus();
                Integer s2 = o2.getStatus();
                return s1 > s2 ? -1 : 1;
            });
            for (ChatServer server : serverService.list()){
                server.getSession().getBasicRemote()
                        .sendObject(WSResponse.list("ok", chatterList));
            }
        }
        return obj;
    }
}
