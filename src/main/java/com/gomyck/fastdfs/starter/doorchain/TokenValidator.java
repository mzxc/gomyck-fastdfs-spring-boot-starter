/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.gomyck.fastdfs.starter.doorchain;

import com.gomyck.cache.redis.starter.core.redis.RedisCache;
import com.gomyck.util.StringJudge;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * | blog: https://blog.gomyck.com |
 * --------------------------------
 * @version [gomyck-quickdev-1.0.0]
 * @since 2022/5/20 10:09
 */
public class TokenValidator implements Validator<String>{

    @Autowired
    RedisCache rs;

    /**
     * 验证方法
     *
     * @param token token
     * @return 是否成功
     */
    @Override
    public boolean verifyIt(final String token) {
        return StringJudge.notNull(rs.get(token));
    }

}
