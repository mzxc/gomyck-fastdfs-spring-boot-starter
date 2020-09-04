

package com.gomyck.fastdfs.starter.database.entity;

import lombok.Data;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 * @since [2019-07-24]
 */
@Data
public class CkFileInfo {

    private String id;

    private String name; //文件名称

    private String group; //组

    private String uploadPath; //文件服务器路径

    private String fileMd5; //摘要

    private Long size; //文件大小

    private String uploadTime; //上传时间

    private String type;

    private String chunk;

    private String chunks;

    private Long chunkSize; //每块文件的大小

    private Long expireTime; //过期时间, 以秒为单位

    private boolean thumbFlag = true; //是否生成略缩图 默认生成

    private String thumbImgPath; //略缩图文件位置

    private Integer thumbImgWidth; //缩放长度

    private Integer thumbImgHeight; //缩放高度

    private Double thumbImgPercent; //缩放比例 如果存在宽高比, 那么默认按照宽高比缩放

}