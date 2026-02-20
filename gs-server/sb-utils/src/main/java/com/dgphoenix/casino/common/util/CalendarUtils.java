package com.dgphoenix.casino.common.util;

import java.util.*;
import java.util.concurrent.TimeUnit;


public final class CalendarUtils {
    private CalendarUtils() {
    }

    public static long timeToMidnight() {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis() - System.currentTimeMillis();
    }

    //return days from 01.01.1970
    public static long getDaysFrom1970(long date) {
        return TimeUnit.MILLISECONDS.toDays(date);
    }

    public static int getDayOfWeek(Date date) {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getHourOfDay(Date date) {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static DatePeriod getPrevWeekPeriod(long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(now));
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date prevWeekDay = calendar.getTime();
        return new DatePeriod(getStartWeek(prevWeekDay).getTime(), getEndWeek(prevWeekDay).getTime());

    }

    public static DatePeriod getPrevDayPeriod(long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(now));
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date prevDay = calendar.getTime();
        return new DatePeriod(getStartDay(prevDay).getTime(), getEndDay(prevDay).getTime());
    }

    public static boolean isPrevDay(long thisDate) {
        Calendar nowStartDay = getStartDay(new Date());
        Calendar thisDateStartDay = getStartDay(new Date(thisDate));
        thisDateStartDay.add(Calendar.DAY_OF_MONTH, 1);
        return nowStartDay.get(Calendar.DAY_OF_MONTH) == thisDateStartDay.get(Calendar.DAY_OF_MONTH) &&
                nowStartDay.get(Calendar.MONTH) == thisDateStartDay.get(Calendar.MONTH) &&
                nowStartDay.get(Calendar.YEAR) == thisDateStartDay.get(Calendar.YEAR);
    }

    public static DatePeriod getThisDayPeriod() {
        Date now = new Date();
        return new DatePeriod(getStartDay(now).getTime(), getEndDay(now).getTime());
    }

    public static Calendar getStartWeek(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return getStartWeek(cal);
    }

    public static Calendar getStartWeek(Calendar cal) {
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static Calendar getEndWeek(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return getEndWeek(cal);
    }

    public static Calendar getEndWeek(Calendar cal) {
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        cal.add(Calendar.HOUR, 24);
        return cal;
    }

    public static Calendar getStartOfMonth(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return getStartOfMonth(cal);
    }

    public static Calendar getStartOfMonth(Calendar cal) {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static Calendar getEndOfMonth(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return getEndOfMonth(cal);
    }

    public static Calendar getEndOfMonth(Calendar cal) {
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }

    public static Calendar getStartDay(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        return getStartDay(cal);
    }

    public static Calendar getStartDay(Long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        return getStartDay(cal);
    }

    public static String getDaysFromPeriod(Date startDate, Date endDate) {
        Calendar startDay = getStartDay(startDate);
        Calendar endDay = getStartDay(endDate);
        StringBuilder sb = new StringBuilder();
        while(startDay.getTimeInMillis() <= endDay.getTimeInMillis()) {
            if(sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(startDay.getTimeInMillis());
            startDay.add(Calendar.DAY_OF_MONTH, 1);
        }
        return sb.toString();
    }

    public static List<Long> getDaysFromPeriodAsList(Date startDate, Date endDate) {
        List<Long> days = new ArrayList<Long>();
        Calendar startDay = getStartDay(startDate);
        Calendar endDay = getStartDay(endDate);
        while(startDay.getTimeInMillis() <= endDay.getTimeInMillis()) {
            days.add(startDay.getTimeInMillis());
            startDay.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    public static Calendar getStartDay(Date day, String timeZone) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
            cal.setTime(day);
            return getStartDay(cal);
    }

    public static Calendar getStartDay(long timeInMillis, String timeZoneID, int offset) {
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
            timeZone.setRawOffset(offset);
            Calendar cal = Calendar.getInstance(timeZone);
            cal.setTimeInMillis(timeInMillis);
            return getStartDay(cal);
    }

    public static Calendar getStartDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static Date getStartHourDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Calendar getEndDay(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }

    public static Calendar getEndDay(Date day, String timeZone) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(timeZone));
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }

    public static Date getPrevMonthDay(Date day) {
        Calendar cal = getStartDay(day);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    public static Date getFullDate(Date day, Date hourMinSec) {
        Calendar dayCal = getStartDay(day);
        Calendar cal = Calendar.getInstance();
        cal.setTime(hourMinSec);

        dayCal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        dayCal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        dayCal.set(Calendar.SECOND, cal.get(Calendar.SECOND));
        return dayCal.getTime();
    }

    public static Date getCurrentMonthFirstDay() {
        Calendar cal = getStartDay(new Date());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static Date getCurrentMonthLastDay() {
        Calendar cal = getStartDay(new Date());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public static boolean isDateBetween(Date startDate, Date endDate, Date checked) {
        if (startDate == null || endDate == null || checked == null) {
            throw new IllegalArgumentException("Date is null");
        }
        return checked.getTime() > startDate.getTime() && checked.getTime() < endDate.getTime();
    }

    public static double getHoursBetween(Date startDate, Date endDate) {
        long milisDelta = endDate.getTime() - startDate.getTime();
        return Math.floor(milisDelta / (60 * 60 * 100)) / 10;
    }


    public static void main(String[] args) {
        final Calendar endWeek = getEndWeek(new Date());
        //System.out.println(endWeek.getTime());

        final Calendar startWeek = getStartWeek(new Date());
        //System.out.println(startWeek.getTime());

        startWeek.add(Calendar.DAY_OF_MONTH, -3000);
        //String days = getDaysFromPeriod(startWeek.getTime(), endWeek.getTime());
        //System.out.println("days: " + days);
    }
}
