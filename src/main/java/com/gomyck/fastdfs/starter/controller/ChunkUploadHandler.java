

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

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.domain.upload.FastImageFile;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.common.Constant;
import com.gomyck.fastdfs.starter.database.ServiceCheck;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.fastdfs.starter.lock.FileLock;
import com.gomyck.fastdfs.starter.profile.FileServerProfile;
import com.gomyck.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/upload/chunkUpload")
public class ChunkUploadHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private FastFileStorageClient simpleFileDownloadHandler;

    @Value("${spring.servlet.multipart.max-file-size: 1MB}")
    private String maxSize;

    @Autowired
    FileServerProfile fsp;

    @Autowired(required = false)
    UploadService us;

    @Autowired
    FileLock fl;

    private final static String FILE_PARAM_NAME = "file";

    //获取配置
    @GetMapping("/config")
    @ResponseBody
    public R config() {
        Map<String, Object> stringObjectMap = ParamUtil.initParams();
        stringObjectMap.put("maxFileSize", Long.parseLong(maxSize.replace("MB", "")) * 1024 * 1024);
        stringObjectMap.put("chunkSize", fsp.getChunkSize());
        stringObjectMap.put("fileServerUrl", fsp.getFileServerURI());
        return R.ok(stringObjectMap);
    }

    @PostMapping("/uploadFile")
    @ResponseBody
    public R uploadFile(CkFileInfo fileInfo, HttpServletRequest request) {
        ServiceCheck.uploadServiceCheck(us);
        R checkInfo = this.checkFile(fileInfo.getFileMd5());
        if(!checkInfo.isOk() || R._302 == checkInfo.getResCode()) return checkInfo;
        boolean ifHasLock = false;
        String fileLock = Constant.FILE_LOCK + fileInfo.getFileMd5();
        if (StringJudge.isNull(fileInfo.getChunk())) fileInfo.setChunk("0");
        if (StringJudge.isNull(fileInfo.getChunks())) fileInfo.setChunks("0");
        //todo 查询历史文件, 这个一般来说第一次查是没有的, 但是第二次续传一定有, 所以整体流程以这个对象操作为主
        CkFileInfo historyFileInfo = us.getFileUploadStatus(fileInfo.getFileMd5());
        //设置文件分组
        if(historyFileInfo != null){ //如果历史文件不为空, 把当前的分组设置为原来的分组, 防止前端传过来的分组与历史不符
            fileInfo.setGroup(historyFileInfo.getGroup());
        }else{
            if(StringJudge.isNull(fileInfo.getGroup())){ //如果前端传过来的分组是空, 则设置配置的分组
                fileInfo.setGroup(fsp.getGroupId());
            }
        }
        try {
            if(!fl.addLock(fileLock)){
                return R.error(R._500, "当前文件正在被上传");
            }else{
                ifHasLock = true;
            }
            List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles(FILE_PARAM_NAME);
            int hasUploadChunk = 0; //查询当前文件存储到第几块了
            if(historyFileInfo != null){
                String chunk = historyFileInfo.getChunk();
                if(StringJudge.notNull(chunk)){
                    hasUploadChunk = Integer.parseInt(chunk);
                }
            }else{
                historyFileInfo = new CkFileInfo();
            }
            int currentChunk = Integer.parseInt(fileInfo.getChunk());
            if (currentChunk < hasUploadChunk) {
                return R.error(R._500, "当前文件块已上传, 请重试");
            } else if (currentChunk > (hasUploadChunk + 1)) {
                return R.error(R._500, "非法的文件块, 请重试");
            }
            StorePath path;
            for (final MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                try {
                    if (currentChunk == 0) {
                        try {
                            path = appendFileStorageClient.uploadAppenderFile(fileInfo.getGroup(), file.getInputStream(), file.getSize(), FileUtil.getFileSuffixNameByFileName(fileInfo.getName()));
                            if (path == null) {
                                return R.error(R._500, "文件服务器未返回存储路径, 请联系管理员");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return R.error(R._500, "上传文件服务器文件出错" + e.getMessage());
                        }
                        fileInfo.setUploadTime(CkDateUtil.now2Str(CkDateUtil.DUF.CN_DATETIME_FORMAT));
                        fileInfo.setGroup(fileInfo.getGroup());
                        fileInfo.setUploadPath(path.getPath());
                    } else {
                        historyFileInfo = us.getFileUploadStatus(fileInfo.getFileMd5());
                        try {
                            //appendFileStorageClient.modifyFile(fileUploadStatus.getGroup(), fileUploadStatus.getUploadPath(), file.getInputStream(), file.getSize(), (hasUploadChunk + 1) * fileInfo.getChunkSize());
                            //todo 修复丢失字节的 BUG
                            appendFileStorageClient.appendFile(historyFileInfo.getGroup(), historyFileInfo.getUploadPath(), file.getInputStream(), file.getSize());
                        } catch (Exception e) {
                            return R.error(R._500, "续传文件出错" + e.getMessage());
                        }
                    }
                    BeanUtil.copyProperties(fileInfo, historyFileInfo); //把本次传入的参数copy到历史数据中, 然后更新
                    us.saveFileUploadStatus(historyFileInfo);
                    int allChunks = Integer.parseInt(fileInfo.getChunks());
                    if ((currentChunk + 1) == allChunks || allChunks == 0) {
                        historyFileInfo.setUploadTime(CkDateUtil.now2Str(CkDateUtil.DUF.CN_DATETIME_FORMAT));
                        generateThumbImg(historyFileInfo);
                        us.saveUploadInfo(historyFileInfo);
                        us.delFileUploadStatus(historyFileInfo.getFileMd5());
                    }
                } catch (Exception e) {
                    return R.error(R._500, "上传错误: " + e.getMessage());
                }
                break;
            }
        } finally {
            if (ifHasLock) fl.delLock(fileLock);
        }
        return R.ok(historyFileInfo);
    }

    /**
     * 生成缩略图
     *
     * @param historyFileInfo 上传的文件
     */
    private void generateThumbImg(CkFileInfo historyFileInfo) {
        if(historyFileInfo.isThumbFlag() && historyFileInfo.getType().contains("image")){
            try{
                DownloadByteArray callback = new DownloadByteArray();
                //todo 这里要下载一下, 因为主流程为断点续传, 图片与略缩图上传不支持断点续传, 所以要全部传完之后, 统一上传
                byte[] bytes = simpleFileDownloadHandler.downloadFile(historyFileInfo.getGroup(), historyFileInfo.getUploadPath(), callback);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                FastImageFile.Builder builder = new FastImageFile.Builder();
                builder.toGroup(historyFileInfo.getGroup());
                builder.withFile(byteArrayInputStream, historyFileInfo.getSize(), FileUtil.getFileSuffixNameByFileName(historyFileInfo.getName()));
                if(StringJudge.notNull(historyFileInfo.getThumbImgPercent()))  {
                    builder.withThumbImage(historyFileInfo.getThumbImgPercent());
                } else if(StringJudge.notNull(historyFileInfo.getThumbImgWidth(), historyFileInfo.getThumbImgHeight())) {
                    builder.withThumbImage(historyFileInfo.getThumbImgWidth(), historyFileInfo.getThumbImgHeight());
                } else {
                    builder.withThumbImage();
                }
                FastImageFile imgFile = builder.build();
                builder.withFile(byteArrayInputStream, historyFileInfo.getSize(), FileUtil.getFileSuffixNameByFileName(historyFileInfo.getName()));
                StorePath storePath = simpleFileDownloadHandler.uploadImage(imgFile);
                simpleFileDownloadHandler.deleteFile(storePath.getGroup(), storePath.getPath()); //todo 删除略缩图的原图
                historyFileInfo.setThumbImgPath(imgFile.getThumbImagePath(storePath.getPath()));
                log.info("thumb img path is: {}", historyFileInfo.getThumbImgPath());
            }catch (Exception imgError){
                //todo 一般来说, 图片格式不受支持则会报错
                log.error(imgError.toString());
            }
        }
    }


    @PostMapping("/checkFile")
    @ResponseBody
    public R checkFile(String fileMd5) {
        ServiceCheck.uploadServiceCheck(us);
        if (StringJudge.isNull(fileMd5)) return R.error(R._500, "fileMd5不能为空");
        String fileLock = Constant.FILE_LOCK + fileMd5;
        if(fl.ifLock(fileLock)){
            return R.error(R._500, "当前文件正在被上传, 请稍后再试");
        }
        CkFileInfo fileByMessageDigest = us.getFileByMessageDigest(fileMd5);
        if (fileByMessageDigest != null) {
            return R.ok(R._302, fileByMessageDigest);
        }
        CkFileInfo fileUploadStatus = us.getFileUploadStatus(fileMd5);
        if (fileUploadStatus != null && StringJudge.notNull(fileUploadStatus.getChunk())) {
            return R.ok(fileUploadStatus);
        } else {
            fileUploadStatus = new CkFileInfo();
            fileUploadStatus.setChunk("0");
            return R.ok(fileUploadStatus);
        }

    }
}
