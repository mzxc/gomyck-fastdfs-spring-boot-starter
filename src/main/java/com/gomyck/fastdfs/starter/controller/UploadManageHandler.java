

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

import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.gomyck.fastdfs.starter.database.ServiceCheck;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.fastdfs.starter.profile.FileServerProfile;
import com.gomyck.util.ParamUtil;
import com.gomyck.util.R;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Controller
@RequestMapping("upload/manage")
public class UploadManageHandler {

    @Autowired
    private AppendFileStorageClient storageClient;

    @Autowired
    FileServerProfile fsp;

    @Autowired(required = false)
    UploadService us;

    @RequestMapping("/list")
    @ResponseBody
    public R uploadListNoPage() {
        ServiceCheck.uploadServiceCheck(us);
        Map<String, Object> stringObjectMap = ParamUtil.initParams();
        stringObjectMap.put("fileServerUrl", fsp.getFileServerURI());
        stringObjectMap.put("fileList", us.selectCompleteFileInfo());
        return R.ok(stringObjectMap);
    }

    @PostMapping("/delFile")
    @ResponseBody
    public R delFile(String fileMd5) {
        ServiceCheck.uploadServiceCheck(us);
        List<CkFileInfo> fileInfos = us.selectCompleteFileInfo();
        if (fileInfos == null) {
            return R.error(R._500, "文件服务器不存在该文件");
        }
        CkFileInfo fileInfo = new CkFileInfo();
        for (CkFileInfo e : fileInfos) {
            if (fileMd5.equals(e.getFileMd5())) {
                fileInfo = e;
                break;
            }
        }
        if(StringJudge.isNull(fileInfo.getUploadPath())){
            return R.error(R._500, "文件服务器不存在该文件");
        }
        try{
            storageClient.deleteFile(fileInfo.getGroup(), fileInfo.getUploadPath().replace(fileInfo.getGroup() + File.separator, ""));
        }catch (Exception e){
            e.printStackTrace();
            return R.error(R._500, e.getMessage());
        }
        us.delFile(fileInfo);
        return R.ok();
    }


    @PostMapping(value = "/batchDelFile")
    @ResponseBody
    public R batchDelFile(@RequestBody String fileMd5s) {
        ServiceCheck.uploadServiceCheck(us);
        List<CkFileInfo> fileInfos = us.selectCompleteFileInfo();
        if (fileInfos == null) {
            return R.error(R._500, "文件服务器不存在该文件");
        }
        Stream.of(fileMd5s.split(",")).forEach(fileMd5 -> {
            CkFileInfo fileInfo= null;
            for (CkFileInfo e : fileInfos) {
                if (fileMd5.equals(e.getFileMd5())) {
                    fileInfo = e;
                    break;
                }
            }
            if(fileInfo == null) fileInfo = us.getFileUploadStatus(fileMd5);
            if(fileInfo == null) return;
            if(StringJudge.isNull(fileInfo.getUploadPath())) return;
            try{
                storageClient.deleteFile(fileInfo.getGroup(), fileInfo.getUploadPath().replace(fileInfo.getGroup() + File.separator, ""));
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
            us.delFile(fileInfo);
            us.delFileUploadStatus(fileMd5);
        });
        return R.ok();
    }

}

