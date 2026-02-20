package com.dgphoenix.casino.common.util;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtils {

    public static final double MONEY_PRECISION = 0.001;
    private static final NumberFormat MONEY_DISPLAY_FORMAT =
            NumberFormat.getInstance(Locale.ENGLISH);

    static {
        MONEY_DISPLAY_FORMAT.setMinimumFractionDigits(2);
    }

    public static double asMoney(double d) {
        return (double) Math.round(d * 100) / 100;
        //    return (new BigDecimal(d).setScale(2,4).doubleValue());//ROUND_HALF_UP
    }

    public static double asPercent(double d) {
        return (double) Math.round(d * 10000) / 10000;
    }

    public static String asMoneyDisplayFormat(double d) {
        return MONEY_DISPLAY_FORMAT.format(d);
    }

    public static String asMoneyFormat(double d) {
        return asMoneyFormat(Double.toString(NumberUtils.asMoney(d)));
    }

    public static String asMoneyFormat(String s) {

        final int dot = s.indexOf('.');

        if (dot < 0) {
            s += ".00";
        } else if (s.length() == dot + 2) {
            s += "0";
        }

        return s;
    }

    public static boolean equalsDouble(double value1, double value2) {

        final long first = Double.doubleToLongBits(value1);
        final long second = Double.doubleToLongBits(value2);

        return first == second;
    }

    public static boolean equalsDouble(
            double val1,
            double val2,
            double delta) {
        return Math.abs(Math.abs(val1) - Math.abs(val2)) < Math.abs(delta);
    }

    public static boolean equalsMoney(double value1, double value2) {
        return equalsDouble(value1, value2, MONEY_PRECISION);
    }

    public static boolean positiveMoney(double sum) {
        return !(sum < 0 || equalsMoney(sum, 0.0d));
    }

    public static String twoDisits(long i) {
        if (i >= 10) {
            return String.valueOf(i);
        }
        return "0" + String.valueOf(i);
    }
}