package com.mola.molachat.Common.websocket;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: molamola
 * @Date: 19-8-8 下午5:18
 * @Version 1.0
 * websocket传输对象
 */
@Data
public class WSResponse<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;

    public static <T> WSResponse<T> list(String msg, T data){
        return new WSResponse<>(WSResponseCode.LIST, msg, data);
    }

    public static <T> WSResponse<T> message(String msg, T data){
        return new WSResponse<>(WSResponseCode.MESSAGE, msg, data);
    }

    public static <T> WSResponse<T> exception(String msg, T data){
        return new WSResponse<>(WSResponseCode.EXCEPTION, msg, data);
    }

    public static <T> WSResponse<T> createSession(String msg, T data){
        return new WSResponse<>(WSResponseCode.CREATE_SESSION, msg, data);
    }

    private WSResponse(Integer code, String msg, T data){
        this.msg = msg;
        this.code = code;
        this.data = data;
    }
}
