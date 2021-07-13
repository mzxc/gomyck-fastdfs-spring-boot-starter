

package com.gomyck.fastdfs.starter.lock;

/**
 * 文件锁
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
public interface FileLock {

    boolean addLock(String fileKey);

    boolean delLock(String fileKey);

    boolean ifLock(String fileKey);

}
