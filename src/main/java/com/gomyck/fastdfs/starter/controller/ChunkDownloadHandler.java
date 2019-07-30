package com.gomyck.fastdfs.starter.controller;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.common.FileNotFoundException;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 郝洋 QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-28]
 */
@Controller
@RequestMapping("download/chunkDownload")
public class ChunkDownloadHandler {

    @Autowired
    FastFileStorageClient ffsc;

    @Autowired
    UploadService us;

    @Value("${gomyck.fastdfs.download-chunk-size: 1000000}")
    private long chunkFileSize;

    /**
     * 文件下载 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param fileMd5
     * @return
     */
    @GetMapping("downloadFile")
    public void chunkDownload(String fileMd5){
        DownloadByteArray callback = new DownloadByteArray();
        CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
        if(fileInfo == null) throw new FileNotFoundException("数据列表中不存在该文件");
        FileInfo remoteFileInfo = ffsc.queryFileInfo(fileInfo.getGroup(), fileInfo.getUploadPath());
        if(remoteFileInfo == null) throw new FileNotFoundException("文件服务器中不存在该文件");
        long cycle = 0L;  //下载次数
        long offset = 0L; //当前偏移量
        long downloadFileSize = chunkFileSize; //当前实际要下载的块大小
        long remoteFileSize = remoteFileInfo.getFileSize(); //文件服务器存储的文件大小 (byte为单位)
        for(;;cycle = cycle + 1L){
            //todo 如果文件大小 小于分块大小, 一次性下载
            if(remoteFileSize <= chunkFileSize){
                byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), 0, remoteFileSize, callback);
                ResponseWriter.writeFile(content, fileInfo.getName(), fileInfo.getType(), true);
                return;
            }

            if((cycle + 1) * chunkFileSize < remoteFileInfo.getFileSize()){
                byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), offset, downloadFileSize, callback);
                boolean ifDownload = ResponseWriter.writeFile(content, fileInfo.getName(), fileInfo.getType(), false);
                if(!ifDownload) return;
            }else{
                downloadFileSize = remoteFileInfo.getFileSize() - (cycle * chunkFileSize);
                byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), offset, downloadFileSize, callback);
                boolean ifDownload = ResponseWriter.writeFile(content, fileInfo.getName(), fileInfo.getType(), true);
                if(!ifDownload) return;
            }
            offset = offset + chunkFileSize; //偏移量改变
        }

    }



}
