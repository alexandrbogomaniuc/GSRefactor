package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 16.07.18.
 */
public class CheckNicknameAvailability extends TInboundObject {
    public static final int FREQUENCY_LIMIT = 50;

    private String nickname;

    public CheckNicknameAvailability(long date, int rid, String nickname) {
        super(date, rid);
        this.nickname = nickname;
    }

    @Override
    public int getFrequencyLimit() {
        return FREQUENCY_LIMIT;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CheckNicknameAvailability [");
        sb.append("nickname='").append(nickname).append('\'');
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
