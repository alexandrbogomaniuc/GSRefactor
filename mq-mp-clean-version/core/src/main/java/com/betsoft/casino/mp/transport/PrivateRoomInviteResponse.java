package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.room.IPrivateRoomInviteResponse;
import com.betsoft.casino.utils.TInboundObject;

public class PrivateRoomInviteResponse extends TInboundObject implements IPrivateRoomInviteResponse {
    private boolean successful;

    public PrivateRoomInviteResponse(long date, int rid, boolean successful) {
        super(date, rid);
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    @Override
    public String toString() {
        return "PrivateRoomInvite{" +
                "successful='" + successful + '\'' +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
