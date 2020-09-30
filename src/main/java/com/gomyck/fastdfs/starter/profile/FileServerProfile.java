

package com.gomyck.fastdfs.starter.profile;

import com.gomyck.util.StringJudge;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 *
 * @since [2019-07-23]
 */
@ConfigurationProperties(value = "gomyck.fastdfs")
@Data
public class FileServerProfile {

    private String fileServerProtocol;

    private String fileServerUrl;

    private String groupId = "group1";

    private long downloadChunkSize = 1024 * 1024; //下载速度 byte

    //最大下载文件数量
    private Integer maxDownloadFileNum = 100;

    //分块大小 MB
    private Long chunkSize = 5L;

    //是否开启异常处理增强
    private boolean enableErrorAdvice = false;

    //错误页 hostname, 一般来说, 就是当前的项目 hostname + port, 但是在有网关的情况下, 需要配置成网关, 或者代理服务的地址, 否则会出现被墙的问题
    private String errorPageHostName = "";

    public String getFileServerURI(){
        return StringJudge.isNull(fileServerProtocol) ? "http" : fileServerProtocol + "://" + fileServerUrl;
    }

    public Long getChunkSize() {
        return chunkSize * 1024 * 1024;
    }

}
