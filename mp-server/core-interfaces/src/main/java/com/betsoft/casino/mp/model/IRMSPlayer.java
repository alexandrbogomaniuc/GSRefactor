package com.betsoft.casino.mp.model;

public interface IRMSPlayer {
    public int getServerId();

    public String getNickname();

    public boolean isIsOwner();

    public String getSessionId();

    public int getSeatNr();
}
