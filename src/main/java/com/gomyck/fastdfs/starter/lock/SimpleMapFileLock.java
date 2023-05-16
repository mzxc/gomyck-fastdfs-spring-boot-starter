

package com.gomyck.fastdfs.starter.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 默认的文件锁
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * | blog: https://blog.gomyck.com |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/7/13
 */
public class SimpleMapFileLock extends ConcurrentHashMap<String, Integer> implements FileLock {

    /**
     * 文件锁, 简单的公平同步锁
     */
    private static ReentrantLock rl = new ReentrantLock(true);

    @Override
    public boolean addLock(String fileKey) {
        try{
            rl.lock();
            Integer integer = get(fileKey);
            integer = (integer == null ? 0 : integer);
            if (integer > 0) {
                return false;
            }
            this.put(fileKey, 1);
        }finally {
            rl.unlock();
        }
        return true;
    }

    @Override
    public  boolean delLock(String fileKey) {
        try{
            rl.lock();
            remove(fileKey);
            //put(fileKey, 0);
        }finally {
            rl.unlock();
        }
        return true;
    }

    @Override
    public boolean ifLock(String fileKey) {
        try{
            rl.lock();
            Integer integer = get(fileKey);
            integer = (integer == null ? 0 : integer);
            return integer > 0;
        }finally {
            rl.unlock();
        }
    }
}
