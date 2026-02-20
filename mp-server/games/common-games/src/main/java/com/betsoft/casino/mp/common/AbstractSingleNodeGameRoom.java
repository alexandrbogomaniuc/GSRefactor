package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.ISingleNodeRoom;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.betsoft.casino.mp.utils.ErrorCodes.*;

/**
 * User: flsh
 * Date: 17.01.2022.
 */
public abstract class AbstractSingleNodeGameRoom<GAME extends IGame, MAP extends IMap, SEAT extends ISingleNodeSeat,
        SNAPSHOT extends IGameRoomSnapshot, ENEMY extends IEnemy, ENEMY_TYPE extends IEnemyType, ROOM_INFO extends ISingleNodeRoomInfo,
        RPI extends IRoomPlayerInfo>
        extends AbstractGameRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI>
        implements ISingleNodeRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI> {
    protected SEAT[] seats;
    protected transient ReentrantLock roomLock = new ReentrantLock();

    @SuppressWarnings("rawtypes")
    public AbstractSingleNodeGameRoom(ApplicationContext context, Logger logger, SEAT[] seats, ROOM_INFO roomInfo, GAME game, MAP map,
                                      IPlayerStatsService playerStatsService, IPlayerQuestsService playerQuestsService, IWeaponService weaponService,
                                      IExecutorService remoteExecutorService, IPlayerProfileService playerProfileService,
                                      IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                                      IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService) {
        super(context, logger, roomInfo, game, map, playerStatsService, playerQuestsService, weaponService, remoteExecutorService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService);
        this.seats = seats;
    }

    @Override
    protected IRoomInfoService getRoomInfoService(ApplicationContext context) {
        //todo possible need rework
        IRoomInfoService bean = roomInfo != null && roomInfo.isPrivateRoom() ?
                (IRoomInfoService) context.getBean("bgPrivateRoomInfoService") : (IRoomInfoService) context.getBean("singleNodeRoomInfoService");
        if (bean != null && bean.isInitialized()) {
            return bean;
        } else {
            getLog().warn("roomInfoService not initialized");
            return null;
        }
    }

    @Override
    public GameType getGameType() {
        return null;
    }

    @Override
    public SNAPSHOT getSnapshot() {
        return null;
    }

    @Override
    public SEAT createSeat(RPI playerInfo, IGameSocketClient socketClient, double currentRate) {
        return null;
    }

    @Override
    public ITransportObject getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client) {
        return null;
    }

    @Override
    public IGetRoomInfoResponse getRoomInfoResponse(int requestId, IGameSocketClient client, String currency) throws CommonException {
        return null;
    }

    @Override
    public Logger getLog() {
        return null;
    }

    @Override
    protected IGameState getWaitingPlayersGameState() {
        return null;
    }

    @Override
    protected List<ITransportEnemy> getTransportEnemies() {
        return null;
    }

    @Override
    protected void calculateWeaponsSurplusCompensation(SEAT seat) {

    }

    @Override
    protected List<Integer> getWeaponLootBoxPrices() {
        return null;
    }

    @Override
    public List<SEAT> getAllSeats() {
        return seats == null ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(seats));
    }

    @Override
    public int processSitIn(SEAT seat, ISitIn request) throws CommonException {
        checkAndStartRoom();
        if (seats.length == 1 && seats[0] != null && seats[0].getPlayerInfo().getId() != seat.getPlayerInfo().getId()) {
            getLog().error("processSitIn: single room full, newSeat={}, occupied={}", seat, seats[0]);
            return TOO_MANY_PLAYER;
        }
        lock();
        try {
            IGameState<?, ?, ?, ?> gameState = getGameState();
            if (!gameState.isSitInAllowed()) {
                getLog().error("sitIn is not allowed during {}", gameState.getRoomState());
                return NOT_ALLOWED_SITIN;
            }
            try {
                getLog().debug("AbstractSingleNodeGameRoom processSitIn seats size: {}", seats.length);
                for (int i = 0; i < seats.length; i++) {
                    if (seats[i] == null) {
                        seats[i] = seat;
                        seat.setNumber(i);
                        IGameSocketClient socketClient = seat.getSocketClient();
                        socketClient.setSeatNumber(i);
                        socketClient.setSeat(seat);
                        finishSitIn(seat);
                        getLog().info("AbstractSingleNodeGameRoom processSitIn: success,  getAccountId: {}, getNickname: {} ",
                                seat.getAccountId(), seat.getNickname());
                        return OK;
                    } else {
                        getLog().debug("AbstractSingleNodeGameRoom processSitIn seats size[{}]: getAccountId: {}, getNickname: {}",
                                i,  seats[i].getAccountId(), seats[i].getNickname());
                    }
                }
            } catch (Exception e) {
                getLog().error("processSitIn error, seat={}, occupiedId={}, state={}", seat, seat.getNumber(),
                        getGameState(), e);
                seats[seat.getNumber()] = null;
                seat.setNumber(-1);
            }
        } finally {
            unlock();
        }
        return TOO_MANY_PLAYER;
    }

    @Override
    public void saveSeat(int number, SEAT seat) {
        if (seat != null && seat.getNumber() == -1) {
            getLog().debug("Possible error! saving seat with -1 id seat:{}, number={}", seat, number);
        }
        seats[number] = seat;
    }

    @Override
    public void removeSeat(int number, SEAT seat) {
        rememberRemovedSeat(seat);
        seats[number] = null;
    }

    @Override
    public int getSeatNumber(SEAT seat) {
        return seat.getNumber();
    }

    @Override
    public void setSeatNumber(SEAT seat, int number) {
        seat.setNumber(number);
    }

    @Override
    public void lock() {
        getLog().debug("lock start");
        roomLock.lock();
        getLog().debug("lock end");
    }

    @Override
    public void unlock() {
        getLog().debug("unlock");
        roomLock.unlock();
    }

    @Override
    public boolean isLocked() {
        return roomLock.isLocked();
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return roomLock.tryLock(timeout, timeUnit);
    }
}
