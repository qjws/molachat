package com.mola.molachat.entity.dto;

import lombok.Data;

import java.util.Date;

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
}
