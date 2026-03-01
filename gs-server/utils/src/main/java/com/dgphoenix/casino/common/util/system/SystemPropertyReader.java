package com.abs.casino.common.util.system;


import com.abs.casino.common.util.string.StringUtils;

/** Provides API to read system properties */
public class SystemPropertyReader {
    public String getStringProperty(String propertyName) {
        String value = System.getProperty(propertyName);
        if (StringUtils.isTrimmedEmpty(value)) {
            throw new RuntimeException("Cannot find system property: " + propertyName);
        }
        return value;
    }

    public int getIntProperty(String propertyName) {
        String strValue = getStringProperty(propertyName);
        int value;
        try {
            value = Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("System property %s can't be represented as integer", propertyName));
        }
        return value;
    }
}
