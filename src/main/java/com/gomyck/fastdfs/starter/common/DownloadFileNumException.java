package com.gomyck.fastdfs.starter.common;

/**
 * 下载数量异常
 *
 * @author gomyck
 * @version 1.0.0
 */
public class DownloadFileNumException extends RuntimeException {

    public DownloadFileNumException(String message) {
        super(message);
    }

}
