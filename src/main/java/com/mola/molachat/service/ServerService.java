package com.mola.molachat.service;

import com.mola.molachat.server.ChatServer;

import java.util.List;

/**
 * @Author: molamola
 * @Date: 19-8-6 上午11:59
 * @Version 1.0
 */
public interface ServerService {

    /**
     * 创建
     * @param chatServer
     * @return
     */
    ChatServer create(ChatServer chatServer);

    /**
     * 根据chatterID查找服务器
     * @param chatterId
     * @return
     */
    ChatServer selectByChatterId(String chatterId);

    /**
     * 移除服务器对象
     * @param chatServer
     * @return
     */
    ChatServer remove(ChatServer chatServer);

    /**
     * 获取所有chatService对象
     * @return
     */
    List<ChatServer> list();

    /**
     * 设置心跳
     */
    void setHeartBeat(String chatterId);

}
