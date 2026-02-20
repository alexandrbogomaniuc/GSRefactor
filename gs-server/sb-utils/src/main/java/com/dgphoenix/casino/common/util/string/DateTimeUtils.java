package com.dgphoenix.casino.common.util.string;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {
    public DateTimeUtils() {
    }

    public static String toHumanReadableFormat(long dateTimeMillis) {
        return toHumanReadableFormat(dateTimeMillis, (String)null);
    }

    public static String toHumanReadableFormat(long dateTimeMillis, String formatPattern) {
        return toHumanReadableFormat(dateTimeMillis, formatPattern, (String)null);
    }

    public static String toHumanReadableFormat(long dateTimeMillis, String formatPattern, String timeZoneID) {
        if (formatPattern == null || formatPattern.isEmpty()) {
            formatPattern = "HH:mm:ss.SSS";
        }

        if (timeZoneID == null || timeZoneID.isEmpty()) {
            timeZoneID = "UTC";
        }

        DateFormat dateFormat = new SimpleDateFormat(formatPattern);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneID));
        Date date = new Date(dateTimeMillis);
        return dateFormat.format(date);
    }
}