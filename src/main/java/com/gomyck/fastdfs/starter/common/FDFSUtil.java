package com.gomyck.fastdfs.starter.common;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.FileUtil;
import com.gomyck.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件存储工具类
 *
 * @author gomyck
 * @version 1.0.0
 */
public class FDFSUtil {

    private static Logger logger = LoggerFactory.getLogger(FDFSUtil.class);

    public static CkFileInfo getFileInfo(UploadService us, String fileMd5) {
        CkFileInfo fileInfo = us.getFileByMessageDigest(fileMd5);
        if (fileInfo == null) {
            logger.error("从数据库查询文件信息出错, 文件MD5: {}", fileMd5);
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
            logger.error("从文件服务器查询文件信息出错, 分组: {}, 路径: {}", fileInfo.getGroup(), fileInfo.getUploadPath());
            throw new FileNotFoundException("文件服务器中不存在该文件");
        }
        return remoteFileInfo;
    }

    // 处理重名
    public static void resolveDuplicate(ZipOutputStream zos, String zipName, ZipEntry zipEntry) throws IOException {
        try {
            zos.putNextEntry(new ZipEntry(zipEntry));
        } catch (Exception e) {
            zipEntry = new ZipEntry(FileUtil.getFileNameAndSuffix(zipName)[0] + "(" + IdUtil.getUUID() + ")" + "." + FileUtil.getFileNameAndSuffix(zipName)[1]);
            zos.putNextEntry(new ZipEntry(zipEntry));
        }
    }

}
