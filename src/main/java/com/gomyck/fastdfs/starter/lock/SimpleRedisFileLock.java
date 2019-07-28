

package com.gomyck.fastdfs.starter.lock;

import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-25]
 */
public class SimpleRedisFileLock implements FileLock {

    @Autowired
    RedisCache rc;

    @Override
    public boolean addLock(String fileKey) {
        rc.startDoIt();
        long lock = rc.incr(fileKey);
        rc.finishDoIt();
        return lock <= 1;
    }

    @Override
    public boolean delLock(String fileKey) {
        rc.startDoIt();
        rc.delKey(fileKey);
        rc.finishDoIt();
        return true;
    }

    @Override
    public boolean ifLock(String fileKey) {
        rc.startDoIt();
        String lock = rc.get(fileKey);
        rc.finishDoIt();
        return StringJudge.notNull(lock) && Long.parseLong(lock) > 0;
    }
}
