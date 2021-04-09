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
import com.gomyck.util.PageUtil;
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

    /**
     * 查询文件列表, 分页信息可以不传
     *
     * @param pageIndex list 起始位置
     * @param limit list 结束位置
     *
     * @return R 结果
     */
    @RequestMapping("/list")
    @ResponseBody
    public R uploadListByPage(Long pageIndex, Long limit) {
        ServiceCheck.uploadServiceCheck(us);
        if(pageIndex == null) pageIndex = 1L;
        if(limit == null) limit = -1L;
        Map<String, Object> stringObjectMap = ParamUtil.initParams();
        stringObjectMap.put("fileServerUrl", fsp.getFileServerURI());
        stringObjectMap.put("fileList", us.selectCompleteFileInfo(PageUtil.getStartOfPage(pageIndex, limit), PageUtil.getEndOfPage(pageIndex, limit)));
        return R.ok(stringObjectMap);
    }

    /**
     * 删除文件
     *
     * @param fileMd5 MD5
     * @return R 是否成功
     */
    @PostMapping("/delFile")
    @ResponseBody
    public R delFile(String fileMd5) {
        ServiceCheck.uploadServiceCheck(us);
        CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
        if (fileInfo == null || StringJudge.hasNull(fileInfo.getUploadPath())) {
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

    /**
     * 批量删除文件
     * @param fileMd5s MD5
     * @return R 是否成功
     */
    @PostMapping(value = "/batchDelFile")
    @ResponseBody
    public R batchDelFile(@RequestBody String fileMd5s) {
        ServiceCheck.uploadServiceCheck(us);
        Stream.of(fileMd5s.split(",")).forEach(fileMd5 -> {
            CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
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

    /**
     * 删除文件过期标记, 使文件永久有效(配合客户端传的expireTime一起使用, 如果不传expireTime, 则不需要调用此方法)
     *
     * @param fileMd5s 文件 MD5
     * @return R 是否成功
     */
    @PostMapping(value = "/delExpireStatus")
    @ResponseBody
    public R delExpireStatus(@RequestBody String fileMd5s) {
        StringBuffer noSuchMd5 = new StringBuffer();
        Stream.of(fileMd5s.split(",")).forEach(fileMd5 -> {
            boolean completeStatus = false;
            CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
            if(fileInfo == null) {
                fileInfo = us.getFileUploadStatus(fileMd5);
            }else{
                completeStatus = true;
            }
            if(fileInfo == null) {
                noSuchMd5.append("|").append(fileMd5).append("|");
                return;
            }
            us.delExpireStatus(fileInfo, completeStatus);
        });
        if(noSuchMd5.length() > 0){
            return R.error(R._500, "下述文件不存在: " + noSuchMd5);
        }else {
            return R.ok();
        }
    }

}

