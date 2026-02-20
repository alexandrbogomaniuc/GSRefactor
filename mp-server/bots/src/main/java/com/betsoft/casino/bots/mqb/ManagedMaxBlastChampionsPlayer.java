package com.betsoft.casino.bots.mqb;

public class ManagedMaxBlastChampionsPlayer {
    private String nickName;
    private boolean isBot;

    public ManagedMaxBlastChampionsPlayer(String nickName, boolean isBot) {
        this.nickName = nickName;
        this.isBot = isBot;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    @Override
    public String toString() {
        return "ManagedMaxBlastChampionsPlayer{" +
                "nickName='" + nickName + '\'' +
                ", isBot=" + isBot +
                '}';
    }
}
