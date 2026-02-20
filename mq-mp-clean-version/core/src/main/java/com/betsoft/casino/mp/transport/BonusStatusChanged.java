package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 13.07.2020.
 */
public class BonusStatusChanged extends TObject {
    private long id;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private String type;

    public BonusStatusChanged(long date, int rid, long id, String oldStatus, String newStatus, String reason, String type) {
        super(date, rid);
        this.id = id;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BonusStatusChanged [");
        sb.append("id=").append(id);
        sb.append(", oldStatus='").append(oldStatus).append('\'');
        sb.append(", newStatus='").append(newStatus).append('\'');
        sb.append(", reason='").append(reason).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
