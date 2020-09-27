/*
 * Copyright (c) 2019 gomyck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gomyck.fastdfs.starter.controller;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.common.DownloadFileNumException;
import com.gomyck.fastdfs.starter.common.FDFSUtil;
import com.gomyck.fastdfs.starter.common.FileNotFoundException;
import com.gomyck.fastdfs.starter.common.IllegalParameterException;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.BatchDownLoadParameter;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.fastdfs.starter.profile.FileServerProfile;
import com.gomyck.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author gomyck QQ:474798383
 * @version [1.0]
 * @since [2019-07-28]
 */
@Controller
@RequestMapping("download/chunkDownload")
public class ChunkDownloadHandler {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(1000);
    }

    Logger logger = LoggerFactory.getLogger(ChunkDownloadHandler.class);


    @Autowired
    FastFileStorageClient ffsc;

    @Autowired
    UploadService us;

    @Autowired
    FileServerProfile profile;

    @Value("${gomyck.fastdfs.download-chunk-size: 1000000}")
    private long chunkFileSize;

    private static final String THUMB_FLAG_TRUE = "1";

    /**
     * 文件下载 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param fileMd5 文件摘要信息
     * @param fileName 下载文件名 非必传
     * @param thumbFlag 是否是下载略缩图 非必传  1为下载略缩图, 如果略缩图没有, 就返回原图 (仅图片有此选项, 业务侧使用时自行判断)
     *
     */
    @GetMapping("downloadFile")
    @ResponseBody
    public void chunkDownload(String fileMd5, String fileName, String thumbFlag) {
        CkFileInfo fileInfo = FDFSUtil.getFileInfo(us, fileMd5);
        // 如果自定义文件名 则替换
        if(StringJudge.notNull(fileName)) fileInfo.setName(FileUtil.getFileNameAndSuffix(fileName)[0] + "." + FileUtil.getFileNameAndSuffix(fileInfo.getName())[1]);
        // 如果是下载略缩图 则替换下载路径
        if(StringJudge.notNull(thumbFlag, fileInfo.getThumbImgPath()) && thumbFlag.equals(THUMB_FLAG_TRUE)) fileInfo.setUploadPath(fileInfo.getThumbImgPath());
        FileInfo remoteFileInfo = FDFSUtil.getFileInfoRemote(ffsc, fileInfo);
        long cycle = 0L;  //下载次数
        long offset = 0L; //当前偏移量
        long downloadFileSize = chunkFileSize; //当前实际要下载的块大小
        long remoteFileSize = remoteFileInfo.getFileSize(); //文件服务器存储的文件大小 (byte为单位)
        DownloadByteArray callback = new DownloadByteArray();
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
        if (fileMd5s.length > profile.getMaxDownloadFileNum()) throw new DownloadFileNumException("下载文件数量过多");
        BatchDownLoadParameter bdlp = new BatchDownLoadParameter();
        ArrayList<BatchDownLoadParameter.FileBatchDownload> list = new ArrayList<>();
        Stream.of(fileMd5s).forEach(e -> {
            BatchDownLoadParameter.FileBatchDownload bdl = new BatchDownLoadParameter.FileBatchDownload();
            bdl.setFileMd5(e);
            list.add(bdl);
        });
        bdlp.setFiles(list);
        batchDownloadFileHasGroup(bdlp);
    }


    /**
     * 文件批量下载, 增加了在压缩包中的存储结构, 具体请看入参实体
     *
     * 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param downloadInfo 附加了目录信息
     *                     fileMd5: 文件摘要
     *                     zipSrc: 文件在压缩包中的路径 exp: /demo/xxx/gomyck/  前后的 / 不可少
     *                     fileName: 文件名称, 如果为空, 则取文件服务器内的文件名
     */
    @PostMapping("batchDownloadFileHasGroup")
    @ResponseBody
    public void batchDownloadFileHasGroup(BatchDownLoadParameter downloadInfo) {
        if (downloadInfo == null || downloadInfo.getFiles().size() < 1) throw new IllegalParameterException("非法的参数");
        if (downloadInfo.getFiles().size() > profile.getMaxDownloadFileNum()) throw new DownloadFileNumException("下载文件数量过多");
        HttpServletResponse response = ResponseWriter.getResponse();
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + ResponseWriter.fileNameWrapper(downloadInfo.getZipFileName() + ".zip\""));
            response.setContentType(CkContentType.ZIP.getTypeValue());
            ServletOutputStream outputStream = response.getOutputStream();
            ZipOutputStream zos = new ZipOutputStream(outputStream);
            for (BatchDownLoadParameter.FileBatchDownload bdl : downloadInfo.getFiles()) {
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
                    FDFSUtil.resolveDuplicate(zos, zipName, zipEntry);
                    zos.write(content);
                    zos.flush();
                    zos.closeEntry();
                    continue;
                }
                FDFSUtil.resolveDuplicate(zos, zipName, zipEntry);
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
