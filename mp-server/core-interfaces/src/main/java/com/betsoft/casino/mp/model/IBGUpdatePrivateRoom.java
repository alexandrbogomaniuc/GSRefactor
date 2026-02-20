package com.betsoft.casino.mp.model;

import java.util.List;

public interface IBGUpdatePrivateRoom {
    public String getPrivateRoomId();

    public List<IBGPlayer> getPlayers();

    public int getBankId();
}
