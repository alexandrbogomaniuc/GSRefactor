package com.betsoft.casino.mp.model.room;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.service.IRoomPlayerInfoService;
import com.abs.casino.common.cache.Identifiable;

import java.util.Set;

/**
 * User: flsh
 * Date: 09.11.17.
 */
public interface IRoomInfo extends Identifiable {
    int NOT_ASSIGNED_ID = -1;

    long getTemplateId();

    long getBankId();

    GameType getGameType();

    void register(long serverId);

    void startNewRound(long roundId);

    long getLastRoundStartDate();

    boolean isClosed();

    void close();

    void open();

    void unregister();

    long getUpdateDate();

    void setUpdateDate(long updateDate);

    String getName();

    long getRoundId();

    short getMaxSeats();

    short getMinSeats();

    short getSeatsCount(IRoomPlayerInfoService roomPlayerInfoService);

    MoneyType getMoneyType();

    boolean isBonusSession();

    RoomState getState();

    void setState(RoomState state);

    int getWidth();

    int getHeight();

    float getMinBuyIn();

    Money getStake();

    int getMapId();

    String getCurrency();

    int getRoundDuration();

    void setRoundDuration(int duration);

    boolean isBattlegroundMode();

    void setBattlegroundMode(boolean battlegroundMode);

    long getBattlegroundBuyIn();

    void setBattlegroundBuyIn(long battlegroundBuyIn);

    int getBattlegroundAmmoAmount();

    void setBattlegroundAmmoAmount(int battlegroundAmmoAmount);

    default boolean isPrivateRoom() {
        return false;
    }

    default boolean isDeactivated() {
        return false;
    }
    default void setDeactivated(boolean isDeactivated) {}

    default long getDeactivationTime() {
        return 0;
    }

    default long getOwnerAccountId(){
        return 0;
    }

    default String getJoinUrl() {
        return null;
    }

    default int getCountGamesPlayed() {
        return -1;
    }

    default void setLastTimeActivity(long lastTimeActivity) {
    }

    default long getLastTimeActivity() {
        return -1;
    }

    default void updateLastTimeActivity() {

    }

    default void incrementCountGamesPlayed() {

    }

    default String getPrivateRoomId() {
        return "";
    }

    default String getOwnerUsername() {
        return null;
    }

    default void kickPlayer(long accountId) {}

    default void cancelKick(long accountId) {}

    default boolean isPlayerKicked(long accountId) {
        return false;
    }

    default void clearKickedPlayers() {}

    default Set<Long> getKickedPlayers() {
        return null;
    }

    default void setKickedPlayers(Set<Long> kickedPlayers) {}
}
