package com.gomyck.fastdfs.starter.controller;

import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.common.FileNotFoundException;
import com.gomyck.fastdfs.starter.common.IllegalParameterException;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.BatchDownLoadParameter;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.ResponseWriter;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
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
     * 文件下载
     *
     * 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param fileMd5 文件摘要信息
     *
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
     * 文件批量下载
     *
     * 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param fileMd5s  文件摘要集合, 使用逗号分隔即可注入
     *
     */
    @GetMapping("batchDownloadFile")
    @ResponseBody
    public void simpleBatchDownload(String[] fileMd5s) {
        List<BatchDownLoadParameter> list = new ArrayList<>();
        Stream.of(fileMd5s).forEach(e -> {
            BatchDownLoadParameter bdl = new BatchDownLoadParameter();
            bdl.setFileMd5(e);
            list.add(bdl);
        });
        simpleBatchDownloadHasGroup(list);
    }


    /**
     * 文件批量下载, 增加了在压缩包中的存储结构, 具体请看入参实体
     *
     * 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param downloadInfo 附加了目录信息
     *
     */
    @GetMapping("batchDownloadFileHasGroup")
    public void simpleBatchDownloadHasGroup(List<BatchDownLoadParameter> downloadInfo) {
        if (downloadInfo == null || downloadInfo.size() < 1) throw new IllegalParameterException("非法的参数");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(outputStream);
            for (BatchDownLoadParameter bdl : downloadInfo) {
                CkFileInfo fileInfo = us.getFileByMessageDigest(bdl.getFileMd5());
                if (fileInfo == null) continue;
                DownloadByteArray callback = new DownloadByteArray();
                byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), callback);
                String zipName = bdl.getZipSrc() + (StringJudge.isNull(bdl.getFileName()) ? fileInfo.getName() : bdl.getFileName());
                ZipEntry zipEntry = new ZipEntry(zipName);
                zos.putNextEntry(zipEntry);
                zos.write(content);
                zos.closeEntry();
            }
            zos.finish();
            zos.close();
            byte[] bytes = outputStream.toByteArray();
            ResponseWriter.writeFile(bytes, "归档", ResponseWriter.ContextType.ZIP);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
