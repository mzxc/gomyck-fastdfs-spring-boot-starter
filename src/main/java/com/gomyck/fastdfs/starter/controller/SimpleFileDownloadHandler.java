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

import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.common.DownloadFileNumException;
import com.gomyck.fastdfs.starter.common.FDFSUtil;
import com.gomyck.fastdfs.starter.common.IllegalParameterException;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.BatchDownLoadParameter;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.fastdfs.starter.profile.FileServerProfile;
import com.gomyck.util.CkContentType;
import com.gomyck.util.FileUtil;
import com.gomyck.util.ResponseWriter;
import com.gomyck.util.StringJudge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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
 * @version [1.0]
 * @since [2019-07-30]
 */
@Controller
@RequestMapping("download/simpleDownload")
public class SimpleFileDownloadHandler {

    Logger logger = LoggerFactory.getLogger(SimpleFileDownloadHandler.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
    }

    @Autowired
    FastFileStorageClient ffsc;

    @Autowired
    UploadService us;

    @Autowired
    FileServerProfile profile;

    /**
     * 如果不使用当前requestMapping作为下载入口, 请在业务代码中, 注入该类实例, 调用本方法即可
     *
     * @param fileMd5 文件摘要
     * @param fileName 文件名
     * @param thumbFlag 是否下载的是缩略图
     */
    @GetMapping("downloadFile")
    @ResponseBody
    public void simpleDownload(String fileMd5, String fileName, String thumbFlag) {
        CkFileInfo fileInfo = FDFSUtil.getFileInfo(us, fileMd5);
        // 如果自定义文件名 则替换
        if(StringJudge.notNull(fileName)) fileInfo.setName(FileUtil.getFileNameAndSuffix(fileName)[0] + "." + FileUtil.getFileNameAndSuffix(fileInfo.getName())[1]);
        // 如果是下载略缩图 则替换下载路径
        if(StringJudge.notNull(thumbFlag, fileInfo.getThumbImgPath()) && thumbFlag.equals("1")) fileInfo.setUploadPath(fileInfo.getThumbImgPath());
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
        if (fileMd5s.length > profile.getMaxDownloadFileNum()) throw new DownloadFileNumException("下载文件数量过多");
        BatchDownLoadParameter bdlp = new BatchDownLoadParameter();
        List<BatchDownLoadParameter.FileBatchDownload> list = new ArrayList<>();
        Stream.of(fileMd5s).forEach(e -> {
            BatchDownLoadParameter.FileBatchDownload bdl = new BatchDownLoadParameter.FileBatchDownload();
            bdl.setFileMd5(e);
            list.add(bdl);
        });
        bdlp.setFiles(list);
        simpleBatchDownloadHasGroup(bdlp);
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
    @GetMapping("batchDownloadFileHasGroup")
    @ResponseBody
    public void simpleBatchDownloadHasGroup(BatchDownLoadParameter downloadInfo) {
        if (downloadInfo == null || downloadInfo.getFiles().size() < 1) throw new IllegalParameterException("非法的参数");
        if (downloadInfo.getFiles().size() > profile.getMaxDownloadFileNum()) throw new DownloadFileNumException("下载文件数量过多");
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(outputStream);
            for (BatchDownLoadParameter.FileBatchDownload bdl : downloadInfo.getFiles()) {
                CkFileInfo fileInfo = us.getFileByMessageDigest(bdl.getFileMd5());
                if (fileInfo == null) continue;
                DownloadByteArray callback = new DownloadByteArray();
                byte[] content = ffsc.downloadFile(fileInfo.getGroup(), fileInfo.getUploadPath(), callback);
                String zipName = bdl.getZipSrc() + (StringJudge.isNull(bdl.getFileName()) ? fileInfo.getName() : bdl.getFileName());
                ZipEntry zipEntry = new ZipEntry(zipName);
                FDFSUtil.resolveDuplicate(zos, zipName, zipEntry);
                zos.write(content);
                zos.closeEntry();
            }
            zos.finish();
            zos.close();
            byte[] bytes = outputStream.toByteArray();
            ResponseWriter.writeFile(bytes, downloadInfo.getZipFileName() + ".zip", CkContentType.ZIP);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
