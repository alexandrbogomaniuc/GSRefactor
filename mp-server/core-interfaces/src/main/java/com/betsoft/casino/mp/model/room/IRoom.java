package com.betsoft.casino.mp.model.room;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.InboundObject;
import com.abs.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ILongIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 21.09.17.
 */
@SuppressWarnings("rawtypes")
public interface IRoom<GAME extends IGame, MAP extends IMap, SEAT extends ISeat, SNAPSHOT extends IGameRoomSnapshot, ENEMY extends IEnemy,
        ENEMY_TYPE extends IEnemyType, ROOM_INFO extends IRoomInfo, RPI extends IRoomPlayerInfo> extends Identifiable, IPlayEventConsumer, IBuyInPostProcessor {
    int DEFAULT_STAKES_RESERVE = 300;
    int DEFAULT_STAKES_LIMIT = 100;

    GameType getGameType();

    IRoomEnemy convert(ENEMY enemy, boolean fillTrajectory);

    ROOM_INFO getRoomInfo();

    ILobbySessionService getLobbySessionService();

    default boolean isBattlegroundMode() {
        return false;
    }

    GAME getGame();

    MAP getMap();

    int getMapId();

    int getNextMapId();

    SNAPSHOT getSnapshot();

    default long getId() {
        return getRoomInfo().getId();
    }

    default String getName() {
        return getRoomInfo().getName();
    }

    default short getMaxSeats() {
        return getRoomInfo() != null ? getRoomInfo().getMaxSeats() : 1;
    }

    default short getMinSeats() {
        return getRoomInfo().getMinSeats();
    }

    //return seats without null and with CrashBetsCount > 0
    default List<SEAT> getRealSeats() {
        return Collections.emptyList();
    }

    short getSeatsCount();

    short getRealSeatsCount();

    default List<Long> getRealSeatsAccountId() {
        return Collections.emptyList();
    }

    default short getRealConfirmedSeatsCount() {
        return 0;
    }

    default List<Integer> getRealConfirmedSeatsId() {
        return new ArrayList<>();
    }

    default float getMinBuyIn() {
        return getRoomInfo().getMinBuyIn();
    }

    RoomState getState();

    IGameState getGameState();

    List<SEAT> getAllSeats();

    //return seats without null
    List<SEAT> getSeats();

    void saveSeat(int number, SEAT seat);

    void removeSeat(int number, SEAT seat);

    int getSeatNumber(SEAT seat);

    void setSeatNumber(SEAT seat, int number);

    int getObserverCount();

    Collection<IGameSocketClient> getObservers();

    IGameSocketClient getObserver(Long accountId);

    IGameSocketClient getObserver(String nickname);

    void persistCashBonusSession(IActiveCashBonusSession session);

    void persistTournamentSession(ITournamentSession session);

    long getBalance(SEAT seat);

    void makeBuyInForCashBonus(SEAT seat);

    void makeBuyInForTournament(SEAT seat);

    ILobbySession updateCashBonus(SEAT seat, long balance, long betSum);

    ILobbySession updateTournamentSession(SEAT seat, long balance, long betSum);

    ILobbySession setBalance(SEAT seat, long balance);

    void clearSeatDataFromPreviousRound();

    SEAT getSeat(int number);

    SEAT getSeatByAccountId(long accountId);

    void start() throws CommonException;

    SNAPSHOT shutdown() throws CommonException;

    boolean shutdownRoomIfEmpty() throws CommonException;

    SEAT tryReconnect(SEAT seat) throws CommonException;

    SEAT createSeat(RPI playerInfo, IGameSocketClient socketClient, double currentRate);

    int processSitIn(SEAT seat, ISitIn request) throws CommonException;

    void rollbackSitIn(SEAT seat);

    void convertBulletsToMoney();

    SEAT processSitOut(IGameSocketClient client, ISitOut request, int seatNumber, long accountId, boolean updateStats)
            throws CommonException;

    ITransportObject processOpenRoom(IGameSocketClient client, IOpenRoom request, String currency) throws CommonException;

    void processCloseRoom(IGameSocketClient client, ICloseRoom request) throws CommonException;

    void processCloseRoom(long accountId) throws CommonException;

    ITransportObject getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client);

    IGetRoomInfoResponse getRoomInfoResponse(int requestId, IGameSocketClient client, String currency) throws CommonException;

    void sendChanges(ITransportObject allMessage);

    void sendChangesToObserversOnly(ITransportObject allMessage);

    void sendChanges(ITransportObject allMessage, ITransportObject seatMessage, long seatAccountId, InboundObject inboundObject);

    void sendMessageToPlayer(ITransportObject message, long seatAccountId);

    boolean isBuyInAllowed(SEAT seat);

    boolean tryChangeBetLevel(long accountId, int betLevel);

    default void closeResults(SEAT seat) {
    }

    ILongIdGenerator getIdGenerator();

    Trajectory convertTrajectory(Trajectory origin, long currentTime);

    Trajectory convertFullTrajectory(Trajectory origin);

    Logger getLog();

    long getCurrentTime();

    //small hack
    static int extractServerId(String sid) {
        //fast fix for sb branch:
        if (StringUtils.isTrimmedEmpty(sid) || !sid.contains("_")) {
            return 1;
        }
        StringTokenizer st = new StringTokenizer(sid, "_");
        return Integer.parseInt(st.nextToken());
    }

    void restartTimer() throws CommonException;

    void startTimer() throws CommonException;

    void stopTimer() throws CommonException;

    void startUpdateTimer();

    void stopUpdateTimer();

    boolean isTimerStopped();

    void setTimerTime(long time) throws CommonException;

    long getTimerTime();

    long getTimerElapsed();

    long getTimerRemaining();

    void updateRoomInfo(IRoomInfoUpdater updater);

    void reloadRoomInfo();

    void setGameState(IGameState<?, ?, ?, ?> gameState) throws CommonException;

    void updateStats();

    void sendRoundResults();

    default Map<Long, IRoundResult> getExtResultsAndSendForLocal(){
        return Collections.emptyMap();
    }

    default void sendRoundResultsForExtSeats(Map<Long, IRoundResult> roundResults){}
    default void sendRoundResultForExtObserversWithNoSeats(IRoundResult roundResult){}

    void resetRoundResults();

    void removeDisconnectedObservers();
    void removeAllEnemies();
    int getRoundDuration();
    void toggleMap();
    double getExpScale(SEAT seat);
    IGameConfigService getGameConfigService();
    IRoomPlayerInfoService getPlayerInfoService();
    void sendStartNewRoundToAllPlayers(List<ISeat> seats);
    ITransportObjectsFactoryService getTOFactoryService();
    void changeBonusStatus(IActiveCashBonusSession newCashBonusSession) throws CommonException;
    void changeFRBBonusStatus(IActiveFrbSession activeFrbSession) throws CommonException;
    void changeTournamentState(ITournamentSession session) throws CommonException;

    void fireReBuyAccepted(SEAT seat) throws CommonException;

    void executeOnAllMembers(Runnable task);

    Runnable createSendSeatOwnerMessageTask(ITransportObject message);

    Runnable createSendSeatsMessageTask(Long relatedAccountId, boolean notSendToRelatedAccountId, long relatedRequestId, ITransportObject message,
                                        boolean sendToAllObservers);

    Runnable createUpdateCrashHistoryTask(ICrashRoundInfo crashRoundInfo);

    Runnable createSendSeatMessageTask(Long accountId, ITransportObject message);

    Runnable createSendAllObserversNoSeatMessageTask(ITransportObject message);

    default boolean isNotAllowPlayWithAnyPendingPlayers() {
        return true;
    }

    void lock();

    void unlock();

    boolean isLocked();

    boolean tryLock(long timeout, TimeUnit timeUnit) throws InterruptedException;

    void removeObserverByAccountId(long accountId);

    void registerStateChangedListener(IRoomStateChangedListener listener);

    void registerSeatsCountChangedListener(ISeatsCountChangedListener listener);

    void registerOpenRoomListener(IRoomOpenedListener listener);

    void registerCloseRoomListener(IRoomClosedListener listener);

    void removeSeatsWithPendingOperations();

    boolean hasNotReadyNotKickedSeat();

    default void clearKickedPlayers(){}

    default void removePlayersFromPrivateRoom() {};

    default void updateRoomInfoForDeactivation() {};
}
