package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.web.IGameSocketClient;

import java.util.List;

public interface IBattlegroundRoom<GAME extends IGame, MAP extends IMap, SEAT extends ISeat, SNAPSHOT extends IGameRoomSnapshot,
        ENEMY extends IEnemy, ENEMY_TYPE extends IEnemyType, ROOM_INFO extends IRoomInfo, RPI extends IBattlegroundRoomPlayerInfo>
        extends IRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI> {

    default boolean isBattlegroundMode() {
        return true;
    }

    void sentBattlegroundMessageToPlayers(List<Integer> oldKings, int rid);

    void sitOutAllPlayersWithoutConfirmedRebuy();

    void sitOutAllDisconnectedPlayers();

    default int getCurrentPowerUpMultiplierBySeat(SEAT alreadySeat) {
        return 0;
    }

    /**
     * mark not ready player as kicked and send response to kicked player. Ready = is seater
     * @param client game socket client of player
     */
    void kickNotReadyPlayer(IGameSocketClient client);

    /**
     * mark ready player as kicked, send response to kicked player and sit out player from room. Ready = is seater
     * @param client game socket client of player
     */
    void kickReadyPlayer(IGameSocketClient client);
}
