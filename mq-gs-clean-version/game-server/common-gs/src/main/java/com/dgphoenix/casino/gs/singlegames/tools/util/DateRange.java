package com.dgphoenix.casino.gs.singlegames.tools.util;

import java.io.Serializable;
import java.util.Date;


/**
 * Date range.
 */
public class DateRange implements Serializable {

    private static final long serialVersionUID = -2080384986532080550L;

    private Date startDate;
    private Date endDate;

    /**
     * Creates new date range.
     *
     * @param startDate First date in range.
     * @param endDate   Last date in range.
     * @throws NullPointerException     if <code>startDate</code> or
     *                                  <code>endDate</code> is <code>null</code>.
     * @throws IllegalArgumentException if <code>startDate</code> is later than
     *                                  <code>endDate</code>.
     */
    public DateRange(Date startDate, Date endDate)
            throws NullPointerException, IllegalArgumentException {
        setRange(startDate, endDate);
    }

    /**
     * Creates new date range with start and end date at current time.
     */
    public DateRange() {
        this.startDate = this.endDate = new Date();
    }

    /**
     * Creates new date range as copy of another one.
     *
     * @param range Another date range.
     */
    public DateRange(DateRange range) {
        setRange(range);
    }

    /**
     * Changes date range bounds.
     *
     * @param startDate First date in range.
     * @param endDate   Last date in range.
     * @throws NullPointerException     if <code>startDate</code>
     *                                  or <code>endDate</code> is null.
     * @throws IllegalArgumentException if <code>startDate</code> is later than
     *                                  <code>endDate</code>.
     */
    public final void setRange(Date startDate, Date endDate)
            throws NullPointerException, IllegalArgumentException {
        if (startDate == null) {
            throw new NullPointerException("Unknown beginning of date range");
        }
        if (endDate == null) {
            throw new NullPointerException("Unknown ending of date range");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException(
                    "Beginning of date range (" + startDate
                            + ") is later than and it's ending (" + endDate + ")");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Changes date range bounds equal to another date range bounds.
     *
     * @param range Another date range.
     */
    public final void setRange(DateRange range) {
        this.startDate = range.startDate;
        this.endDate = range.endDate;
    }

    /**
     * @return Returns the startDate.
     */
    public final Date getStartDate() {
        return this.startDate;
    }

    /**
     * @return Returns the endDate.
     */
    public final Date getEndDate() {
        return this.endDate;
    }

}
