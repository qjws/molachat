package com.mola.molachat.service;


import com.mola.molachat.entity.Message;
import com.mola.molachat.entity.dto.ChatterDTO;
import com.mola.molachat.entity.dto.SessionDTO;

import java.util.List;
import java.util.Set;

/**
 * @Author: molamola
 * @Date: 19-8-6 上午11:07
 * @Version 1.0
 */
public interface SessionService {

    /**
     * 创建session
     * @return
     */
    SessionDTO create(Set<ChatterDTO> chatterDTOSet);

    /**
     * 查询session
     * @param
     * @return
     */
    SessionDTO findSession(String chatterId1, String chatterId2);

    /**
     * 列出所有session信息
     * @return
     */
    List<SessionDTO> list();

    /**
     * 根据chatterId关闭session
     * @param chatterId
     * @return
     */
    Integer closeSessions(String chatterId);

    /**
     * 向一个session中插入消息
     * @param sessionId
     * @param message
     * @return
     */
    Message insertMessage(String sessionId, Message message);

    /**
     * 保存sessionList
     * @param sessionList
     */
    void save(List<SessionDTO> sessionList);
}
