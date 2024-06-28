/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.gomyck.fastdfs.starter.util.validator;

import com.gomyck.util.servlet.R;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校验异常
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [gomyck-quickdev-1.0.0]
 * @since 2021/6/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CkValidationException extends RuntimeException{

    private R result;

    public CkValidationException(R r) {
        super(r.getResMsg());
        this.result = r;
    }

    public CkValidationException(String msg) {
        super(msg);
        this.result = R.error(R._500, msg);
    }

    public CkValidationException(Integer code, String msg) {
        super(msg);
        this.result = R.error(code, msg);
    }

    public CkValidationException(Integer code, String msg, Object data) {
        super(msg);
        this.result = R.error(code, msg, data);
    }

    @Override
    public String toString() {
        return result.getResMsg();
    }
}
