package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.room.IFinishGameSessionResponse;
import com.betsoft.casino.utils.TInboundObject;

public class FinishGameSessionResponse extends TInboundObject implements IFinishGameSessionResponse {
    private boolean successful;

    public FinishGameSessionResponse(long date, int rid, boolean successful) {
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
        return "FinishGameSessionResponse[" +
                "successful=" + successful +
                ", date=" + date +
                ", rid=" + rid +
                ']';
    }
}
