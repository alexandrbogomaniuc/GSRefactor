package com.abs.casino.gs.singlegames.tools.cbservtools;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

public class CM2Date {
    private static final Logger LOG = Logger.getLogger(CM2Date.class);

    protected static final SimpleDateFormat dateFormatWithoutTime = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US);
    protected static final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US);
    protected static SimpleDateFormat vabDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static String parseVAB(String date) {
        String res = date;
        try {
            res = vabDateFormat.format(dateFormat.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;

    }

    public static Date getDatePlusMinutes(Date date, int minutes) {
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, minutes);
            return calendar.getTime();
        } catch (Exception ex) {
        }
        return date;
    }

    public static String dbToCM2(String date) {
        String res = date;
        try {
            res = dateFormat.format(dbDateFormat.parse(date));
        } catch (Exception e) {
        }
        return res;
    }

    public static Date parse(String date) throws Exception {
        return dateFormat.parse(date);
    }

    public static String parse(Date date) {
        return dateFormat.format(date);
    }

    public static String parseWithoutTime(Date date) {
        return dateFormatWithoutTime.format(date);
    }

    public static String getToday() {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            result = dateFormat.format(calendar.getTime());
        } catch (Exception e) {
        }
        return result;
    }

    public static int getMonth(String date) {
        int result = 0;
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(dateFormat.parse(date));
            result = calendar.get(Calendar.MONTH);
        } catch (Exception e) {
        }
        return result;
    }

    public static int getDay(String date) {
        int result = 0;
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(dateFormat.parse(date));
            result = calendar.get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
        }
        return result;
    }

    public static int getYear(String date) {
        int result = 0;
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(dateFormat.parse(date));
            result = calendar.get(Calendar.YEAR);
        } catch (Exception e) {
        }
        return result;
    }

    public static String getTodayBegin() {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
            result = dateFormat.format(calendar.getTime());
        } catch (Exception e) {
        }
        return result;
    }

    public static String getTodayWithoutTime() {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            result = dateFormatWithoutTime.format(calendar.getTime());
        } catch (Exception e) {
        }
        return result;
    }

    public static String getEraBegin() // )
    {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
            calendar.set(Calendar.YEAR, 2000);
            //calendar.add(Calendar.YEAR, -1);
            result = dateFormat.format(calendar.getTime());
        } catch (Exception e) {
        }
        return result;
    }

    public static String getTodayEnd() {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
            result = dateFormat.format(calendar.getTime());
        } catch (Exception e) {
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            String currentDate = "29 Oct 2002 00:59:59";
            System.out.println(getNextPeriod(currentDate, currentDate, "1", 0));
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    protected static String check(String endDate, String tryDate) {
        try {
            if (dateFormat.parse(tryDate).after(dateFormat.parse(endDate)))
                return endDate;
        } catch (Exception e) {
        }
        return tryDate;
    }

    public static String getNextPeriod(String startDate, String endDate, String reportType, int index) {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(dateFormat.parse(startDate));
            if (reportType.equals("1")) //daily
            {
                calendar.add(Calendar.DAY_OF_MONTH, index);
                result = dateFormat.format(calendar.getTime()) + " -<br>";
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.SECOND, -1);
                result += check(endDate, dateFormat.format(calendar.getTime()));
            } else if (reportType.equals("2")) //weekly
            {
                calendar.add(Calendar.DAY_OF_MONTH, index * 7);
                result = dateFormat.format(calendar.getTime()) + " -<br>";
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                calendar.add(Calendar.SECOND, -1);
                result += check(endDate, dateFormat.format(calendar.getTime()));
            } else if (reportType.equals("3")) //monthly
            {
                calendar.add(Calendar.MONTH, index);
                result = dateFormat.format(calendar.getTime()) + " -<br>";
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.SECOND, -1);
                result += check(endDate, dateFormat.format(calendar.getTime()));
            }
        } catch (Exception e) {
        	LOG.error("CM2Date::exception:", e);
        }
        return result;
    }

    public static String getNextPeriodText(String startDate, String endDate, String reportType, int index) {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(dateFormat.parse(startDate));
            if (reportType.equals("1")) //daily
            {
                calendar.add(Calendar.DAY_OF_MONTH, index);
                result = dateFormat.format(calendar.getTime()) + " - ";
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.SECOND, -1);
                result += check(endDate, dateFormat.format(calendar.getTime()));
            } else if (reportType.equals("2")) //weekly
            {
                calendar.add(Calendar.DAY_OF_MONTH, index * 7);
                result = dateFormat.format(calendar.getTime()) + " - ";
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                calendar.add(Calendar.SECOND, -1);
                result += check(endDate, dateFormat.format(calendar.getTime()));
            } else if (reportType.equals("3")) //monthly
            {
                calendar.add(Calendar.MONTH, index);
                result = dateFormat.format(calendar.getTime()) + " - ";
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.SECOND, -1);
                result += check(endDate, dateFormat.format(calendar.getTime()));
            }
        } catch (Exception e) {
        	LOG.error("CM2Date::exception:", e);
        }
        return result;
    }

    public static String getNextPeriodStartDate(String startDate, String reportType, int index) {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(dateFormat.parse(startDate));
            if (reportType.equals("1")) //daily
            {
                calendar.add(Calendar.DAY_OF_MONTH, index);
                result = dateFormat.format(calendar.getTime());
            } else if (reportType.equals("2")) //weekly
            {
                calendar.add(Calendar.DAY_OF_MONTH, index * 7);
                result = dateFormat.format(calendar.getTime());
            } else if (reportType.equals("3")) //monthly
            {
                calendar.add(Calendar.MONTH, index);
                result = dateFormat.format(calendar.getTime());
            }
        } catch (Exception e) {
        	LOG.error("CM2Date::exception:", e);
        }
        return result;
    }

    public static String getNextPeriodEndDate(String startDate, String reportType, int index) {
        String result = "";
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(dateFormat.parse(startDate));
            if (reportType.equals("1")) //daily
            {
                calendar.add(Calendar.DAY_OF_MONTH, index);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.SECOND, -1);
                result = dateFormat.format(calendar.getTime());
            } else if (reportType.equals("2")) //weekly
            {
                calendar.add(Calendar.DAY_OF_MONTH, index * 7);
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                calendar.add(Calendar.SECOND, -1);
                result = dateFormat.format(calendar.getTime());
            } else if (reportType.equals("3")) //monthly
            {
                calendar.add(Calendar.MONTH, index);
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.SECOND, -1);
                result = dateFormat.format(calendar.getTime());
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static String getAverage(String totalTime, String count) {
        String res = "";
        System.out.println("TYT: get averate time='" + totalTime + "'  count='" + count + "'");
        try {
            StringTokenizer st = new StringTokenizer(totalTime.trim(), ":");
            long hours = 0, minutes = 0, secs = 0;
            if (st.countTokens() == 3) {
                hours = Integer.parseInt(st.nextToken());
                minutes = Integer.parseInt(st.nextToken());
                secs = Integer.parseInt(st.nextToken());
            } else if (st.countTokens() == 2) {
                minutes = Integer.parseInt(st.nextToken());
                secs = Integer.parseInt(st.nextToken());
            } else
                secs = Integer.parseInt(st.nextToken());
            secs = hours * 60 * 60 + minutes * 60 + secs;
            res = getAverage(secs, Long.parseLong(count));
        } catch (Exception ex) {
        }
        return res;
    }

    protected static String getAverage(long sec, long count) {
        String res = "";
        try {
            sec = sec / count;
            if (sec < 0)
                throw new Exception();
            long hours = sec / 3600;
            sec -= hours * 3600;
            long minutes = sec / 60;
            sec -= minutes * 60;
            res = hours < 10 ? "0" : "";
            res += hours;
            res += minutes < 10 ? ":0" : ":";
            res += minutes;
            res += sec < 10 ? ":0" : ":";
            res += sec;
        } catch (Exception ex) {
            res = "";
        }
        return res;
    }

    public static String getAverage(String startDate, String endDate, String count) {
        String res = "0";
        try {
            res = getAverage((dateFormat.parse(endDate).getTime() - dateFormat.parse(startDate).getTime()) / 1000, Long.parseLong(count));
        } catch (Exception e) {
        	LOG.error("CM2Date::exception:", e);
        }
        return res;
    }

    public static String getDiff(String startDate, String endDate) {
        try {
            long sec = (dateFormat.parse(endDate).getTime() - dateFormat.parse(startDate).getTime()) / 1000;
            if (sec < 0)
                throw new Exception();
            long hours = sec / 3600;
            sec -= hours * 3600;
            long minutes = sec / 60;
            sec -= minutes * 60;
            String res = hours < 10 ? "0" : "";
            res += hours;
            res += minutes < 10 ? ":0" : ":";
            res += minutes;
            res += sec < 10 ? ":0" : ":";
            res += sec;
            return res;
        } catch (Exception ex) {
        }
        return "00:00:00";
    }
}