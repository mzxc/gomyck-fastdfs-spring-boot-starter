package com.gomyck.fastdfs.starter.common;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.CkFile;
import com.gomyck.util.CkId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件存储工具类
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/4/9
 */
@Slf4j
public class FDFSUtil {

    /**
     * 获取文件信息
     * @param us 上传 service
     * @param fileMd5 文件摘要
     * @return 文件信息
     */
    public static CkFileInfo getFileInfo(UploadService us, String fileMd5) {
        CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
        if (fileInfo == null) {
            log.error("从数据库查询文件信息出错, 文件MD5: {}", fileMd5);
            throw new FileNotFoundException("数据列表中不存在该文件");
        }
        return fileInfo;
    }

    public static FileInfo getFileInfoRemote(FastFileStorageClient ffsc, CkFileInfo fileInfo) {
        FileInfo remoteFileInfo;
        try {
            remoteFileInfo = ffsc.queryFileInfo(fileInfo.getGroup(), fileInfo.getUploadPath());
            if (remoteFileInfo == null) throw new FileNotFoundException("文件服务器中不存在该文件");
        } catch (Exception e) {
            log.error("从文件服务器查询文件信息出错, 分组: {}, 路径: {}", fileInfo.getGroup(), fileInfo.getUploadPath());
            throw new FileNotFoundException("文件服务器中不存在该文件");
        }
        return remoteFileInfo;
    }

    // 处理重名
    public static void resolveDuplicate(ZipOutputStream zos, String zipName, ZipEntry zipEntry) throws IOException {
        try {
            zos.putNextEntry(new ZipEntry(zipEntry));
        } catch (Exception e) {
            zipEntry = new ZipEntry(CkFile.getFileNameAndSuffix(zipName)[0] + "(" + CkId.getUUID() + ")" + "." + CkFile.getFileNameAndSuffix(zipName)[1]);
            zos.putNextEntry(new ZipEntry(zipEntry));
        }
    }

}
