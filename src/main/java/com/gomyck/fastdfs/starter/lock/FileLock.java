

package com.gomyck.fastdfs.starter.lock;

/**
 * 文件锁
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/7/13
 */
public interface FileLock {

    /**
     * 添加锁
     *
     * @param fileKey md5
     * @return 是否成功上锁
     */
    boolean addLock(String fileKey);

    /**
     * 删除锁
     *
     * @param fileKey md5
     * @return 是否成功
     */
    boolean delLock(String fileKey);

    /**
     * 是否有锁
     *
     * @param fileKey md5
     * @return 是否有锁
     */
    boolean ifLock(String fileKey);

}
