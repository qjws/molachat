package com.mola.molachat.data.impl;

import com.mola.molachat.config.SelfConfig;
import com.mola.molachat.data.SessionFactoryInterface;
import com.mola.molachat.entity.Chatter;
import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.Session;
import com.mola.molachat.enumeration.DataErrorCodeEnum;
import com.mola.molachat.exception.SessionException;
import com.mola.molachat.utils.IdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author: molamola
 * @Date: 19-8-5 下午4:56
 * @Version 1.0
 * 管理session的工厂
 */
@Component
@Slf4j
public class SessionFactory implements SessionFactoryInterface {

    @Autowired
    private SelfConfig config;

    /**
     * sessionMap sessionId -> entity
     */
    private static Map<String, Session> sessionMap;

    public SessionFactory(){
        sessionMap = new HashMap<>();
    }

    @Override
    public synchronized Session create(Set<Chatter> chatterSet) throws SessionException{

        if (chatterSet.size() < 2){
            log.info("集合中小于两个chatter，无法创建session");
            throw new SessionException(DataErrorCodeEnum.CREATE_SESSION_ERROR
                    , "集合中小于两个chatter，无法创建session");
        }

        //sessionID为聊天室包含所有聊天者的id，第一个为创建者
        StringBuffer sessionId = new StringBuffer("");
        for (Chatter chatter : chatterSet){
            sessionId.append(chatter.getId());
        }

        //1.创建session
        Session session = new Session();
        session.setChatterSet(chatterSet);
        session.setMessageList(new ArrayList<>());
        session.setSessionId(sessionId.toString());
        session.setCreateTime(new Date());

        //2.加入map
        sessionMap.put(session.getSessionId(),session);

        return session;
    }

    @Override
    public Session create(Session session) {
        sessionMap.put(session.getSessionId(), session);
        return session;
    }

    @Override
    public Session selectById(String id) {
        return sessionMap.get(id);
    }


    @Override
    public synchronized Session remove(Session session) throws SessionException{

        if (!sessionMap.keySet().contains(session.getSessionId())){
            throw new SessionException(DataErrorCodeEnum.REMOVE_SESSION_ERROR);
        }
        sessionMap.remove(session.getSessionId());

        return session;
    }

    @Override
    public List<Session> list() {

        List<Session> sessionList = new ArrayList<>();

        for(String key : sessionMap.keySet()){
            sessionList.add(sessionMap.get(key));
        }

        return sessionList;
    }

    @Override
    public Message insertMessage(String sessionId, Message message) throws SessionException{

        Session session = sessionMap.get(sessionId);
        if (null == session){
            throw new SessionException(DataErrorCodeEnum.SESSION_NOT_EXIST);
        }
        List<Message> messageList = session.getMessageList();

        message.setId(IdUtils.getMessageId());
        message.setCreateTime(new Date());

        //针对每一个messageList同步
        synchronized (messageList){
            if (messageList.size() >= config.getMAX_SESSION_MESSAGE_NUM()){
                messageList.remove(0);
            }
            messageList.add(message);
        }

        return message;
    }
}
