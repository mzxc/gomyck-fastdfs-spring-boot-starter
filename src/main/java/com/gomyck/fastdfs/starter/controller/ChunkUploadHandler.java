

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
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/upload/chunkUpload")
public class ChunkUploadHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Value("${spring.servlet.multipart.max-file-size: 1MB}")
    private String maxSize;

    @Autowired
    FileServerProfile fsp;

    @Autowired(required = false)
    UploadService us;

    @Autowired
    FileLock fl;


    //获取配置
    @RequestMapping("/config")
    @ResponseBody
    public R config() {
        Map<String, Object> stringObjectMap = ParaUtils.initParams();
        stringObjectMap.put("maxFileSize", maxSize);
        stringObjectMap.put("chunkSize", fsp.getChunkSize());
        stringObjectMap.put("fileServerUrl", fsp.getFileServerURI());
        return R.ok(stringObjectMap);
    }

    @PostMapping("/uploadFile")
    @ResponseBody
    public R uploadFile(CkFileInfo fileInfo, HttpServletRequest request) {
        ServiceCheck.uploadServiceCheck(us);
        boolean ifHasLock = false;
        String fileLock = Constant.FILE_LOCK + fileInfo.getFileMd5();
        if (StringJudge.isNull(fileInfo.getChunk())) fileInfo.setChunk("0");
        if (StringJudge.isNull(fileInfo.getChunks())) fileInfo.setChunks("0");
        CkFileInfo fileUploadStatus = us.getFileUploadStatus(fileInfo.getFileMd5());
        //设置文件分组
        if(fileUploadStatus != null){ //如果历史文件不为空, 把当前的分组设置为原来的分组, 防止前端传过来的分组与历史不符
            fileInfo.setGroup(fileUploadStatus.getGroup());
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
            List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
            Integer hasUploadChunk = 0; //查询当前文件存储到第几块了
            if(fileUploadStatus != null){
                String chunk = fileUploadStatus.getChunk();
                if(StringJudge.notNull(chunk)){
                    hasUploadChunk = Integer.parseInt(chunk);
                }
            }else{
                fileUploadStatus = new CkFileInfo();
            }
            Integer currentChunk = Integer.parseInt(fileInfo.getChunk());
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
                            path = appendFileStorageClient.uploadAppenderFile(fileInfo.getGroup(), file.getInputStream(), file.getSize(), FileUtils.extName(fileInfo.getName()));
                            if (path == null) {
                                return R.error(R._500, "文件服务器未返回存储路径, 请联系管理员");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return R.error(R._500, "上传远程服务器文件出错" + e.getMessage());
                        }
                        fileInfo.setGroup(fileInfo.getGroup());
                        fileInfo.setUploadPath(path.getPath());
                    } else {
                        fileUploadStatus = us.getFileUploadStatus(fileInfo.getFileMd5());
                        try {
                            //appendFileStorageClient.modifyFile(fileUploadStatus.getGroup(), fileUploadStatus.getUploadPath(), file.getInputStream(), file.getSize(), (hasUploadChunk + 1) * fileInfo.getChunkSize());
                            //todo 修复丢失字节的 BUG
                            appendFileStorageClient.appendFile(fileUploadStatus.getGroup(), fileUploadStatus.getUploadPath(), file.getInputStream(), file.getSize());
                        } catch (Exception e) {
                            return R.error(R._500, "续传文件出错" + e.getMessage());
                        }
                    }
                    BeanUtils.copyProperties(fileInfo, fileUploadStatus); //把本次传入的参数copy到历史数据中, 然后更新
                    us.saveFileUploadStatus(fileUploadStatus);
                    int allChunks = Integer.parseInt(fileInfo.getChunks());
                    if ((currentChunk + 1) == allChunks || allChunks == 0) {
                        fileUploadStatus.setUploadTime(DateUtils.now2Str(DateUtils.DUF.CN_DATETIME_FORMAT));
                        us.saveUploadInfo(fileUploadStatus);
                        us.delFileUploadStatus(fileUploadStatus.getFileMd5());
                    }
                } catch (Exception e) {
                    return R.error(R._500, "上传错误 " + e.getMessage());
                }
                break;
            }
        } finally {
            if (ifHasLock) fl.delLock(fileLock);
        }
        return R.ok(fileUploadStatus);
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
        List<CkFileInfo> fileInfos = us.selectCompleteFileInfo();
        if (fileInfos != null) {
            for (CkFileInfo e : fileInfos) {
                if (fileMd5.equals(e.getFileMd5())) {
                    return R.ok(R._302, e);
                }
            }
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
