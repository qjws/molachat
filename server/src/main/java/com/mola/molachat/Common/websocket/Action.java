package com.mola.molachat.Common.websocket;

import lombok.Data;
import lombok.ToString;

/**
 * @Author: molamola
 * @Date: 19-8-10 下午11:31
 * @Version 1.0
 * 前端发来的请求动作
 */
@Data
@ToString
public class Action<T> {

    private Integer code;

    private String msg;

    private T data;

}
