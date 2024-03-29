/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.gomyck.fastdfs.starter.doorchain;

/**
 * 验证器
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [gomyck-quickdev-1.0.1]
 * @since 2022/5/20 10:09
 */
public interface Validator<T> {

    /**
     * 验证
     *
     * @param token token
     * @return 是否成功
     */
    boolean verifyIt(T token);

}
