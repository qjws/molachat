package com.mola.molachat.entity;

import lombok.Data;

import java.util.Date;
import java.util.Objects;

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

    //存放最近在线的时间
    private Long lastOnline = System.currentTimeMillis();

    /**
     * 活跃度评分
     */
    private Integer point = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chatter chatter = (Chatter) o;
        return id.equals(chatter.getId()) && name.equals(chatter.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
