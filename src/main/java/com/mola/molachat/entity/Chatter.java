package com.mola.molachat.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Author: molamola
 * @Date: 19-8-5 下午2:45
 * @Version 1.0
 * 实体：聊天者
 */
@Data
public class Chatter {

    /**
     * id唯一
     */
    private String id;

    /**
     * 昵称
     */
    private String name;

    /**
     * ip
     */
    private String ip;

    /**
     * status,离线or在线
     */
    private Integer status;

    /**
     * createTime
     */
    private Date createTime;

    /**
     * 头像url
     */
    private String imgUrl;

    /**
     * 个性签名
     */
    private String signature;

}
