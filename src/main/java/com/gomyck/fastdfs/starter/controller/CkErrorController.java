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
import com.gomyck.util.servlet.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 错误页跳转 handler
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/5/13
 */
@Controller
@ConditionalOnProperty(value = "gomyck.fastdfs.enable-error-advice", havingValue = "true")
public class CkErrorController {

    @Autowired
    private RedisCache rc;


    /**
     * 返回错误
     *
     * @param uid uid
     * @return R
     */
    @GetMapping("getThrowableInfo")
    @ResponseBody
    @RedisManager
    public R getThrowableInfo(String uid){
        String s = rc.get(Constant.EXCEPTION_ID + uid);
        rc.delKey(Constant.EXCEPTION_ID + uid);
        return R.ok(R._200, s);
    }


}
