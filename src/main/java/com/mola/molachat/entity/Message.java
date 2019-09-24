package com.mola.molachat.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: molamola
 * @Date: 19-8-5 下午2:51
 * @Version 1.0
 * 聊天信息
 */
@Data
public class Message {

    /**
     * id
     */
    protected String id;

    /**
     * 对应的chatterID
     */
    protected String chatterId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 时间
     */
    protected Date createTime;

}
