package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

public class CancelKick extends TInboundObject{
    private String nickname;

    public CancelKick(long date, int rid, String nickname) {
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
        return "CancelKick{" +
                "nickname='" + nickname + '\'' +
                ", date=" + date +
                ", rid=" + rid +
                '}';
    }
}
