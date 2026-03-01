package com.abs.casino.common.util.property;

import com.abs.casino.common.util.string.StringUtils;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: zhevlakoval
 * Date: 26.05.11
 * Time: 12:28
 * helper class for Map<String, String> work
 */
public class PropertyUtils {

    public static Long getLongProperty(Map<String, String> properties, String name) {
        String result = getStringProperty(properties, name);
        return result == null ? null : Long.valueOf(result);
    }

    public static long getLongProperty(Map<String, String> properties, String name, long defaultValue) {
        Long result = getLongProperty(properties, name);
        return result == null ? defaultValue : result;
    }

    public static Integer getIntProperty(Map<String, String> properties, String name) {
        String result = getStringProperty(properties, name);
        return result == null ? null : Integer.valueOf(result);
    }

    public static Integer getIntProperty(Map<String, String> properties, String name, int defaultValue) {
        Integer result = getIntProperty(properties, name);
        return result == null ? defaultValue : result;
    }

    public static boolean getBooleanProperty(Map<String, String> properties, String name) {
        String result = properties.get(name);
        if(result == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(result);
    }

    public static String getStringProperty(Map<String, String> properties, String name) {
        if(properties == null) {
            return null;
        }
        String result = properties.get(name);
        if (StringUtils.isTrimmedEmpty(result)) {
            return null;
        }
        return result.trim();
    }
}
