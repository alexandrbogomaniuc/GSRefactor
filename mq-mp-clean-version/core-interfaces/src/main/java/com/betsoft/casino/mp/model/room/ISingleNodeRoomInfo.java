package com.betsoft.casino.mp.model.room;

import java.util.Map;

/**
 * User: flsh
 * Date: 24.12.2021.
 */
public interface ISingleNodeRoomInfo extends IRoomInfo {
    void setGameServerId(int gameServerId);

    int getGameServerId();

    void removePlayerFromWaitingOpenRoom(Long accountId);

    void addPlayerToWaitingOpenRoom(Long accountId);

    Map<Long, Long> getWaitingOpenRoomPlayersWithCheck();

}
