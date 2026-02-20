package com.dgphoenix.casino.gs.singlegames.tools.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtils {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static String dateAsStringGMT(Date d, String format) {
        DateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(d);
    }

    public static String dateAsString(Date d) {
        return DF.format(d.toInstant().atZone(ZoneId.systemDefault()));
    }

    public static String dateAsString(long d) {
        return dateAsString(new Date(d));
    }

    public static String userFriendlyDate(Date d) {
        DateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
        return df.format(d);
    }

    public static String userFriendlyDate(long d) {
        return userFriendlyDate(new Date(d));
    }

    public static String periodAsString(long period) {
        StringBuilder result = new StringBuilder();

        long days = (long) Math.floor(period / (24 * 60 * 60 * 1000));
        if (days > 0) {
            period -= days * 24 * 60 * 60 * 1000;
            result.append(days + " days ");
        }

        long hours = (long) Math.floor(period / (60 * 60 * 1000));
        period -= hours * 60 * 60 * 1000;
        result.append((hours < 10 ? "0" : "") + hours + ":");

        long minutes = (long) Math.floor(period / (60 * 1000));
        period -= minutes * 60 * 1000;
        result.append((minutes < 10 ? "0" : "") + minutes + ":");

        long seconds = (long) Math.ceil(period / 1000);
        result.append((seconds < 10 ? "0" : "") + seconds);

        return result.toString();
    }

    public static Date zeroHourMinuteSecond(Date date) {
        return setHoursMinutesSecondsMillis(date, 0, 0, 0, 0);
    }

    public static Date maxHourMinuteSecond(Date date) {
        return setHoursMinutesSecondsMillis(date, 23, 59, 59, 999);
    }

    private static Date setHoursMinutesSecondsMillis(Date date, int hours, int minutes, int seconds, int millis) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, millis);

        return calendar.getTime();
    }

    /**
     * Calculates which of TimePeriods contains <code>currentDate</code>
     * and sets DateRange of active period activity.
     *
     * <p>TimePeriods repeating sequentially after <code>startDate</code>.</p>
     *
     * @param startDate    Start date to start TimePeriods from
     * @param firstPeriod  First TimePeriod.
     * @param secondPeriod Second TimePeriod starting right after
     *                     <code>firstPeriod</code> or <code>null</code>.
     * @param currentDate  Date to check.
     * @param resultRange  Resulting DateRange.
     * @return <code>true</code> if <code>firstPeriod</code> contains
     * <code>curerntDate</code> and <code>false</code> otherwise.
     */
    public static boolean whichPeriodFromStart(
            Date startDate,
            TimePeriod firstPeriod,
            TimePeriod secondPeriod,
            Date currentDate,
            DateRange resultRange) {

        final Calendar cal = Calendar.getInstance();
        final long start;
        final long firstLength;
        final long totalLength;

        cal.setTime(startDate);
        start = cal.getTimeInMillis();
        cal.add(firstPeriod.getType(), firstPeriod.getLength());
        firstLength = cal.getTimeInMillis() - start;
        if (secondPeriod == null) {
            totalLength = firstLength;
        } else {
            cal.add(secondPeriod.getType(), secondPeriod.getLength());
            totalLength = cal.getTimeInMillis() - start;
        }

        cal.setTime(currentDate);

        final long current = cal.getTimeInMillis();
        final long offset = (current - start) % totalLength;
        final long rangeStartMillis = current - offset;
        final Date rangeStart;
        final Date rangeEnd;

        cal.setTimeInMillis(rangeStartMillis);
        rangeStart = cal.getTime();

        cal.setTimeInMillis(rangeStartMillis + totalLength);
        rangeEnd = cal.getTime();

        resultRange.setRange(rangeStart, rangeEnd);

        return offset < firstLength;
    }

    public static void main(String[] args) {
        //Calendar cal = new GregorianCalendar();
        Calendar cal = Calendar.getInstance();

        cal.set(2004, Calendar.SEPTEMBER, 16, 0, 0, 0);
        Date startDate = cal.getTime();

        cal.set(2004, Calendar.SEPTEMBER, 24, 1, 0, 0);
        Date currentDate = cal.getTime();

        DateRange range = new DateRange();
        boolean active = DateUtils.whichPeriodFromStart(startDate, TimePeriod.DAILY, TimePeriod.WEEKLY, currentDate, range);

        System.out.println(dateAsString(range.getStartDate()) + " != " + dateAsString(startDate));
        System.out.println(active ? "active" : "inactive");
    }
}
