package com.mola.molachat.service.impl;

import com.mola.molachat.data.impl.ServerFactory;
import com.mola.molachat.enumeration.ServiceErrorEnum;
import com.mola.molachat.exception.ServerException;
import com.mola.molachat.exception.service.ServerServiceException;
import com.mola.molachat.server.ChatServer;
import com.mola.molachat.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: molamola
 * @Date: 19-8-6 上午11:59
 * @Version 1.0
 */
@Service
public class ServerServiceImpl implements ServerService {

    @Autowired
    private ServerFactory serverFactory;

    @Override
    public ChatServer selectByChatterId(String chatterId) {
        return serverFactory.selectOne(chatterId);
    }

    @Override
    public ChatServer create(ChatServer chatServer){
        //data层创建服务器
        ChatServer server = null;
        try {
            server = serverFactory.create(chatServer);
        } catch (ServerException e) {
            throw new ServerServiceException(ServiceErrorEnum.SERVER_CREATE_ERROR, e.getMessage());
        }
        return server;
    }

    @Override
    public ChatServer remove(ChatServer chatServer) {

        ChatServer server = null;
        try {
            server = serverFactory.remove(chatServer);
        } catch (ServerException e) {
            throw new ServerServiceException(ServiceErrorEnum.SERVER_REMOVE_ERROR, e.getMessage());
        }

        return server;
    }

    @Override
    public List<ChatServer> list() {
        return serverFactory.list();
    }

    @Override
    public void setHeartBeat(String chatterId) {
        ChatServer server = serverFactory.selectOne(chatterId);
        if (null == server){
            //未找到chatterId对应的服务器
            throw new ServerServiceException(ServiceErrorEnum.SERVER_NOT_FOUND);
        }
        server.setLastHeartBeat(System.currentTimeMillis());
    }
}
