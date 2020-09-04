

package com.gomyck.fastdfs.starter.database;

import com.alibaba.fastjson.JSONObject;
import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.fastdfs.starter.common.Constant;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.R;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 *
 * @since [2019-07-24]
 */
public class SimpleUploadService implements UploadService {

    @Autowired
    RedisCache rs;

    @Override
    public R saveUploadInfo(CkFileInfo fileInfo) {
        //持久化上传完成文件,也可以存储在mysql中
        rs.startDoIt();
        rs.oneTime(e -> {
            String _fileInfo = JSONObject.toJSONString(fileInfo);
            e.lpush(Constant.COMPLETED_LIST, _fileInfo);
            e.hset(Constant.COMPLETED_MAP, fileInfo.getFileMd5(), _fileInfo);
            return true;
        });
        rs.finishDoIt();
        return null;
    }

    @Override
    public List<CkFileInfo> selectCompleteFileInfo(int start, int end) {
        rs.startDoIt();
        List<String> fileList = rs.lrange(Constant.COMPLETED_LIST, start, end);
        rs.finishDoIt();
        List<CkFileInfo> result = new ArrayList<>();
        if(fileList == null) return null;
        for(String fileInfo : fileList){
            result.add(JSONObject.parseObject(fileInfo, CkFileInfo.class));
        }
        return result;
    }

    @Override
    public R delFile(CkFileInfo fileInfo) {
        rs.startDoIt();
        rs.oneTime(e -> {
            e.lrem(Constant.COMPLETED_LIST, 1, JSONObject.toJSONString(fileInfo));
            e.hdel(Constant.COMPLETED_MAP, fileInfo.getFileMd5());
            return true;
        });
        rs.finishDoIt();
        return R.ok();
    }

    @Override
    public R delExpireStatus(CkFileInfo messageDigest, boolean completeStatus) {
        rs.startDoIt();
        rs.oneTime(e -> {
            if(completeStatus){
                e.lrem(Constant.COMPLETED_LIST, 1, JSONObject.toJSONString(messageDigest));
                e.hdel(Constant.COMPLETED_MAP, messageDigest.getFileMd5());
                messageDigest.setExpireTime(null);
                String _fileInfo = JSONObject.toJSONString(messageDigest);
                e.lpush(Constant.COMPLETED_LIST, _fileInfo);
                e.hset(Constant.COMPLETED_MAP, messageDigest.getFileMd5(), _fileInfo);
            }else{
                e.del(Constant.FILE_INFO + messageDigest.getFileMd5());
                messageDigest.setExpireTime(null);
                e.set(Constant.FILE_INFO + messageDigest.getFileMd5(), JSONObject.toJSONString(messageDigest));
            }
            return true;
        });
        rs.finishDoIt();
        return R.ok();
    }

    @Override
    public R saveFileUploadStatus(CkFileInfo fileInfo) {
        rs.startDoIt();
        rs.set(Constant.FILE_INFO + fileInfo.getFileMd5(), JSONObject.toJSONString(fileInfo));
        rs.finishDoIt();
        return R.ok();
    }

    @Override
    public CkFileInfo getFileUploadStatus(String fileMd5) {
        rs.startDoIt();
        String string = rs.get(Constant.FILE_INFO + fileMd5);
        rs.finishDoIt();
        if(StringJudge.isNull(string)) return null;
        return JSONObject.parseObject(string, CkFileInfo.class);
    }

    @Override
    public R delFileUploadStatus(String fileMd5) {
        rs.startDoIt();
        rs.delKey(Constant.FILE_INFO + fileMd5);
        rs.finishDoIt();
        return R.ok();
    }

    @Override
    public CkFileInfo getFileByMessageDigest(String fileMd5) {
        rs.startDoIt();
        String fileInfo = rs.hGet(Constant.COMPLETED_MAP, fileMd5);
        if(StringJudge.isNull(fileInfo)) return null;
        rs.finishDoIt();
        return JSONObject.parseObject(fileInfo, CkFileInfo.class);
    }
}
