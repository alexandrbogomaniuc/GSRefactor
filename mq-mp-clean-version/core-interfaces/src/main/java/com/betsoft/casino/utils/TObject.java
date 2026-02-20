package com.betsoft.casino.utils;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 03.06.17.
 */
public abstract class TObject implements ITransportObject, Serializable {
    public static final int SERVER_RID = -1;
    public static final int FREQUENCY_LIMIT = 500;
    protected long date;
    protected int rid;

    public TObject(long date, int rid) {
        this.date = date;
        this.rid = rid;
    }

    @Override
    public int getFrequencyLimit() {
        return FREQUENCY_LIMIT;
    }

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public int getRid() {
        return rid;
    }

    @Override
    public void setRid(int rid) {
        this.rid = rid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TObject tObject = (TObject) o;

        if (date != tObject.date) return false;
        return rid == tObject.rid;
    }

    @Override
    public int hashCode() {
        int result = (int) (date ^ (date >>> 32));
        result = 31 * result + rid;
        return result;
    }

    //may be overrided for prevent class name collisions
    @Override
    public String getClassName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "TObject(" + getClassName() + ")[" +
                "date=" + date +
                ", rid=" + rid +
                ']';
    }
}
