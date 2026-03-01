package com.abs.casino.common.util;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.abs.casino.common.exception.CommonException;

public class BeanPropertyUtils {
    private BeanPropertyUtils() {
    }

    public static PropertyUtilsBean getPropertyUtils() {
        return new PropertyUtilsBean();
    }


    public static void copyProperties(Object dest, Object orig, String... excludePropertyNames)
            throws CommonException {
        try {
            PropertyUtilsBean propertyUtils = getPropertyUtils();
            PropertyDescriptor descriptors[] = propertyUtils
                    .getPropertyDescriptors(orig);
            List<String> propertyNames = new ArrayList<String>();
            for (PropertyDescriptor descriptor : descriptors) {
                String name = descriptor.getName();
                if (!isExclude(name, excludePropertyNames)) {
                    if (descriptor.getReadMethod() != null
                            /*&& descriptor.getWriteMethod() != null*/) {
                        propertyNames.add(name);
                    }
                }
            }

            for (String name : propertyNames) {
                Object value = propertyUtils.getNestedProperty(orig, name);
                //System.out.println("copyProperties: " + name + ", value=" + value +
                //        ", simple=" + (value == null ? "null" : isSimpleType(value.getClass())));
                if (value != null && isSimpleType(value.getClass())) {
                    BeanUtils.copyProperty(dest, name, value);
                }
            }
        } catch (Exception e) {
            throw new CommonException("Unable to copy properties of '"
                    + orig.getClass().getName() + "'", e);
        }
    }

    private static boolean isSimpleType(Class<?> klass) {
        return Date.class.equals(klass) || Date.class.isAssignableFrom(klass) ||
                String.class.equals(klass) || String.class.isAssignableFrom(klass) ||
                Integer.class.equals(klass) || Integer.TYPE.equals(klass) || Integer.class.isAssignableFrom(klass) ||
                Long.class.equals(klass) || Long.TYPE.equals(klass) || Long.class.isAssignableFrom(klass) ||
                Double.class.equals(klass) || Double.TYPE.equals(klass) || Double.class.isAssignableFrom(klass) ||
                Float.class.equals(klass) || Float.TYPE.equals(klass) || Float.class.isAssignableFrom(klass) ||
                Boolean.class.equals(klass) || Boolean.TYPE.equals(klass) || Boolean.class.isAssignableFrom(klass) ||
                Enum.class.equals(klass) || Enum.class.isAssignableFrom(klass);
    }

    private static boolean isExclude(String name, String[] excludePropertyNames) {
        for (String string : excludePropertyNames) {
            if (string.trim().equals(name.trim())) {
                return true;
            }
        }
        return false;
    }
}
