package com.dgphoenix.casino.common.engine.tracker;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 17.10.13
 */
public class TrackingInfo implements Serializable, Comparable<TrackingInfo> {
    private long trackingObjectId;
    private long lastTrackDate;

    public TrackingInfo() {
    }

    public TrackingInfo(long trackingObjectId, long lastTrackDate) {
        this.trackingObjectId = trackingObjectId;
        this.lastTrackDate = lastTrackDate;
    }

    public long getTrackingObjectId() {
        return trackingObjectId;
    }

    public void setTrackingObjectId(long trackingObjectId) {
        this.trackingObjectId = trackingObjectId;
    }

    public long getLastTrackDate() {
        return lastTrackDate;
    }

    public void setLastTrackDate(long lastTrackDate) {
        this.lastTrackDate = lastTrackDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackingInfo that = (TrackingInfo) o;

        if (trackingObjectId != that.trackingObjectId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (trackingObjectId ^ (trackingObjectId >>> 32));
    }

    @Override
    public int compareTo(TrackingInfo o) {
        return (trackingObjectId < o.trackingObjectId) ? -1 : ((trackingObjectId == o.trackingObjectId) ? 0 : 1);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TrackingInfo [");
        sb.append("trackingObjectId=").append(trackingObjectId);
        sb.append(", lastTrackDate=").append(lastTrackDate);
        sb.append(']');
        return sb.toString();
    }
}
