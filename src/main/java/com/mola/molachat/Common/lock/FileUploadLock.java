package com.mola.molachat.Common.lock;

/**
 * @Author: molamola
 * @Date: 19-9-14 上午12:14
 * @Version 1.0
 * 上传文件锁
 */
public class FileUploadLock {

    /**
     * 锁
     */
    private volatile static Boolean lock = false;

    /**
     * 加锁
     */
    public synchronized static void lock(){
        lock = true;
    }

    /**
     * 解锁
     */
    public synchronized static void unLock(){
        lock = false;
    }

    /**
     * 查看锁
     * @return
     */
    public static Boolean catLock(){
        return lock;
    }
}
