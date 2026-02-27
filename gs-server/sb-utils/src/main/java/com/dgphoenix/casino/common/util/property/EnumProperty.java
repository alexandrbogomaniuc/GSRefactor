package com.abs.casino.common.util.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: shegan
 * Date: 04.05.16
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumProperty {
    Class<? extends Enum<?>> value();
    String description() default "";
}
