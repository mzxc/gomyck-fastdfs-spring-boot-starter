package com.gomyck.fastdfs.starter.controller;

import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.common.FileNotFoundException;
import com.gomyck.fastdfs.starter.common.IllegalParameterException;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.ResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
     *
     * @return
     */
    @GetMapping("downloadFile")
    public void simpleDownload(String fileMd5) {
        CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
        if (fileInfo == null) throw new FileNotFoundException("数据列表中不存在该文件");
        DownloadByteArray callback = new DownloadByteArray();
        byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), callback);
        ResponseWriter.writeFile(content, fileInfo.getName(), fileInfo.getType(), true);
    }


    /**
     * 文件下载 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param fileMd5s 文件MD5使用逗号分开就可以注入
     *
     * @return
     */
    @GetMapping("batchDownloadFile")
    public void simpleBatchDownload(String[] fileMd5s) {
        if (fileMd5s == null || fileMd5s.length < 1) throw new IllegalParameterException("非法的参数");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(outputStream);
            for (String fileMd5 : fileMd5s) {
                CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
                if (fileInfo == null) continue;
                DownloadByteArray callback = new DownloadByteArray();
                byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), callback);
                zos.putNextEntry(new ZipEntry(fileInfo.getName()));
                zos.write(content);
                zos.closeEntry();
            }
            zos.finish();
            zos.close();
            byte[] bytes = outputStream.toByteArray();
            ResponseWriter.writeFile(bytes, "归档.zip", "application/octet-stream; charset=UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


}
