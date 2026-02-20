package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.ITransportObserver;
import com.betsoft.casino.mp.model.onlineplayer.Friend;
import com.betsoft.casino.utils.ITransportObject;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IFullGameInfo<TRANSPORT_OBSERVER extends ITransportObserver, ROOM_ENEMY extends IRoomEnemy, SEAT extends ITransportSeat, MINE extends IMinePlace>
        extends ITransportObject {
    List<ROOM_ENEMY> getRoomEnemies();

    RoomState getState();

    List<SEAT> getSeats();

    int getMapId();

    long getStartTime();

    String getSubround();

    List<MINE> getMines();

    Map<Long, Integer> getFreezeTime();

    boolean isImmortalBoss();

    long getRoundId();

    Map<Integer, Integer> getSeatGems();

    Integer getFragments();

    void setFragments(Integer fragments);

    int getBossNumberShots();

    void setBossNumberShots(int bossNumberShots);

    boolean isNeedWaitingWhenEnemiesLeave();

    void setNeedWaitingWhenEnemiesLeave(boolean needWaitingWhenEnemiesLeave);

    long getTimeToStart();

    void setTimeToStart(long timeToStart);

    Long getEndTime();

    void setEndTime(Long endTime);

    void setObservers(List<TRANSPORT_OBSERVER> observers);

    List<TRANSPORT_OBSERVER> getObservers();

    void setFriends(List<Friend> friend);
}
