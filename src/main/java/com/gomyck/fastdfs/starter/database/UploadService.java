

package com.gomyck.fastdfs.starter.database;

import com.gomyck.fastdfs.starter.database.entity.FileInfo;
import com.gomyck.util.R;

import java.util.List;

/**
 * @author 郝洋 QQ:474798383
 * @version [版本号/1.0]
 * @see [相关类/方法]
 * @since [2019-07-24]
 */
public interface UploadService {

    /**
     * 保存已完成上传的文件信息(向已完成的文件列表中加入)
     * @param fileInfo
     * @return
     */
    R saveUploadInfo(FileInfo fileInfo);

    /**
     * 查询已上传完成的文件列表(查询已完成上传的文件列表)
     * @return
     */
    List<FileInfo> selectCompleteFileInfo();

    /**
     * 删除文件(单个, 从已完成列表中删除)
     * @param messageDigest
     * @return
     */
    R delFile(FileInfo messageDigest);

    //--------------以下为上传辅助接口, 为了保存单个文件上传临时状态-----------

    /**
     * 保存单个文件上传状态
     * @param fileInfo 文件信息
     * @return
     */
    R saveFileUploadStatus(FileInfo fileInfo);

    /**
     * 删除单个文件上传状态
     * @param fileMd5
     * @return
     */
    R delFileUploadStatus(String fileMd5);

    /**
     * 获取文件上传状态
     * @param fileMd5
     * @return
     */
    FileInfo getFileUploadStatus(String fileMd5);

}
