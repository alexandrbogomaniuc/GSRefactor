package com.betsoft.casino.mp.model.room;

import com.betsoft.casino.mp.model.*;

import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 28.12.2021.
 */
public interface IMultiNodeRoom<GAME extends IGame, MAP extends IMap, SEAT extends ISeat,
        SNAPSHOT extends IGameRoomSnapshot, ENEMY extends IEnemy, ENEMY_TYPE extends IEnemyType, ROOM_INFO extends IMultiNodeRoomInfo,
        RPI extends IRoomPlayerInfo>
        extends IRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI> {

    void addSeatFromOtherServer(SEAT seat);

    Class<SEAT> getSeatClass();

    ISharedGameStateService getSharedGameStateService();

    void lockSeat(long accountId);

    boolean tryLockSeat(long accountId, long time, TimeUnit timeunit) throws InterruptedException;

    void unlockSeat(long accountId);

    int getAllowedPlayers();

    boolean isRoomFull();

    boolean isRoomFullOrManyObservers();
}
