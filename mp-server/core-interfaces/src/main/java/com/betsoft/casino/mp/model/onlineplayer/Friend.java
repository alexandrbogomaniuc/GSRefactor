package com.betsoft.casino.mp.model.onlineplayer;

public class Friend {

    private String nickname;
    private boolean online;

    public Friend() {
    }

    public Friend(String nickname, boolean online) {
        this.nickname = nickname;
        this.online = online;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "nickname='" + nickname + '\'' +
                ", online=" + online +
                '}';
    }
}
