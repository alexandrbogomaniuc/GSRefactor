package com.dgphoenix.casino.gs.singlegames.tools.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
public class NumberUtils {

    public static final double MONEY_PRECISION = 0.001;
    public static final int CENTS_SCALE = 2;
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

    // Wave 1 (reporting/display) helper: keeps current cent-based display semantics centralized.
    public static double centsToDouble(long cents) {
        return minorUnitsToDouble(cents, CENTS_SCALE);
    }

    public static double minorUnitsToDouble(long minorUnits, int scale) {
        return BigDecimal.valueOf(minorUnits, scale).doubleValue();
    }

    // Parses a decimal string and rounds to scaled long using explicit HALF_UP (display/reporting paths).
    public static long decimalStringToScaledLongHalfUp(String value, int scale) {
        return new BigDecimal(value)
                .movePointRight(scale)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    public static long decimalStringToCentsHalfUp(String value) {
        return decimalStringToScaledLongHalfUp(value, CENTS_SCALE);
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
