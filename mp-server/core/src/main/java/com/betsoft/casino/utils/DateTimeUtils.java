package com.betsoft.casino.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {


    public static String toHumanReadableFormat(long dateTimeMillis) {
        return  toHumanReadableFormat(dateTimeMillis, null);
    }

    public static String toHumanReadableFormat(long dateTimeMillis, String formatPattern) {
        return  toHumanReadableFormat(dateTimeMillis, formatPattern, null);
    }

    public static String toHumanReadableFormat(long dateTimeMillis, String formatPattern, String timeZoneID) {

        if(formatPattern == null || formatPattern.isEmpty()) {
            //formatPattern = "yyyy-MM-dd HH:mm:ss.SSS";
            formatPattern = "HH:mm:ss.SSS";
        }

        if(timeZoneID == null || timeZoneID.isEmpty()) {
            timeZoneID = "UTC";
        }
        DateFormat dateFormat = new SimpleDateFormat(formatPattern);
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone(timeZoneID));
        Date date = new Date(dateTimeMillis);
        return dateFormat.format(date);
    }
}
