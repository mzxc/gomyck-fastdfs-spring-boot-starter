package com.gomyck.fastdfs.starter.util.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;


/**
 * 校验工具类
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [1.0.0]
 * @since 2021/5/20
 */
public class CkValidator {

    private static final Validator validator;

    private final static ThreadLocal<Object> validateEntityHold = new ThreadLocal<>();

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public static void validateEntity(Object object, Class<?>... groups) {
        try {
            validateEntityHold.set(object);
            Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
            if (!constraintViolations.isEmpty()) {
                StringBuilder msg = new StringBuilder();
                int index = 1;
                int size = constraintViolations.size();
                for (ConstraintViolation<Object> constraint : constraintViolations) {
                    msg.append(constraint.getMessage());
                    if(index != size) {
                        msg.append("\r\n");
                        index = index + 1;
                    }
                }
                throw new CkValidationException(msg.toString());
            }
        } finally {
            validateEntityHold.remove();
        }
    }

    public static Object getValidateEntity() {
        return validateEntityHold.get();
    }

}
