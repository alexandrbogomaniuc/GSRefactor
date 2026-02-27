package com.abs.casino.gs.singlegames.tools.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;


/**
 * Period of time.
 */
public class TimePeriod implements Serializable {

    private static final long serialVersionUID = 2466611711053318947L;

    /**
     * Denotes time period measurement in days.
     */
    public static final int DAY = Calendar.DAY_OF_YEAR;

    /**
     * Denotes time period measurement in weeks.
     */
    public static final int WEEK = Calendar.WEEK_OF_YEAR;

    /**
     * Denotes time period measurement in months.
     */
    public static final int MONTH = Calendar.MONTH;


    /**
     * One-month period.
     */
    public static final TimePeriod MONTHLY = new TimePeriod(MONTH, 1);

    /**
     * One-week period.
     */
    public static final TimePeriod WEEKLY = new TimePeriod(WEEK, 1);

    /**
     * One-day period.
     */
    public static final TimePeriod DAILY = new TimePeriod(DAY, 1);


    private final int typeIdx;
    private final int length;

    /**
     * Sorted valid type values.
     */
    private static int[] VALID_TYPES;

    private static int[] getValidTypes() {
        if (VALID_TYPES == null) {
            VALID_TYPES = new int[]{
                    DAY, WEEK, MONTH,
            };
        }
        Arrays.sort(VALID_TYPES);

        return VALID_TYPES;
    }

    /**
     * Creates new time period.
     *
     * @param type   Type of period interval unit.
     * @param length Period length in <code>type</code> units.
     * @throws IllegalArgumentException <code>type</code> has illegal value or
     *                                  <code>length</code> is negative or zero.
     */
    public TimePeriod(int type, int length)
            throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException(
                    "TimePeriod length must not be positive: " + length);
        }
        this.typeIdx = Arrays.binarySearch(getValidTypes(), type);
        if (this.typeIdx < 0) {
            throw new IllegalArgumentException(
                    "Illegal TimePeriod type: " + type);
        }
        this.length = length;
    }

    /**
     * Returns type of period interval unit.
     *
     * @return One of constants.
     */
    public final int getType() {
        return getValidTypes()[this.typeIdx];
    }

    /**
     * Returns period length.
     *
     * @return Amount of <code>type</code> units in period.
     */
    public final int getLength() {
        return this.length;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TimePeriod)) {
            return false;
        }

        final TimePeriod tp = (TimePeriod) o;

        return getType() == tp.getType() && getLength() == tp.getLength();
    }

    public int hashCode() {
        return typeIdx | (length << 2);
    }

    public String toString() {

        final StringBuilder res = new StringBuilder();
        final int len = getLength();

        res.append(len);

        switch (getType()) {
            case DAY:
                res.append(" day");
                break;
            case WEEK:
                res.append(" week");
                break;
            case MONTH:
                res.append(" month");
                break;
            default:
                res.append(" ???");
        }

        if (len != 1) {
            res.append('s');
        }

        return res.toString();
    }

}
