/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.gomyck.fastdfs.starter.util.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性描述
 *
 * @author gomyck
 * --------------------------------
 * | qq: 474798383                 |
 * | email: hao474798383@163.com   |
 * --------------------------------
 * @version [zhaoshang-pay-1.0.0]
 * @since 2021/5/20
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMapping {

    String key(); //指定属性名称
    String value()  default ""; //当这个属性的值为 value 时, 则触发 rules 规则, 如果不填, 那么所有值都匹配, 但不包括空
    Rules  rules()  default Rules.NOT_NULL;
    String mustBe() default "";


    enum Rules{
        NOT_NULL,      // 一定非空
        MUST_BE,       // 一定等于某个值
        NULL           // 一定是空
    }

}
