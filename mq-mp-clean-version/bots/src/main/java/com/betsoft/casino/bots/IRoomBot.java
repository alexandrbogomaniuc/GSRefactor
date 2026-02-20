package com.betsoft.casino.bots;

import com.betsoft.casino.bots.strategies.IRoomBotStrategy;
import com.betsoft.casino.mp.model.ICrashGameInfo;
import com.betsoft.casino.mp.model.IGetRoomInfoResponse;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.transport.RoomEnemy;

import java.util.List;

public interface IRoomBot extends IBot {

    void setBalance(Long balance);

    long getBalance();

    IRoomBotStrategy getStrategy();

    void clearAmmo();

    void addEnemy(IRoomEnemy enemy);

    void addEnemies(List<IRoomEnemy> enemies);

    void removeEnemy(long enemyId);

    void setRoomEnemies(List<RoomEnemy> enemies);

    void setRoomInfo(IGetRoomInfoResponse roomInfo);

    void setRoomInfo(ICrashGameInfo roomInfo);

    void setRoomId(long roomId);

    void setRoomState(RoomState state);

    RoomState getRoomState();

    void setState(BotState state, String reason);

    BotState getState();

    long getLastStateChangeDate();

    void clearShotRequests();

    boolean shot();

    void sendCloseRoomRequest();

    void sendSitOutRequest();

    void sendSitInRequest(int failedCount);

    void sendBuyInRequest(int failedCount);

    default void sendReBuyInRequest(int failedCount){};

    void setNickname(String nickname);

    String getNickname();

    void activateWeapon(int weaponId);

    void addWeapon(int weaponId, int shots);

    void updateWeapon(int weaponId, int shots);

    double getRoomStake();

    void doActionWithSleep(String debugInfo);

    void doActionWithSleep(long waitTime, String debugInfo);

    void doActionWithSleep(long waitTime, String debugInfo, boolean logDebug);

    void setDefaultWeapon();

    int getCurrentBeLevel();

    void setCurrentBeLevel(int currentBeLevel);

    int getServerAmmo();

    void setServerAmmo(int serverAmmo);

    void addServerAmmo(int serverAmmo);

    int getSeatId();

    void setSeatId(int seatId);

    default boolean isBattleBot(){
        return false;
    }

    default boolean isMqbBattleBot(){
        return false;
    }

    default boolean isUsualActionBot(){
        return true;
    }

    default long getWaitTimeAfterSwitchWeapon() {
        return 1000;
    }

    default long getLastReceivedServerTime(){return 0;}

    default void setLastReceivedServerTime(long lastReceivedServerTime){}

    default void resetFocusedEnemy(){}

    default void sentCheckPendingStatus(int failedCount){}

    default boolean isAllowedForShot(IRoomEnemy roomEnemy) {
        return true;
    }
}
