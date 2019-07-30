package com.gomyck.fastdfs.starter.controller;

import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.database.entity.FileInfo;
import com.gomyck.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * @author 郝洋 QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-28]
 */
@RestController
@RequestMapping("download/chunkDownload")
public class ChunkDownloadHandler {

    @Autowired
    FastFileStorageClient ffsc;

    @GetMapping("simpleFileDownload")
    public void chunkDownload(FileInfo fi){

        DownloadByteArray callback = new DownloadByteArray();
        byte[] content = ffsc.downloadFile(fi.getGroup(), fi.getUploadPath(), callback);
        boolean ifDownload = ResponseWriter.writeFile(content, fi.getName(), fi.getType());

    }




}
