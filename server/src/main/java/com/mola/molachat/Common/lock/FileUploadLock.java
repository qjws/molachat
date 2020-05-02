package com.mola.molachat.Common.lock;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author: molamola
 * @Date: 19-9-14 上午12:14
 * @Version 1.0
 * 上传文件锁
 */
@Component
public class FileUploadLock {

    /**
     * 文件读写锁
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private ReentrantReadWriteLock.ReadLock rLock;

    private ReentrantReadWriteLock.WriteLock wLock;

    @PostConstruct
    public void initLock() {
        rLock = lock.readLock();
        wLock = lock.writeLock();
    }

    /**
     * 加锁
     */
    public void writeLock(){
        wLock.lock();
    }
    public void readLock(){
        rLock.lock();
    }

    /**
     * 解锁
     */
    public void readUnlock(){
        rLock.unlock();
    }
    public void writeUnlock() {wLock.unlock(); }

}
