

package com.gomyck.fastdfs.starter.database;

import com.alibaba.fastjson.JSONObject;
import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.fastdfs.starter.common.Constant;
import com.gomyck.fastdfs.starter.database.entity.FileInfo;
import com.gomyck.util.R;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 郝洋 QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-24]
 */
public class SimpleUploadService implements UploadService {

    @Autowired
    RedisCache rs;

    @Override
    public R saveUploadInfo(FileInfo fileInfo) {
        //持久化上传完成文件,也可以存储在mysql中
        rs.startDoIt();
        rs.lpush(Constant.COMPLETED_LIST, JSONObject.toJSONString(fileInfo));
        rs.finishDoIt();
        return null;
    }

    @Override
    public List<FileInfo> selectCompleteFileInfo() {
        rs.startDoIt();
        List<String> fileList = rs.lrange(Constant.COMPLETED_LIST, 0, -1);
        rs.finishDoIt();
        List<FileInfo> result = new ArrayList<>();
        if(fileList == null) return null;
        for(String fileInfo : fileList){
            result.add(JSONObject.parseObject(fileInfo, FileInfo.class));
        }

        return result;
    }

    @Override
    public R delFile(FileInfo fileInfo) {
        rs.startDoIt();
        rs.delListNode(Constant.COMPLETED_LIST, 1, JSONObject.toJSONString(fileInfo));
        rs.finishDoIt();
        return R.ok();
    }

    @Override
    public R saveFileUploadStatus(FileInfo fileInfo) {
        rs.startDoIt();
        rs.cache(Constant.FILE_INFO + fileInfo.getFileMd5(), JSONObject.toJSONString(fileInfo));
        rs.finishDoIt();
        return R.ok();
    }

    @Override
    public FileInfo getFileUploadStatus(String fileMd5) {
        rs.startDoIt();
        String string = rs.get(Constant.FILE_INFO + fileMd5);
        rs.finishDoIt();
        if(StringJudge.isNull(string)) return null;
        return JSONObject.parseObject(string, FileInfo.class);
    }

    @Override
    public R delFileUploadStatus(String fileMd5) {
        rs.startDoIt();
        rs.delKey(Constant.FILE_INFO + fileMd5);
        rs.finishDoIt();
        return R.ok();
    }
}
