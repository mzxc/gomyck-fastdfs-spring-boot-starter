

package com.gomyck.fastdfs.starter.lock;

import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 *
 * @since [2019-07-25]
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
