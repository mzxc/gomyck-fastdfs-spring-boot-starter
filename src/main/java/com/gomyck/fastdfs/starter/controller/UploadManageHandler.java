

package com.gomyck.fastdfs.starter.controller;

import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.gomyck.fastdfs.starter.database.ServiceCheck;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.FileInfo;
import com.gomyck.fastdfs.starter.profile.FileServerProfile;
import com.gomyck.util.ParaUtils;
import com.gomyck.util.R;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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
        Map<String, Object> stringObjectMap = ParaUtils.initParams();
        stringObjectMap.put("fileServerUrl", fsp.getFileServerURI());
        stringObjectMap.put("fileList", us.selectCompleteFileInfo());
        return R.ok(stringObjectMap);
    }

    @PostMapping("/delFile")
    @ResponseBody
    public R delFile(String fileMd5) {
        ServiceCheck.uploadServiceCheck(us);
        List<FileInfo> fileInfos = us.selectCompleteFileInfo();
        if (fileInfos == null) {
            return R.error(R._500, "文件服务器不存在该文件");
        }
        FileInfo fileInfo = new FileInfo();
        for (FileInfo e : fileInfos) {
            if (fileMd5.equals(e.getFileMd5())) {
                fileInfo = e;
                break;
            }
        }
        if(StringJudge.isNull(fileInfo.getUploadPath())){
            return R.error(R._500, "文件服务器不存在该文件");
        }
        try{
            storageClient.deleteFile(fileInfo.getGroup(), fileInfo.getUploadPath().replace(fileInfo.getGroup() + "/", ""));
        }catch (Exception e){
            e.printStackTrace();
            return R.error(R._500, e.getMessage());
        }
        us.delFile(fileInfo);
        return R.ok();
    }


}

