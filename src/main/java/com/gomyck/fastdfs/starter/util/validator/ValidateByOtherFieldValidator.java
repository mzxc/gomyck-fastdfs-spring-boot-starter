/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.gomyck.fastdfs.starter.util.validator;

import com.gomyck.fastdfs.starter.util.validator.annotation.FieldMapping;
import com.gomyck.fastdfs.starter.util.validator.annotation.ValidateByOtherField;
import com.gomyck.util.FieldUtil;
import com.gomyck.util.ObjectJudge;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;


/**
 * 额外的校验器, 为了满足根据场景判断个别属性是否为空
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
public class ValidateByOtherFieldValidator implements ConstraintValidator<ValidateByOtherField, Object> {

    private ValidateByOtherField validateByOtherField;

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        FieldMapping[] fieldMappings = validateByOtherField.fieldMappings();
        Object validateEntity = CkValidator.getValidateEntity();
        for (FieldMapping fieldMapping : fieldMappings) {
            try {
                Object invoke = FieldUtil.getMethod(validateEntity.getClass(), fieldMapping.key()).invoke(validateEntity);
                if (ObjectJudge.isNull(invoke, fieldMapping.value()) || invoke.equals(fieldMapping.value())) {
                    switch (fieldMapping.rules()) {
                        case NOT_NULL:
                            if (ObjectJudge.isNull(value)) return false;
                            break;
                        case NULL:
                            if (ObjectJudge.notNull(value)) return false;
                            break;
                        case MUST_BE:
                            if (!fieldMapping.mustBe().equals(value)) return false;
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("may be this field {} getter is not found", fieldMapping.key());
                throw new CkValidationException(MessageFormat.format("may be this field {0} getter is not found", fieldMapping.key()));
            }
        }
        return true;
    }

    @Override
    public void initialize(ValidateByOtherField constraintAnnotation) {
        this.validateByOtherField = constraintAnnotation;
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

}
