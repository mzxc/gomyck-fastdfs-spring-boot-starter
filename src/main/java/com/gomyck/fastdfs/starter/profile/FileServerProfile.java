

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

    private Integer maxDownloadFileNum = 100;

    private Long chunkSize = 5L;

    public String getFileServerURI(){
        return StringJudge.isNull(fileServerProtocol) ? "http" : fileServerProtocol + "://" + fileServerUrl;
    }

    public Long getChunkSize() {
        return chunkSize * 1024 * 1024;
    }

}
