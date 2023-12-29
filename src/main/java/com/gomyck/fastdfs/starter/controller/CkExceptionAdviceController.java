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

package com.gomyck.fastdfs.starter.controller;


import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.cache.redis.starter.core.redis.annotation.RedisManager;
import com.gomyck.fastdfs.starter.common.Constant;
import com.gomyck.fastdfs.starter.profile.FileServerProfile;
import com.gomyck.util.CkId;
import com.gomyck.util.DataFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 异常处理 handler, 使用 redirect 来重定向请求, 因为存在 form 表单提交下载(批量)
 * 如果不重定向, 会导致 method not support 的错误
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/6/1
 */
@ControllerAdvice
@ConditionalOnProperty(value = "gomyck.fastdfs.enable-error-advice", havingValue = "true")
public class CkExceptionAdviceController {

    private static final Logger log = LoggerFactory.getLogger(CkExceptionAdviceController.class);

    @Autowired
    private FileServerProfile fsp;
    @Value("${server.servlet.context-path:/}")
    private String contextPath;
    @Autowired
    private RedisCache rc;

    /**
     * 默认的异常处理器
     *
     * @param ex 异常信息
     * @return 查看页面
     * @throws UnsupportedEncodingException 编码异常
     */
    @ExceptionHandler(Exception.class)
    @RedisManager
    public String defaultExceptionHandler(Exception ex) throws UnsupportedEncodingException {
        log.error(ex.getMessage());
        String uuid = CkId.getUUID();
        String key = Constant.EXCEPTION_ID + uuid;
        rc.set(key, ex.getMessage());
        rc.expireKeySeconds(key, 60);
        return UrlBasedViewResolver.REDIRECT_URL_PREFIX + DataFilter.getFirstNotNull(fsp.getErrorPageHostName(), contextPath) + "/ck-fastdfs/view/error.html?uid=" + uuid + "&host=" + URLEncoder.encode(DataFilter.getFirstNotNull(fsp.getErrorPageHostName(), contextPath).toString(), StandardCharsets.UTF_8.toString());
    }

}
