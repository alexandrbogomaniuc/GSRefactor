package com.betsoft.casino.mp.model.room;

import com.betsoft.casino.mp.model.*;

/**
 * User: flsh
 * Date: 28.12.2021.
 */
public interface ISingleNodeRoom<GAME extends IGame, MAP extends IMap, SEAT extends ISeat,
        SNAPSHOT extends IGameRoomSnapshot, ENEMY extends IEnemy, ENEMY_TYPE extends IEnemyType, ROOM_INFO extends ISingleNodeRoomInfo,
        RPI extends IRoomPlayerInfo>
        extends IRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI> {
    default int getGameServerId() {
        return getRoomInfo().getGameServerId();
    }
}
