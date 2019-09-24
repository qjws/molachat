package com.mola.molachat.entity;

import lombok.Data;

/**
 * @Author: molamola
 * @Date: 19-9-11 下午5:21
 * @Version 1.0
 * 文件聊天信息
 */
@Data
public class FileMessage extends Message{

    /**
     * 文件的访问地址
     */
    private String url;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private String fileStorage;
}
