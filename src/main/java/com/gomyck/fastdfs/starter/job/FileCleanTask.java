/*
 *
 *  *
 *  *  * Copyright (c) 2019 Gomyck
 *  *  *
 *  *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  *  * of this software and associated documentation files (the "Software"), to deal
 *  *  * in the Software without restriction, including without limitation the rights
 *  *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  *  * copies of the Software, and to permit persons to whom the Software is
 *  *  * furnished to do so, subject to the following conditions:
 *  *  *
 *  *  * The above copyright notice and this permission notice shall be included in all
 *  *  * copies or substantial portions of the Software.
 *  *  *
 *  *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  *  * SOFTWARE.
 *  *
 *
 */

package com.gomyck.fastdfs.starter.job;

import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.cache.redis.starter.core.redis.annotation.RedisManager;
import com.gomyck.fastdfs.starter.common.Constant;
import com.gomyck.fastdfs.starter.controller.UploadManageHandler;
import com.gomyck.fastdfs.starter.database.UploadService;
import com.gomyck.fastdfs.starter.database.entity.CkFileInfo;
import com.gomyck.util.CkDateUtil;
import com.gomyck.util.R;
import com.gomyck.util.StringJudge;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 清理过期的文件(只针对加入了过期标志的文件信息)
 *
 * @author gomyck
 * @version 1.0.0
 * @since 2020-04-15
 */
@Component
@Slf4j
public class FileCleanTask {

    @Autowired
    UploadService uploadService;

    @Autowired
    UploadManageHandler uploadManageHandler;

    @Autowired
    RedisCache rc;

    @Scheduled(cron = "0 */30 * * * *")
    @RedisManager
    public void cleanTempFile(){
        String expireFile = null;
        List<CkFileInfo> ckFileInfos = uploadService.selectCompleteFileInfo(0L, -1L);
        if(ckFileInfos != null && ckFileInfos.size() > 0){
            expireFile = ckFileInfos.stream().filter(e -> {
                Long expireTime = e.getExpireTime();
                if (expireTime == null) {
                    return false;
                } else {
                    LocalDateTime createTime = LocalDateTime.parse(e.getUploadTime(), DateTimeFormatter.ofPattern(CkDateUtil.DUF.CN_DATETIME_FORMAT));
                    return createTime.plusSeconds(expireTime).isBefore(LocalDateTime.now());
                }
            }).map(CkFileInfo::getFileMd5).collect(Collectors.joining(","));
        }
        doClean(expireFile);
        Set<String> keys = rc.keysPattern(Constant.FILE_INFO + "*");
        if(keys != null) {
            expireFile = keys.stream().filter(e -> {
                CkFileInfo fileUploadStatus = uploadService.getFileUploadStatus(e.replace(Constant.FILE_INFO, ""));
                Long expireTime = fileUploadStatus.getExpireTime();
                if (expireTime == null) {
                    return false;
                } else {
                    LocalDateTime createTime = LocalDateTime.parse(fileUploadStatus.getUploadTime(), DateTimeFormatter.ofPattern(CkDateUtil.DUF.CN_DATETIME_FORMAT));
                    return createTime.plusSeconds(expireTime).isBefore(LocalDateTime.now());
                }
            }).map(e -> e.replaceAll(Constant.FILE_INFO, "")).collect(Collectors.joining(","));
        }
        doClean(expireFile);
    }

    private void doClean(String expireFile) {
        if(StringJudge.isNull(expireFile)) return;
        log.info("开始清理过期文件: {}", expireFile);
        R r = uploadManageHandler.batchDelFile(expireFile);
        log.info("==========清理结束, 返回结果为: {}==========", r);
    }


}
