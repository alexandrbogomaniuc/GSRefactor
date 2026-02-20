package com.betsoft.casino.mp.model;

import java.util.List;

public interface IRoomPlayersMonitorService {

    void finishGameSessionAndMakeSitOutAsync(int serverId, String sid, String privateRoomId);
    boolean finishGameSessionAndMakeSitOut(int serverId, String sid, String privateRoomId);

    void pushOnlineRoomsPlayersAsync(int serverId, List<IRMSRoom> trmsRooms);
    boolean pushOnlineRoomsPlayers(int serverId, List<IRMSRoom> trmsRooms);

    void saveRoomsPlayersAsync(int serverId, List<IRMSRoom> trmsRooms);
    boolean saveRoomsPlayers(int serverId, List<IRMSRoom> trmsRooms);
}
