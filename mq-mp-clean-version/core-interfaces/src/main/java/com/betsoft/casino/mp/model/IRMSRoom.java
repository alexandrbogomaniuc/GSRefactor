package com.betsoft.casino.mp.model;

import java.util.List;

public interface IRMSRoom {
    public long getRoomId();

    public int getServerId();

    public boolean isIsActive();

    public boolean isIsBattleground();

    public boolean isIsPrivate();

    public long getBuyInStake();

    public String getCurrency();

    public long getGameId();

    public String getGameName();

    public List<IRMSPlayer> getPlayers();
}
