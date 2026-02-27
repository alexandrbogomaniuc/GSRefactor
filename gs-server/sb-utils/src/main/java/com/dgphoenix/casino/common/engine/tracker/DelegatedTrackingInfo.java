package com.abs.casino.common.engine.tracker;

/**
 * User: Grien
 * Date: 02.04.2014 13:47
 */
public class DelegatedTrackingInfo {
    private ICommonTrackingTaskDelegate delegate;
    private String trackingObjectId;
    private long lastTrackDate;

    public DelegatedTrackingInfo() {
    }

    public DelegatedTrackingInfo(String trackingObjectId, long lastTrackDate, ICommonTrackingTaskDelegate delegate) {
        this.delegate = delegate;
        this.trackingObjectId = trackingObjectId;
        this.lastTrackDate = lastTrackDate;
    }

    public String getTrackingObjectId() {
        return trackingObjectId;
    }

    public void setTrackingObjectId(String trackingObjectId) {
        this.trackingObjectId = trackingObjectId;
    }

    public long getLastTrackDate() {
        return lastTrackDate;
    }

    public void setLastTrackDate(long lastTrackDate) {
        this.lastTrackDate = lastTrackDate;
    }

    public ICommonTrackingTaskDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(ICommonTrackingTaskDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DelegatedTrackingInfo)) return false;
        DelegatedTrackingInfo that = (DelegatedTrackingInfo) o;
        if (lastTrackDate != that.lastTrackDate) return false;
        if (delegate != null ? !delegate.equals(that.delegate) : that.delegate != null) return false;
        if (trackingObjectId != null ? !trackingObjectId.equals(that.trackingObjectId) : that.trackingObjectId != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return trackingObjectId != null ? trackingObjectId.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DelegatedTrackingInfo");
        sb.append("[delegate=").append(delegate);
        sb.append(", trackingObjectId='").append(trackingObjectId).append('\'');
        sb.append(", lastTrackDate=").append(lastTrackDate);
        sb.append(']');
        return sb.toString();
    }
}
