package com.gomyck.fastdfs.starter.common;

/**
 * 下载数量异常
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/4/9
 */
public class DownloadFileNumException extends RuntimeException {

    //下载数量异常
    public DownloadFileNumException(String message) {
        super(message);
    }

}
