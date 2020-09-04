

package com.gomyck.fastdfs.starter.database;

import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.R;

import java.util.List;

/**
 * @author gomyck QQ:474798383
 * @version [版本号/1.0]
 * @since [2019-07-24]
 */
public interface UploadService {

    /**
     * 保存已完成上传的文件信息(向已完成的文件列表中加入)
     *
     * @param fileInfo 文件信息
     *
     * @return R 消息实体
     */
    R saveUploadInfo(CkFileInfo fileInfo);

    /**
     * 查询已上传完成的文件列表(查询已完成上传的文件列表)
     *
     * @param start 开始位置
     * @param end   结束位置
     *
     * @return ListCkFileInfo 文件列表
     */
    List<CkFileInfo> selectCompleteFileInfo(int start, int end);

    /**
     * 删除文件(单个, 从已完成列表中删除)
     *
     * @param fileInfo 文件信息
     *
     * @return R 消息实体
     */
    R delFile(CkFileInfo fileInfo);


    /**
     * 设置文件过期时间为从不
     *
     * @param fileInfo 文件信息
     *
     * @return R 消息实体
     */
    R delExpireStatus(CkFileInfo fileInfo, boolean completeStatus);

    /**
     * 根据摘要, 获取文件信息(已完成列表中获取)
     *
     * @param fileMd5 摘要
     *
     * @return CkFileInfo 文件信息
     */
    CkFileInfo getFileByMessageDigest(String fileMd5);


    //--------------以下为上传辅助接口, 为了保存单个文件上传临时状态-----------

    /**
     * 保存单个文件上传状态
     *
     * @param fileInfo 文件信息
     *
     * @return R 消息实体
     */
    R saveFileUploadStatus(CkFileInfo fileInfo);

    /**
     * 删除单个文件上传状态
     *
     * @param fileMd5 摘要
     *
     * @return R 消息实体
     */
    R delFileUploadStatus(String fileMd5);

    /**
     * 获取文件上传状态
     *
     * @param fileMd5 摘要
     *
     * @return CkFileInfo 文件信息
     */
    CkFileInfo getFileUploadStatus(String fileMd5);

}
