package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

/**
 * User: flsh
 * Date: 13.07.2020.
 */
public class TournamentStateChanged extends TObject {
    private long id;
    private String oldState;
    private String newState;
    private String reason;

    public TournamentStateChanged(long date, int rid, long id, String oldState, String newState, String reason) {
        super(date, rid);
        this.id = id;
        this.oldState = oldState;
        this.newState = newState;
        this.reason = reason;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOldState() {
        return oldState;
    }

    public void setOldState(String oldState) {
        this.oldState = oldState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TournamentStateChanged [");
        sb.append("id=").append(id);
        sb.append(", oldState='").append(oldState).append('\'');
        sb.append(", newState='").append(newState).append('\'');
        sb.append(", reason='").append(reason).append('\'');
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
