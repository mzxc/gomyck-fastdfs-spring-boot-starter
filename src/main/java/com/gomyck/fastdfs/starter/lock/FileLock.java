

package com.gomyck.fastdfs.starter.lock;

/**
 * @author gomyck
 * @version [版本号/1.0]
 *
 * @since [2019-07-25]
 */
public interface FileLock {

    boolean addLock(String fileKey);

    boolean delLock(String fileKey);

    boolean ifLock(String fileKey);

}
