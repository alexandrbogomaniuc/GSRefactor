package com.betsoft.casino.mp.model;

import java.util.List;

public interface IBGUpdateRoomResult {
    public int getCode();

    public String getMessage();

    public String getPrivateRoomId();

    public List<IBGPlayer> getPlayers();
}
