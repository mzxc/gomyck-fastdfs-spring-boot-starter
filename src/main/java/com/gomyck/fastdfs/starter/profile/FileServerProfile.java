

package com.gomyck.fastdfs.starter.profile;

import com.gomyck.util.StringJudge;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-23]
 */
@ConfigurationProperties(value = "gomyck.fastdfs")
public class FileServerProfile {

    private String fileServerProtocol;

    private String fileServerUrl;

    private String groupId = "group1";

    private Long chunkSize = 5L;

    public String getFileServerURI(){
        return StringJudge.isNull(fileServerProtocol) ? "http" : fileServerProtocol + "://" + fileServerUrl;
    }

    public String getFileServerProtocol() {
        return fileServerProtocol;
    }

    public void setFileServerProtocol(String fileServerProtocol) {
        this.fileServerProtocol = fileServerProtocol;
    }

    public String getFileServerUrl() {
        return fileServerUrl;
    }

    public void setFileServerUrl(String fileServerUrl) {
        this.fileServerUrl = fileServerUrl;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Long getChunkSize() {
        return chunkSize * 1024 * 1024;
    }

    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }
}
