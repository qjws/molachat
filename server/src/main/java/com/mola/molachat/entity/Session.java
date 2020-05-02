package com.mola.molachat.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: molamola
 * @Date: 19-8-5 下午4:35
 * @Version 1.0
 * 聊天会话
 */
@Data
public class Session {

    /**
     * 会话的id
     */
    private String sessionId;

    /**
     * 会话中的聊天者
     */
    private Set<Chatter> chatterSet;

    /**
     * content
     */
    private List<Message> messageList;

    /**
     * 创建时间
     */
    private Date createTime;
}
