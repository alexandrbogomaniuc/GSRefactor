package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class FinishGameSession extends TInboundObject {
    private String sid;
    private String privateRoomId;

    public FinishGameSession(long date, String sid, String privateRoomId, int rid) {
        super(date, rid);
        this.sid = sid;
        this.privateRoomId = privateRoomId;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public void setPrivateRoomId(String privateRoomId) {
        this.privateRoomId = privateRoomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FinishGameSession that = (FinishGameSession) o;

        if (rid != that.rid) return false;
        return sid.equals(that.sid);

    }

    @Override
    public int hashCode() {
        int result = sid.hashCode();
        result = 31 * result + rid;
        return result;
    }

    @Override
    public String toString() {
        return "FinishGameSession{" +
                "date=" + date +
                ", rid=" + rid +
                ", sid='" + sid + '\'' +
                ", privateRoomId=" + privateRoomId +
                '}';
    }
}
