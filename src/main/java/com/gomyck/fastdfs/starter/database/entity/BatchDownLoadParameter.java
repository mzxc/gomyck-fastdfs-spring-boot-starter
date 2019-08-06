package com.gomyck.fastdfs.starter.database.entity;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-08-06]
 */
public class BatchDownLoadParameter {

    private String fileMd5; //文件摘要

    private String zipSrc; //在压缩包中的目录

    private String fileName; //文件名, 可以为空, 空时取原始文件名

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getZipSrc() {
        return zipSrc;
    }

    public void setZipSrc(String zipSrc) {
        this.zipSrc = zipSrc;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
