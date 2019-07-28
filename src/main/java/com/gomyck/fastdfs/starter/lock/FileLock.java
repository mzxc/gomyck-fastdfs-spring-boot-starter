

package com.gomyck.fastdfs.starter.lock;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-25]
 */
public interface FileLock {

    boolean addLock(String fileKey);

    boolean delLock(String fileKey);

    boolean ifLock(String fileKey);

}
