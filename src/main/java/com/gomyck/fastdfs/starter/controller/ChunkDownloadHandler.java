package com.gomyck.fastdfs.starter.controller;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.common.FileNotFoundException;
import com.gomyck.fastdfs.starter.common.IllegalParameterException;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.BatchDownLoadParameter;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.ResponseWriter;
import com.gomyck.util.StringJudge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-28]
 */
@Controller
@RequestMapping("download/chunkDownload")
public class ChunkDownloadHandler {

    Logger logger = LoggerFactory.getLogger(ChunkDownloadHandler.class);


    @Autowired
    FastFileStorageClient ffsc;

    @Autowired
    UploadService us;

    @Value("${gomyck.fastdfs.download-chunk-size: 1000000}")
    private long chunkFileSize;

    /**
     * 文件下载 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param fileMd5 文件摘要信息
     *
     */
    @GetMapping("downloadFile")
    @ResponseBody
    public void chunkDownload(String fileMd5) {
        DownloadByteArray callback = new DownloadByteArray();
        CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
        if (fileInfo == null) {
            logger.error("从数据库查询文件信息出错, 文件MD5: {}", fileMd5);
            throw new FileNotFoundException("数据列表中不存在该文件");
        }
        FileInfo remoteFileInfo;
        try {
            remoteFileInfo = ffsc.queryFileInfo(fileInfo.getGroup(), fileInfo.getUploadPath());
            if (remoteFileInfo == null) throw new FileNotFoundException("文件服务器中不存在该文件");
        } catch (Exception e) {
            logger.error("从文件服务器查询文件信息出错, 分组: {}, 路径: {}", fileInfo.getGroup(), fileInfo.getUploadPath());
            throw new FileNotFoundException("文件服务器中不存在该文件");
        }
        long cycle = 0L;  //下载次数
        long offset = 0L; //当前偏移量
        long downloadFileSize = chunkFileSize; //当前实际要下载的块大小
        long remoteFileSize = remoteFileInfo.getFileSize(); //文件服务器存储的文件大小 (byte为单位)
        //todo 如果文件大小 小于分块大小, 一次性下载
        if (remoteFileSize <= chunkFileSize) {
            byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), 0, remoteFileSize, callback);
            ResponseWriter.writeFile(content, fileInfo.getName(), fileInfo.getType(), true);
            return;
        }
        for (; ; cycle = cycle + 1L) {
            if ((cycle + 1) * chunkFileSize < remoteFileSize) {
                byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), offset, downloadFileSize, callback);
                boolean ifDownload = ResponseWriter.writeFile(content, fileInfo.getName(), fileInfo.getType(), false);
                if (!ifDownload) return;
            } else {
                downloadFileSize = remoteFileSize - (cycle * chunkFileSize);
                byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), offset, downloadFileSize, callback);
                ResponseWriter.writeFile(content, fileInfo.getName(), fileInfo.getType(), true);
                return;
            }
            offset = offset + chunkFileSize; //偏移量改变
        }

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
    public void chunkDownload4Batch(String[] fileMd5s) {
        List<BatchDownLoadParameter> list = new ArrayList<>();
        Stream.of(fileMd5s).forEach(e -> {
            BatchDownLoadParameter bdl = new BatchDownLoadParameter();
            bdl.setFileMd5(e);
            list.add(bdl);
        });
        chunkDownload4BatchHasGroup(list);
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
    @ResponseBody
    public void chunkDownload4BatchHasGroup(List<BatchDownLoadParameter> downloadInfo) {
        if (downloadInfo == null || downloadInfo.size() < 1) throw new IllegalParameterException("非法的参数");
        HttpServletResponse response = ResponseWriter.getResponse();
        try {
            response.setHeader("Content-Disposition", "attachment; filename=" + ResponseWriter.fileNameWrapper("归档.zip"));
            response.setContentType(ResponseWriter.ContextType.ZIP.getTypeValue());
            ServletOutputStream outputStream = response.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(outputStream);
            for (BatchDownLoadParameter bdl : downloadInfo) {
                DownloadByteArray callback = new DownloadByteArray();
                CkFileInfo fileInfo = us.getFileByMessageDigest(bdl.getFileMd5());
                if (fileInfo == null) {
                    logger.error("从数据库查询文件信息出错, 文件MD5: {}", bdl.getFileMd5());
                    continue;
                }
                FileInfo remoteFileInfo;
                try {
                    remoteFileInfo = ffsc.queryFileInfo(fileInfo.getGroup(), fileInfo.getUploadPath());
                    if (remoteFileInfo == null) continue;
                } catch (Exception e) {
                    logger.error("从文件服务器查询文件信息出错, 分组: {}, 路径: {}", fileInfo.getGroup(), fileInfo.getUploadPath());
                    continue;
                }
                long cycle = 0L;  //下载次数
                long offset = 0L; //当前偏移量
                long downloadFileSize = chunkFileSize; //当前实际要下载的块大小
                long remoteFileSize = remoteFileInfo.getFileSize(); //文件服务器存储的文件大小 (byte为单位)
                //todo 如果文件大小 小于分块大小, 一次性下载
                String zipName = bdl.getZipSrc() + (StringJudge.isNull(bdl.getFileName()) ? fileInfo.getName() : bdl.getFileName());
                ZipEntry zipEntry = new ZipEntry(zipName);
                if (remoteFileSize <= chunkFileSize) {
                    byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), 0, remoteFileSize, callback);
                    zos.putNextEntry(new ZipEntry(zipEntry));
                    zos.write(content);
                    zos.flush();
                    zos.closeEntry();
                    continue;
                }

                zos.putNextEntry(new ZipEntry(zipEntry));
                for (; ; cycle = cycle + 1L) {
                    if ((cycle + 1) * chunkFileSize < remoteFileSize) {
                        byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), offset, downloadFileSize, callback);
                        zos.write(content);
                        zos.flush();
                    } else {
                        downloadFileSize = remoteFileSize - (cycle * chunkFileSize);
                        byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), offset, downloadFileSize, callback);
                        zos.write(content);
                        zos.flush();
                        zos.closeEntry();
                        break;
                    }
                    offset = offset + chunkFileSize; //偏移量改变
                }
            }
            zos.finish();
            zos.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

}
