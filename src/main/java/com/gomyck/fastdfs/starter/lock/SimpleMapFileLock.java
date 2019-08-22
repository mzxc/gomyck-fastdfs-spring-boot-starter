

package com.gomyck.fastdfs.starter.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 *
 * @since [2019-07-25]
 */
public class SimpleMapFileLock extends ConcurrentHashMap<String, Integer> implements FileLock {

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
    public synchronized boolean ifLock(String fileKey) {
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
