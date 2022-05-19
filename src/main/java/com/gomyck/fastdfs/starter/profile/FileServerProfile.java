

package com.gomyck.fastdfs.starter.profile;

import com.gomyck.util.StringJudge;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置文件
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * | blog: https://blog.gomyck.com |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/7/14
 */
@ConfigurationProperties(value = "gomyck.fastdfs")
@Data
public class FileServerProfile {

    /**
     * 文件服务预览时 使用协议
     */
    private String fileServerProtocol;

    /**
     * 文件服务地址 uri
     */
    private String fileServerUrl;

    /**
     * 默认上传文件服务分组
     */
    private String groupId = "group1";

    /**
     * 下载时块大小(byte 单位)
     */
    private long downloadChunkSize = 1024 * 1024; //下载速度 byte

    /**
     * 最大下载文件数量
     */
    private Integer maxDownloadFileNum = 100;

    /**
     * 分块大小 (MB 单位)
     */
    private Long chunkSize = 5L;

    /**
     * 是否开启异常处理增强
     */
    private boolean enableErrorAdvice = false;

    /**
     * 错误页 hostname, 一般来说, 就是当前的项目 hostname + port, 但是在有网关的情况下, 需要配置成网关, 或者代理服务的地址, 否则会出现被墙的问题
     */
    private String errorPageHostName = "";

    /**
     * 获取文件服务 URI
     * @return
     */
    public String getFileServerURI(){
        return StringJudge.isNull(fileServerProtocol) ? "http" : fileServerProtocol + "://" + fileServerUrl;
    }

    public Long getChunkSize() {
        return chunkSize * 1024 * 1024;
    }

}
