package com.gomyck.fastdfs.starter.common;

/**
 * 下载数量异常
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * | blog: https://blog.gomyck.com |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/4/9
 */
public class DownloadFileNumException extends RuntimeException {

    public DownloadFileNumException(String message) {
        super(message);
    }

}
