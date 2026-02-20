package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ICrashCancelBet;
import com.betsoft.casino.utils.TObject;

import java.util.StringJoiner;

public class CrashCancelAutoEjectResponse extends TObject {
    private String betId;
    private String nickname;

    public CrashCancelAutoEjectResponse(long date, int rid, String betId, String nickname) {
        super(date, rid);
        this.betId = betId;
        this.nickname = nickname;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashCancelAutoEjectResponse.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("betId='" + betId + "'")
                .add("nickname='" + nickname + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CrashCancelAutoEjectResponse that = (CrashCancelAutoEjectResponse) o;

        return betId.equals(that.betId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + betId.hashCode();
        return result;
    }
}

