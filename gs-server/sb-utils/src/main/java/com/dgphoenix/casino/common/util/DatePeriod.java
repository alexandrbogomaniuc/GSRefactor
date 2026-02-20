package com.dgphoenix.casino.common.util;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.Date;

/**
 * User: flsh
 * Date: 06.04.2009
 */
public class DatePeriod implements Serializable, KryoSerializable {
    private Date startDate;
    private Date endDate;

    public DatePeriod() {
    }

    public DatePeriod(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isDateBetween(Date date) {
        if (startDate == null) {
            throw new IllegalArgumentException("StartDate is null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("EndDate is null");
        }
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }
        return date.getTime() > startDate.getTime() && date.getTime() < endDate.getTime();
    }

    @Override
    public String toString() {
        return "DatePeriod [" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ']';
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(startDate.getTime(), true);
        output.writeLong(endDate.getTime(), true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        startDate = new Date(input.readLong(true));
        endDate = new Date(input.readLong(true));
    }
}
