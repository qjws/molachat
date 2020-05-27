package com.mola.molachat.entity.dto;

import lombok.Data;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: molamola
 * @Date: 19-8-6 下午4:19
 * @Version 1.0
 */
@Data
public class ChatterDTO {

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
     * status
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
     * 签名
     */
    private String signature;

    //存放最近在线的时间
    private Long lastOnline = System.currentTimeMillis();

    /**
     * 活跃度评分
     */
    private Integer point = 0;

    /**
     * 视频请求状态
     * 0: 未占用
     * 1: 请求中
     * 2: 已占用
     */
    private AtomicInteger videoState = new AtomicInteger(0);

}
