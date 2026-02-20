package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

/**
 * User: flsh
 * Date: 16.07.18.
 */
public class ChangeNickname extends TInboundObject {
    private String nickname;

    public ChangeNickname(long date, int rid, String nickname) {
        super(date, rid);
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChangeNickname [");
        sb.append("nickname='").append(nickname).append('\'');
        sb.append(", date=").append(date);
        sb.append(", rid=").append(rid);
        sb.append(']');
        return sb.toString();
    }
}
