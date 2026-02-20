package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.IRoomBattlegroundInfo;
import com.betsoft.casino.utils.ITransportObject;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IGetRoomInfoResponse<SEAT extends ITransportSeat, ENEMY extends ITransportEnemy,
        ROOM_ENEMY extends IRoomEnemy, MINE_PLACE extends IMinePlace> extends ITransportObject {
    long getRoomId();

    String getName();

    short getMaxSeats();

    double getMinBuyIn();

    double getStake();

    double getPlayerStake();

    RoomState getState();

    List<SEAT> getSeats();

    int getTtnx();

    int getWidth();

    int getHeight();

    List<ENEMY> getEnemies();

    List<ROOM_ENEMY> getRoomEnemies();

    int getAlreadySitInNumber();

    long getAlreadySitInAmmoCount();

    long getAlreadySitInBalance();

    double getAlreadySitInWin();

    int getMapId();

    String getSubround();

    List<Integer> getAmmoValues();

    List<MINE_PLACE> getMines();

    Map<Long, Integer> getFreezeTime();

    boolean isImmortalBoss();

    long getRoundId();

    Map<Integer, Integer> getSeatGems();

    void setState(RoomState state);

    Integer getFragments();

    void setFragments(Integer fragments);

    int getBossNumberShots();

    void setBossNumberShots(int bossNumberShots);

    boolean isNeedWaitingWhenEnemiesLeave();

    void setNeedWaitingWhenEnemiesLeave(boolean needWaitingWhenEnemiesLeave);

    Long getEndTime();

    void setEndTime(Long endTime);

    Map<Integer, Double> getGemPrizes();

    void setGemPrizes(Map<Integer, Double> gemPrizes);

    public IRoomBattlegroundInfo getBattlegroundInfo();

    boolean isOwner();

    void setOwner(boolean isOwner);

    Boolean getKicked();

    void setKicked(Boolean kicked);
}
