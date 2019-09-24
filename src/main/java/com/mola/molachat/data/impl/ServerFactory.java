package com.mola.molachat.data.impl;

import com.mola.molachat.annotation.RefreshChatterList;
import com.mola.molachat.data.ServerFactoryInterface;
import com.mola.molachat.enumeration.DataErrorCodeEnum;
import com.mola.molachat.exception.ServerException;
import com.mola.molachat.server.ChatServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: molamola
 * @Date: 19-8-6 上午11:20
 * @Version 1.0
 */
@Component
@Slf4j
public class ServerFactory implements ServerFactoryInterface {

    /**
     * chatterId -> ChatServer
     */
    private static Map<String, ChatServer> serverMap;

    public ServerFactory(){
        serverMap = new HashMap<>();
    }

    @Override
    @RefreshChatterList
    public ChatServer create(ChatServer server) throws ServerException{
        if (serverMap.keySet().contains(server.getChatterId())){
            throw new ServerException(DataErrorCodeEnum.CREATE_SERVER_ERROR);
        }
        serverMap.put(server.getChatterId(), server);

        return server;
    }

    @Override
    @RefreshChatterList
    public ChatServer remove(ChatServer server) throws ServerException{
        if (!serverMap.keySet().contains(server.getChatterId())){
            throw new ServerException(DataErrorCodeEnum.REMOVE_SERVER_ERROR);
        }
        serverMap.remove(server.getChatterId());

        return server;
    }

    @Override
    public ChatServer selectOne(String chatterId){
        return serverMap.get(chatterId);
    }

    @Override
    public Session selectWSSessionByChatterId(String chatterId) throws ServerException{

        ChatServer server = this.selectOne(chatterId);

        return server.getSession();
    }

    @Override
    public List<ChatServer> list() {
        List<ChatServer> serverList = new ArrayList<>();
        for (String key : serverMap.keySet()){
            serverList.add(serverMap.get(key));
        }
        return serverList;
    }
}
