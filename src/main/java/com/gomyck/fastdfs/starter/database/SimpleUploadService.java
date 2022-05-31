

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
import java.util.Map;

/**
 * 简单文件上传 service
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * | blog: https://blog.gomyck.com |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/6/28
 */
public class SimpleUploadService implements UploadService {

    @Autowired
    RedisCache rs;

    /**
     * 保存文件信息
     *
     * @param fileInfo 文件信息
     *
     * @return 是否成功
     */
    @Override
    public R saveUploadInfo(CkFileInfo fileInfo) {
        //持久化上传完成文件,也可以存储在mysql中
        try {
            rs.startDoIt();
            Boolean r = rs.oneTime(e -> {
                String _fileInfo = JSONObject.toJSONString(fileInfo);
                e.hset(Constant.COMPLETED_MAP, fileInfo.getFileMd5(), _fileInfo);
                return true;
            });
            return R.unKnow(r);
        } finally {
            rs.finishDoIt();
        }
    }

    /**
     * 查询已完成文件信息
     *
     * @param start 开始位置
     * @param end   结束位置
     *
     * @return 文件列表
     */
    @Override
    public List<CkFileInfo> selectCompleteFileInfo(Long start, Long end) {
        Map<String, String> fileList;
        try {
            rs.startDoIt();
            fileList = rs.hGetAll(Constant.COMPLETED_MAP);
        } finally {
            rs.finishDoIt();
        }
        List<CkFileInfo> result = new ArrayList<>();
        if(fileList == null) return null;
        for(String key : fileList.keySet()){
            result.add(JSONObject.parseObject(fileList.get(key), CkFileInfo.class));
        }
        return result;
    }

    /**
     * 删除文件
     *
     * @param fileInfo 文件信息
     *
     * @return 是否成功
     */
    @Override
    public R delFile(CkFileInfo fileInfo) {
        try {
            rs.startDoIt();
            Boolean r = rs.oneTime(e -> {
                e.hdel(Constant.COMPLETED_MAP, fileInfo.getFileMd5());
                return true;
            });
            return R.unKnow(r);
        } finally {
            rs.finishDoIt();
        }
    }

    @Override
    public R delExpireStatus(CkFileInfo messageDigest, boolean completeStatus) {
        try {
            rs.startDoIt();
            Boolean r = rs.oneTime(e -> {
                if (completeStatus) {
                    e.hdel(Constant.COMPLETED_MAP, messageDigest.getFileMd5());
                    messageDigest.setExpireTime(null);
                    String _fileInfo = JSONObject.toJSONString(messageDigest);
                    e.hset(Constant.COMPLETED_MAP, messageDigest.getFileMd5(), _fileInfo);
                } else {
                    e.del(Constant.FILE_INFO + messageDigest.getFileMd5());
                    messageDigest.setExpireTime(null);
                    e.set(Constant.FILE_INFO + messageDigest.getFileMd5(), JSONObject.toJSONString(messageDigest));
                }
                return true;
            });
            return R.unKnow(r);
        } finally {
            rs.finishDoIt();
        }
    }

    @Override
    public R saveFileUploadStatus(CkFileInfo fileInfo) {
        try {
            rs.startDoIt();
            rs.set(Constant.FILE_INFO + fileInfo.getFileMd5(), JSONObject.toJSONString(fileInfo));
        } finally {
            rs.finishDoIt();
        }
        return R.ok();
    }

    @Override
    public CkFileInfo getFileUploadStatus(String fileMd5) {
        String string;
        try {
            rs.startDoIt();
            string = rs.get(Constant.FILE_INFO + fileMd5);
        } finally {
            rs.finishDoIt();
        }
        if(StringJudge.isNull(string)) return null;
        return JSONObject.parseObject(string, CkFileInfo.class);
    }

    @Override
    public R delFileUploadStatus(String fileMd5) {
        try {
            rs.startDoIt();
            rs.delKey(Constant.FILE_INFO + fileMd5);
        } finally {
            rs.finishDoIt();
        }
        return R.ok();
    }

    @Override
    public CkFileInfo getFileByMessageDigest(String fileMd5) {
        String fileInfo;
        try {
            rs.startDoIt();
            fileInfo = rs.hGet(Constant.COMPLETED_MAP, fileMd5);
            if(StringJudge.isNull(fileInfo)) return null;
        } finally {
            rs.finishDoIt();
        }
        return JSONObject.parseObject(fileInfo, CkFileInfo.class);
    }
}
