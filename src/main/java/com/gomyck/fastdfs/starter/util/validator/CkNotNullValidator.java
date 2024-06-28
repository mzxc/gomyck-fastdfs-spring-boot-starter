/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.gomyck.fastdfs.starter.util.validator;

import com.gomyck.fastdfs.starter.util.validator.annotation.CkNotNull;
import com.gomyck.util.ObjectJudge;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;


/**
 * 非空校验, notNull 不能满足空值校验
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [zhaoshang-pay-1.0.0]
 * @since 2021/5/20
 */
@Slf4j
public class CkNotNullValidator implements ConstraintValidator<CkNotNull, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return ObjectJudge.notNull(value);
    }

    @Override
    public void initialize(CkNotNull constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

}
