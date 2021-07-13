

package com.gomyck.fastdfs.starter.lock;

import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 缓存文件锁
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
public class SimpleRedisFileLock implements FileLock {

    @Autowired
    RedisCache rc;

    @Override
    public boolean addLock(String fileKey) {
        long lock;
        try {
            rc.startDoIt();
            lock = rc.incr(fileKey);
        } finally {
            rc.finishDoIt();
        }
        return lock <= 1;
    }

    @Override
    public boolean delLock(String fileKey) {
        try {
            rc.startDoIt();
            rc.delKey(fileKey);
        } finally {
            rc.finishDoIt();
        }
        return true;
    }

    @Override
    public boolean ifLock(String fileKey) {
        String lock;
        try {
            rc.startDoIt();
            lock = rc.get(fileKey);
        } finally {
            rc.finishDoIt();
        }
        return StringJudge.notNull(lock) && Long.parseLong(lock) > 0;
    }
}
