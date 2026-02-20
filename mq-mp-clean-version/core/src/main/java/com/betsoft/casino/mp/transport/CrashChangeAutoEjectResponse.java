package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TObject;

import java.util.StringJoiner;

public class CrashChangeAutoEjectResponse extends TObject {
    private String betId;
    private String nickname;

    public CrashChangeAutoEjectResponse(long date, int rid, String betId, String nickname) {
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
        return new StringJoiner(", ", CrashChangeAutoEjectResponse.class.getSimpleName() + "[", "]")
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

        CrashChangeAutoEjectResponse that = (CrashChangeAutoEjectResponse) o;

        return betId.equals(that.betId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + betId.hashCode();
        return result;
    }
}

