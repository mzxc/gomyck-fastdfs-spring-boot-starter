package com.gomyck.fastdfs.starter.controller;

import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-30]
 */
@Controller
@RequestMapping("download/simpleDownload")
public class SimpleFileDownloadHandler {

    @Autowired
    FastFileStorageClient ffsc;

    @Autowired
    UploadService us;

    /**
     * 文件下载 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param fileMd5
     * @return
     */
    @GetMapping("downloadFile")
    public void chunkDownload(String fileMd5){
        CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
        DownloadByteArray callback = new DownloadByteArray();
        byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), callback);
        ResponseWriter.writeFile(content, fileInfo.getName(), fileInfo.getType(), true);
    }


}
