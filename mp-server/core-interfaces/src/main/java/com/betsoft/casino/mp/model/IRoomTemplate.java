package com.betsoft.casino.mp.model;

import com.dgphoenix.casino.common.cache.Identifiable;

/**
 * User: flsh
 * Date: 21.05.2020.
 */
public interface IRoomTemplate extends Identifiable {
    void setId(long id);

    long getBankId();

    void setBankId(long bankId);

    int getInitialRooms();

    void setInitialRooms(int initialRooms);

    int getMinFreeRooms();

    void setMinFreeRooms(int minFreeRooms);

    GameType getGameType();

    void setGameType(GameType gameType);

    short getMaxSeats();

    void setMaxSeats(short maxSeats);

    short getMinSeats();

    void setMinSeats(short minSeats);

    MoneyType getMoneyType();

    void setMoneyType(MoneyType moneyType);

    int getWidth();

    void setWidth(int width);

    int getHeight();

    void setHeight(int height);

    int getMinBuyIn();

    void setMinBuyIn(int minBuyIn);

    int getMaxRooms();

    void setMaxRooms(int maxRooms);

    String getName();

    void setName(String name);

    int getRoundDuration();

    void setRoundDuration(int roundDuration);

    boolean isBattlegroundMode();

    void setBattlegroundMode(boolean battlegroundMode);

    long getBattlegroundBuyIn();

    void setBattlegroundBuyIn(long battlegroundBuyIn);

    int getBattlegroundAmmoAmount();

    void setBattlegroundAmmoAmount(int battlegroundAmmoAmount);

    boolean isPrivateRoom();

    void setPrivateRoom(boolean privateRoom);
}
