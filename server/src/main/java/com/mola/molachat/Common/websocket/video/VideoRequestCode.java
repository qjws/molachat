package com.mola.molachat.Common.websocket.video;

/**
 * @author : molamola
 * @Project: molachat
 * @Description:
 * @date : 2020-05-18 16:40
 **/
public class VideoRequestCode {

    public static String VIDEO_REQUEST_MSG = "request_video";

    /**
     * 发起视频请求
     */
    public static final int REQUEST_VIDEO_ON = 1269;

    /**
     * 取消视频请求\挂断
     */
    public static final int REQUEST_VIDEO_OFF = 1272;

    /**
     * 信令交换
     */
    public static final int SIGNALLING_CHANGE = 1483;

    /**
     * 取消请求
     */
    public static final int REQUEST_CANCEL = 1485;
}
