package com.abs.casino.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class DigitFormatter {
    private static final int CURRENCY_FRACTION_DIGITS = 2;

    protected static DecimalFormat getNumberFormat() {
        DecimalFormat numberFormat = new DecimalFormat();
        numberFormat.setGroupingSize(100);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat;
    }

    protected static DecimalFormat getNumberFormatRoundFloor() {
        DecimalFormat numberFormatRoundFloor = new DecimalFormat();
        numberFormatRoundFloor.setGroupingSize(100);
        numberFormatRoundFloor.setMinimumFractionDigits(3);
        numberFormatRoundFloor.setMaximumFractionDigits(2);
        numberFormatRoundFloor.setRoundingMode(RoundingMode.FLOOR);
        return numberFormatRoundFloor;
    }

    protected static DecimalFormat getCurrencyFormat() {
        DecimalFormat currencyFormat = new DecimalFormat();
        currencyFormat.setGroupingSize(100);
        currencyFormat.setMinimumFractionDigits(2);
        DecimalFormatSymbols currencySymb = new DecimalFormatSymbols();
        currencySymb.setGroupingSeparator(',');
        currencySymb.setDecimalSeparator('.');
        currencyFormat.setDecimalFormatSymbols(currencySymb);
        currencyFormat.setDecimalSeparatorAlwaysShown(true);
        currencyFormat.applyPattern("#,##0.00");
        return currencyFormat;
    }

    protected static DecimalFormat getAgaNumberFormat() {
        DecimalFormat agaNumberFormat = new DecimalFormat();
        DecimalFormatSymbols symb = new DecimalFormatSymbols();
        symb.setDecimalSeparator('.');
        agaNumberFormat.setDecimalFormatSymbols(symb);
        agaNumberFormat.applyPattern("0.00000");
        return agaNumberFormat;
    }

    public static String doubleToMoney(double x) {
        return getNumberFormat().format(x).replace(',', '.');
     }
    public static String doubleToMoneyRoundFloor(double x) {
        return getNumberFormatRoundFloor().format(x).replace(',', '.');
    }

    public static String doubleToMoneyCommaDot(double x) {
        return getCurrencyFormat().format(x);
    }
    
    public static String agaFormat(double x) {
        return getAgaNumberFormat().format(x);
    }

	public static long getCentsFromCurrency(Double currency) {
		return new BigDecimal(currency).setScale(CURRENCY_FRACTION_DIGITS,RoundingMode.HALF_UP).unscaledValue().longValue();
	}

    public static double getDollarsFromCents(long cents) {
		return new BigDecimal( ((double)cents) / 100).setScale(CURRENCY_FRACTION_DIGITS, RoundingMode.HALF_UP).doubleValue();
	}

	public static double getDollarsFromCents(Double cents) {
		return new BigDecimal(cents / 100).setScale(CURRENCY_FRACTION_DIGITS, RoundingMode.HALF_UP).doubleValue();
	}

    public static double getDollarsFromCentsRoundDown(Double cents) {
        return new BigDecimal(cents / 100).setScale(CURRENCY_FRACTION_DIGITS, RoundingMode.DOWN).doubleValue();
    }

    public static double denominateMoney(double money, int denominator) {
        return new BigDecimal(doubleToMoney(money))
                .divide(new BigDecimal(denominator), CURRENCY_FRACTION_DIGITS, RoundingMode.FLOOR)
                .doubleValue();
    }

    public static void main(String[] args) {
        System.out.println(getDollarsFromCents(4800024.9));
        System.out.println(getDollarsFromCents(new Long(499)));
        System.out.println(denominateMoney(4800026.9, 1000));
        System.out.println(denominateMoney(700, 1000));
        System.out.println(denominateMoney(0.70, 1));
        System.out.println(Math.round(Math.floor(1.9)));
        System.out.println(Double.valueOf(1.9).longValue());
        System.out.println(doubleToMoney(10.157));
        System.out.println(doubleToMoneyCommaDot(10.157));
        System.out.println(agaFormat(10.157));
//        double d = 100000.03;
//        System.out.println(DigitFormatter.doubleToMoneyCommaDot(d));
    }
}
