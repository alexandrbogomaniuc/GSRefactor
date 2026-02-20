package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.room.IMultiNodeRoom;
import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.payment.PendingOperationType;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.utils.ErrorCodes.*;

/**
 * User: flsh
 * Date: 17.01.2022.
 */
public abstract class AbstractMultiNodeGameRoom<GAME extends IGame, MAP extends IMap, SEAT extends IMultiNodeSeat,
        SNAPSHOT extends IGameRoomSnapshot, ENEMY extends IEnemy, ENEMY_TYPE extends IEnemyType, ROOM_INFO extends IMultiNodeRoomInfo,
        RPI extends IRoomPlayerInfo>
        extends AbstractGameRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI>
        implements IMultiNodeRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI> {
    protected final IMultiNodeSeatService multiNodeSeatService;
    private final ISharedGameStateService gameStateService;
    private final IExecutorService remoteExecutorService;

    @SuppressWarnings("rawtypes")
    /**
     * Abstract class for common rooms with players from a lot of servers.
     */
    public AbstractMultiNodeGameRoom(ApplicationContext context, Logger logger, ROOM_INFO roomInfo, GAME game, MAP map,
                                     IPlayerStatsService playerStatsService, IPlayerQuestsService playerQuestsService, IWeaponService weaponService,
                                     IExecutorService remoteExecutorService, IPlayerProfileService playerProfileService,
                                     IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                                     IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService) {
        super(context, logger, roomInfo, game, map, playerStatsService, playerQuestsService, weaponService, remoteExecutorService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService);
        this.remoteExecutorService = remoteExecutorService;
        this.multiNodeSeatService = (IMultiNodeSeatService) context.getBean("multiNodeSeatService");
        this.gameStateService = (ISharedGameStateService) context.getBean("sharedGameStateService");
    }

    @Override
    protected IRoomInfoService getRoomInfoService(ApplicationContext context) {
        //todo possible need rework
        IRoomInfoService bean = roomInfo != null && roomInfo.isPrivateRoom()
                ? (IRoomInfoService) context.getBean("multiNodePrivateRoomInfoService")
                : (IRoomInfoService) context.getBean("multiNodeRoomInfoService");
        if (bean != null && bean.isInitialized()) {
            return bean;
        } else {
            getLog().warn("roomInfoService not initialized");
            return null;
        }
    }

    @Override
    public ISharedGameStateService getSharedGameStateService() {
        return gameStateService;
    }

    /**
     * Try re-connect player and sitIn to room
     * @param seat seat of player
     * @return SEAT return reconnected seater
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public SEAT tryReconnect(SEAT seat) throws CommonException {
        SEAT reconnectedSeat = super.tryReconnect(seat);
        //need call waitingState.firePlayersCountChanged(), this required for start play after reboot
        if (reconnectedSeat != null && getGameState().isSitInAllowed()) {
            //noinspection unchecked
            getGameState().processSitIn(seat);
        }
        return reconnectedSeat;
    }

    private boolean isPlayerSitIn(SEAT seat) {
        List<SEAT> allSeats = getAllSeats();
        boolean alreadySitIn = false;
        for (SEAT sitInSeat : allSeats) {
            if (sitInSeat.getAccountId() == seat.getAccountId()) {
                alreadySitIn = true;
                break;
            }
        }
        return alreadySitIn;
    }

    /**
     *
     * @param seat seat of player
     * @param request sitIn request from client
     * @return result OK|NOT_ALLOWED_SITIN|CANNOT_OBTAIN_LOCK|TOO_MANY_PLAYER
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public int processSitIn(SEAT seat, ISitIn request) throws CommonException {
        checkAndStartRoom();
        IGameState<?, ?, ?, ?> gameState = getGameState();
        //need fast check without locking for prevent locking in play game state
        if (!gameState.isSitInAllowed() && !isPlayerSitIn(seat)) {
            getLog().error("sitIn is not allowed during {}", gameState.getRoomState());
            return NOT_ALLOWED_SITIN;
        }
        try {
            boolean locked = tryLock(5, TimeUnit.SECONDS);
            if (!locked) {
                return CANNOT_OBTAIN_LOCK;
            }
        } catch (InterruptedException e) {
            throw new CommonException("Cannot lock");
        }
        try {
            gameState = getGameState();
            if (!gameState.isSitInAllowed()) {
                getLog().error("sitIn is not allowed during {}", gameState.getRoomState());
                return NOT_ALLOWED_SITIN;
            }
            try {
                IGameSocketClient socketClient = seat.getSocketClient();
                List<SEAT> allSeats = getAllSeats();
                boolean alreadySitIn = isPlayerSitIn(seat);
                if (!alreadySitIn) {
                    if (allSeats.size() >= getMaxSeats()) {
                        getLog().error("sitIn is not, too many players={}", allSeats.size());
                        return TOO_MANY_PLAYER;
                    }
                    saveSeat(0, seat);
                    socketClient.setSeatNumber(allSeats.size());
                    finishSitIn(seat);
                    getLog().info("processSitIn: success, seat={}", seat);
                    Runnable newSeatNotifyTask = roomInfoService.createNewSeatNotifyTask(getId(), serverConfigService.getServerId(), seat);
                    executeOnAllMembers(newSeatNotifyTask);
                }
                return OK;
            } catch (Exception e) {
                removeSeat(0, seat);
                getLog().error("processSitIn error, seat={}, state={}", seat, getGameState(), e);
            }
        } finally {
            unlock();
        }
        return NOT_ALLOWED_SITIN;
    }

    @Override
    public void addSeatFromOtherServer(SEAT seat) {
        //need notify other seats
        ISitInResponse sitInResponse = getTOFactoryService().createSitInResponse(getCurrentTime(), getSeatNumber(seat), seat.getNickname(), seat.getJoinDate(),
                getAmmoAmount(seat), 0, seat.getAvatar(), null, null, false, 0,
                false, 0, getRoomInfo().getMoneyType().name(), 0);
        sendChanges(sitInResponse);
    }

    @Override
    public List<SEAT> getAllSeats() {
        List<SEAT> roomSeats = multiNodeSeatService.getRoomSeats(getId(), getSeatClass());
        //need restore transient seat.socketClient, todo: may be more intelligent way ?
        getLog().warn("getAllSeats: roomSeats.size()={}", roomSeats.size());

        for (SEAT roomSeat : roomSeats) {
            IGameSocketClient socketClient = observePlayers.get(roomSeat.getAccountId());
            if (socketClient != null) {
                roomSeat.setSocketClient(socketClient);
            }
        }
        return roomSeats;
    }

    @Override
    public SEAT getSeatByAccountId(long accountId) {
        @SuppressWarnings("unchecked")
        SEAT seat = (SEAT) multiNodeSeatService.getSeat(getId(), accountId);
        IGameSocketClient socketClient = observePlayers.get(accountId);
        if (seat != null && socketClient != null) {
            seat.setSocketClient(socketClient);
        }
        return seat;
    }

    /**
     * Send sitOut message of player to all clients on all servers.
     * @param seat seat of player
     * @param request seatOut message from client
     */
    @Override
    protected void sendSitOutMessage(SEAT seat, ISitOut request, int oldSeatNumber, long nextRoomId,
                                     boolean hasNextFrb, boolean frbSitOut) {
        ISitOutResponse allSeatsMessage = getTOFactoryService().createSitOutResponse(getCurrentTime(), TObject.SERVER_RID,
                oldSeatNumber, seat.getNickname(), getCurrentTime(), 0,
                0, 0, nextRoomId, hasNextFrb);
        Runnable sendSeatsMessageTask = createSendSeatsMessageTask(seat.getAccountId(), false, -1, allSeatsMessage, true);
        getLog().debug("sendSitOutMessage: sendSeatsMessageTask={}", sendSeatsMessageTask);
        executeOnAllMembers(sendSeatsMessageTask);
        ISitOutResponse seatMessage = getTOFactoryService().createSitOutResponse(getCurrentTime(),
                request != null ? request.getRid() : TObject.SERVER_RID,
                oldSeatNumber, seat.getNickname(), getCurrentTime(), 0,
                0, 0, nextRoomId, hasNextFrb);
        sendChanges(allSeatsMessage, seatMessage, seat.getAccountId(), request);
    }

    /**
     * This method called in the end of round for calculation and send result of round to gs side. (IAddWinResult)
     * Depending on the result of the processing, the player may be forcibly sit out of the room.
     */
    @Override
    public void convertBulletsToMoney() {
        lock();
        try {
            assertRoomStarted();
            long now = System.currentTimeMillis();
            Set<SEAT> seatsForProcess = new HashSet<>(getSeats());
            Set<SEAT> wantSitOutCandidates = new HashSet<>();
            boolean isAllSeatsWithoutShoot = isAllSeatsWithoutShoot(seatsForProcess);
            Set<IAddWinRequest> winRequests = new HashSet<>(seatsForProcess.size());
            for (SEAT seat : seatsForProcess) {
                IAddWinRequest winRequest = convertBulletsToMoneyForSeat(seat, wantSitOutCandidates, isAllSeatsWithoutShoot);
                if (winRequest != null) {
                    winRequests.add(winRequest);
                }
            }
            Map<Long, IAddWinResult> addWinResults = addBatchWin(winRequests);
            Set<Long> seatsWithWins = winRequests.stream().map(IAddWinRequest::getAccountId).collect(Collectors.toSet());
            for (SEAT seat : seatsForProcess) {
                if (seatsWithWins.contains(seat.getAccountId())) {
                    long accountId = seat.getAccountId();
                    IAddWinResult result = addWinResults.get(accountId);
                    final Money roundWin = seat.retrieveRoundWin();
                    final int ammoAmount = getAmmoAmount(seat);
                    final Money returnedBet = getReturnedBet(seat);
                    if (result != null) {
                        int seatNumber = getSeatNumber(seat);
                        IGameSocketClient socketClient = seat.getSocketClient();
                        String sessionId = socketClient != null ? socketClient.getSessionId() : seat.getPlayerInfo().getSessionId();
                        handleAddWinResult(result, seat, socketClient, accountId,
                                roundWin, ammoAmount, seatNumber, wantSitOutCandidates, sessionId,
                                returnedBet, null);
                    } else {
                        setPendingWinForPlayer(seat, roundWin, ammoAmount, returnedBet.toCents());
                    }
                }
            }
            for (SEAT sitOutCandidate : wantSitOutCandidates) {
                SEAT actualSeat = getSeatByAccountId(sitOutCandidate.getAccountId());
                if (actualSeat.isWantSitOut() || isSeatClientDisconnected(actualSeat)) {
                    getLog().info("convertBulletsToMoney: sitOut disconnected seat={}", actualSeat);
                    processSitOutSeat(sitOutCandidate)
                            .doOnSuccess(result -> getLog().debug("convertBulletsToMoney processSitOutSeat success, seat {}", actualSeat))
                            .doOnError(error -> getLog().error("convertBulletsToMoney processSitOutSeat: error, seat={}", actualSeat, error))
                            .subscribeOn(getScheduler())
                            .subscribe();
                } else {
                    getLog().debug("convertBulletsToMoney: skip sitOut actualSeat={}", actualSeat);
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics("GameRoom::convertBulletsToMoney",
                    System.currentTimeMillis() - now, "" + getRoomInfo().getId() + ":" + getRoomInfo().getRoundId());
            getLog().debug("convertBulletsToMoney: all seats process");
        } finally {
            unlock();
        }
    }

    Mono<Void> processSitOutSeat(SEAT seat) {
        return Mono.create(sink -> {
            try {
                processSitOut(seat.getSocketClient(), null, getSeatNumber(seat),
                        seat.getAccountId(), false, true);
                sink.success();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    protected IAddWinRequest convertBulletsToMoneyForSeat(SEAT seat, Set<SEAT> wantSitOutCandidates, boolean isAllSeatsWithoutShoot) {
        SEAT actualSeat = (SEAT) multiNodeSeatService.getSeat(seat.getRoomId(), seat.getAccountId());
        if (actualSeat.getSocketClient() == null) {
            actualSeat.setSocketClient(observePlayers.get(seat.getAccountId()));
        }
        return _convertBulletsToMoneyForSeat(actualSeat, wantSitOutCandidates);
        //saveSeat(0, actualSeat);
    }


    /**
     *
     * @param seat seat of player
     * @param wantSitOutCandidates set of players (candidates for sitOut)
     * @return {@code IAddWinRequest}  request data to gs side. (bet, win, returnedBet, history data, ...)
     */
    protected IAddWinRequest _convertBulletsToMoneyForSeat(SEAT seat, Set<SEAT> wantSitOutCandidates) {
        IGameSocketClient socketClient = seat.getSocketClient();
        IAddWinRequest addWinRequest = null;
        long accountId = seat.getAccountId();
        getLog().debug("convertBulletsToMoneyForSeat: seat.getPlayerInfo(): {}", seat.getPlayerInfo());
        try {
            final IRoomPlayerInfo playerInfoFromService = playerInfoService.get(accountId);
            //may be already sitOut
            if (playerInfoFromService == null) {
                getLog().debug("convertBulletsToMoneyForSeat: after lock player not found in playerInfoService, " +
                        "accountId={}", accountId);
                return null;
            } else if (playerInfoFromService.getRoomId() != getRoomInfo().getId()) {
                getLog().debug("convertBulletsToMoneyForSeat: after lock player found roomPlayerInfo for other room, " +
                        "playerInfoFromService={}", playerInfoFromService);
                return null;
            }
            IPendingOperation pendingOperation = pendingOperationService.get(seat.getAccountId());
            if (pendingOperation != null && pendingOperation.getOperationType() == PendingOperationType.ADD_WIN) {
                getLog().debug("convertBulletsToMoneyForSeat: player {} has pendingOperation already", seat.getAccountId());
                return null;
            }
            final IRoomPlayerInfo roomPlayerInfo = seat.getPlayerInfo();
            String sessionId = socketClient != null ? socketClient.getSessionId() : roomPlayerInfo.getSessionId();
            long gameSessionId = roomPlayerInfo.getGameSessionId();
            getLog().debug("convertBulletsToMoneyForSeat: seat={}", seat);

            IPlayerRoundInfo playerRoundInfo = seat.getCurrentPlayerRoundInfo();
            IPlayerBet newPlayerBet = roomPlayerInfo.createNewPlayerBet();

            IPlayerBet playerBet = playerRoundInfo.getPlayerBet(newPlayerBet, -1);
            playerBet.setStartRoundTime(getGameState().getStartRoundTime());

            boolean noActivity = isNoActivityInRound(seat, playerBet);
            if (noActivity) {
                getLog().debug("convertBulletsToMoneyForSeat: has no activity in round");
                playerBet.setData("");
            }
            final Money roundWin = seat.retrieveRoundWin();
            final Money returnedBet = getReturnedBet(seat);
            IBattlegroundRoundInfo bgRoundInfo = null;
            if (roomInfo.isBattlegroundMode()) {
                IBattlegroundRoomPlayerInfo castedPlayerInfo = (IBattlegroundRoomPlayerInfo) roomPlayerInfo;
                bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();
            }
            addWinRequest = getTOFactoryService().createAddWinRequest(sessionId, gameSessionId, roundWin.toCents(), returnedBet.toCents(),
                    accountId, playerBet, bgRoundInfo, roomPlayerInfo.getExternalRoundId(), false);
        } catch (Exception e) {
            getLog().error("convertBulletsToMoneyForSeat failed for accountId={}", accountId, e);
        }
        addToWantSitOutCandidatesIfNeed(wantSitOutCandidates, seat);
        return addWinRequest;
    }

    /**
     * Method will call gs side and get new roundId's for each seater from list of players.
     * @param seats list of seats
     */
    @Override
    public void sendStartNewRoundToAllPlayers(List<ISeat> seats) {
        try {
            List<ISeat> noPendingSeats = seats.stream().filter((seat) -> {
                IRoomPlayerInfo playerInfo = playerInfoService.get(seat.getAccountId());
                return !playerInfo.isPendingOperation();
            }).collect(Collectors.toList());

            List<IStartNewRoundResult> iStartNewRoundResults = socketService.startNewRoundForManyPlayers(noPendingSeats, roomInfo.getId(),
                    roomInfo.getRoundId(), roomInfo.getLastRoundStartDate(), false, roomInfo.getStake().toCents());
            for (IStartNewRoundResult newRoundResult : iStartNewRoundResults) {
                getLog().debug("sendStartNewRoundToAllPlayers: newRoundResult={}", newRoundResult);
                long newRoundId = newRoundResult.getPlayerRoundId();
                SEAT seat = getSeatByAccountId(newRoundResult.getAccountId());
                if (newRoundId > 0 && newRoundResult.isSuccess()) {
                    if (seat == null) {
                        getLog().error("sendStartNewRoundToAllPlayers: seat is null for accountId={}", newRoundResult.getAccountId());
                        continue;
                    }
                    IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                    if (playerInfo == null) { //impossible, remove after check
                        getLog().error("sendStartNewRoundToAllPlayers: playerInfo is null for seat={}", seat);
                        continue;
                    }
                    playerInfo.setExternalRoundId(newRoundId);
                    seat.updatePlayerRoundInfo(newRoundId);
                    playerInfoService.put(playerInfo);
                }
                getLog().debug("sendStartNewRoundToAllPlayers success: {}", seat.getAccountId());
            }
        } catch (Exception e) {
            getLog().error("sendStartNewRoundToAllPlayers: error, seat={}", seats, e);
        }
    }


    /**
     * Save seater to hazelcast map. Should be called after any critical changes fields of seater.
     * @param number seat number. For multi node rooms is not used.
     * @param seat seat of player
     */
    @Override
    public void saveSeat(int number, SEAT seat) {
        multiNodeSeatService.put(seat);
    }

    /**
     * Tries to acquire the lock  for player. Lock is needed for critical changes.
     * @param accountId accountId of player
     */
    @Override
    public void lockSeat(long accountId) {
        playerInfoService.lock(accountId);
    }

    /**
     * Tries to acquire the lock for player. Lock is needed for critical changes.
     * @param accountId accountId of player
     * @param time time
     * @param timeunit TimeUnit
     * @return true if the lock was acquired, false if the waiting time elapsed before the lock was acquired
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean tryLockSeat(long accountId, long time, TimeUnit timeunit) throws InterruptedException {
        return playerInfoService.tryLock(accountId, time, timeunit);
    }

    /**
     * Releases the lock for accountId. It never blocks and returns immediately.
     * @param accountId accountId of player
     */
    @Override
    public void unlockSeat(long accountId) {
        playerInfoService.unlock(accountId);
    }

    /**
     * Remove seater to hazelcast map.
     * @param number seat number. For multi node rooms is not used.
     * @param seat seat of player
     */
    @Override
    public void removeSeat(int number, SEAT seat) {
        multiNodeSeatService.remove(seat);
    }

    @Override
    public int getSeatNumber(SEAT seat) {
        //this game without numbered seats
        return 0;
    }

    @Override
    public void setSeatNumber(SEAT seat, int number) {
        //nop, this game without numbered seats
    }

    @Override
    protected void convertBulletsToMoneyForSeat(SEAT seat, CountDownLatch asyncCallLatch, Set<SEAT> wantSitOutCandidates,
                                                boolean isAllSeatsWithoutShoot) {
        @SuppressWarnings("unchecked")
        SEAT actualSeat = (SEAT) multiNodeSeatService.getSeat(seat.getRoomId(), seat.getAccountId());
        if (actualSeat.getSocketClient() == null) {
            actualSeat.setSocketClient(observePlayers.get(seat.getAccountId()));
        }
        super.convertBulletsToMoneyForSeat(actualSeat, asyncCallLatch, wantSitOutCandidates, isAllSeatsWithoutShoot);
        saveSeat(0, actualSeat);
    }

    @Override
    protected boolean isSeatClientDisconnected(SEAT seat) {
        return seat.getActiveServerId() == serverConfigService.getServerId() && super.isSeatClientDisconnected(seat);
    }

    @Override
    protected void addToWantSitOutCandidatesIfNeed(Set<SEAT> wantSitOutCandidates, SEAT seat) {
        SEAT actualSeat = getSeatByAccountId(seat.getAccountId());
        if (actualSeat.isWantSitOut() || isSeatClientDisconnected(actualSeat)) {
            IGameSocketClient gameSocketClient = actualSeat.getSocketClient();
            getLog().debug("convertBulletsToMoneyForSeat: add to wantSitOutCandidates seat.isWantSitOut()={}, seat.activeServerId={}, " +
                            "gameSocketClient={}, super.isSeatClientDisconnected={}", actualSeat.isWantSitOut(), actualSeat.getActiveServerId(),
                    gameSocketClient == null ? null : gameSocketClient.isDisconnected(), super.isSeatClientDisconnected(actualSeat));
            wantSitOutCandidates.add(actualSeat);
        }
    }

    @Override
    public ITransportObject getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client) {
        SEAT alreadySeat = null;
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                if (seat.getAccountId() == client.getAccountId()) {
                    alreadySeat = seat;
                }
            }
        }
        return getTOFactoryService().createFullGameInfo(getCurrentTime(), request.getRid(), gameState.getCurrentMapId(),
                gameState.getSubround().name(), gameState.getStartTime(), gameState.getRoomState(),
                null, getTransportSeats(), null, null, false, getRoomInfo().getRoundId(), null,
                alreadySeat == null ? getDefaultBetLevel() : alreadySeat.getBetLevel(), null, null, 0L, null, 0, null
        );
    }

    /**
     * Prepares and sends detailed information about the room to the client.
     * @param requestId requestId from client
     * @param client game socket client of player
     * @param playerCurrency player currency
     * @return {@code IGetRoomInfoResponse} detail room info
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public IGetRoomInfoResponse getRoomInfoResponse(int requestId, IGameSocketClient client, String playerCurrency)
            throws CommonException {
        checkAndStartRoom();
        long accountId = client.getAccountId();
        SEAT alreadySeat = null;
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                if (seat.getAccountId() == client.getAccountId()) {
                    alreadySeat = seat;
                }
            }
        }
        float minBuyIn = getRoomInfo().getMinBuyIn();
        double stake = getRoomInfo().getStake().toCents();
        IRoomPlayerInfo playerInfo = alreadySeat == null ? null : alreadySeat.getPlayerInfo();
        long activeFrbWin = 0;
        IActiveCashBonusSession activeCashBonusSession = null;
        ITournamentSession activeTournamentSession = null;
        boolean isFRBSession = getRoomInfo().getMoneyType() == MoneyType.FRB;
        if (isFRBSession) {
            if (playerInfo != null) {
                IActiveFrbSession activeFrbSession = playerInfo.getActiveFrbSession();
                if (activeFrbSession != null) {
                    activeFrbWin = activeFrbSession.getWinSum();
                }
            } else {
                ILobbySession lobbySession = lobbySessionService == null ? null :
                        lobbySessionService.get(client.getSessionId());
                if (lobbySession != null) {
                    activeFrbWin = lobbySession.getActiveFrbSession().getWinSum();
                } else {
                    List<IActiveFrbSession> frbSessions = activeFrbSessionService.getByAccountId(accountId);
                    if (frbSessions.size() > 0) {
                        if (frbSessions.size() > 1) {
                            getLog().error("Found many frbSessions, this error, please fix. frbSessions={}", frbSessions);
                        } else {
                            IActiveFrbSession activeFrbSession = frbSessions.get(0);
                            activeFrbWin = activeFrbSession.getWinSum();
                        }
                    }
                }
            }
        } else if (getRoomInfo().getMoneyType() == MoneyType.CASHBONUS) {
            if (playerInfo != null) {
                activeCashBonusSession = playerInfo.getActiveCashBonusSession();
            } else {
                ILobbySession lobbySession = lobbySessionService == null ? null :
                        lobbySessionService.get(client.getSessionId());
                activeCashBonusSession = lobbySession == null ? null : lobbySession.getActiveCashBonusSession();
                if (activeCashBonusSession == null) {
                    getLog().error("getRoomInfoResponse: activeCashBonusSession is null, lobbySession={}", lobbySession);
                }
            }
        } else if (getRoomInfo().getMoneyType() == MoneyType.TOURNAMENT) {
            if (playerInfo != null) {
                activeTournamentSession = playerInfo.getTournamentSession();
            } else {
                ILobbySession lobbySession = lobbySessionService == null ? null :
                        lobbySessionService.get(client.getSessionId());
                activeTournamentSession = lobbySession == null ? null : lobbySession.getTournamentSession();
                if (activeTournamentSession == null) {
                    getLog().error("getRoomInfoResponse: activeTournamentSession is null, lobbySession={}",
                            lobbySession);
                }
            }
        }
        getLog().debug("getRoomInfoResponse: room.stake={}, player currency={}, seatNumber={}",
                getRoomInfo().getStake().toCents(), playerCurrency,
                (alreadySeat == null ? -1 : getSeatNumber(alreadySeat)));
        long alreadySitInWin = alreadySeat == null ? 0 : (long) alreadySeat.getRoundWin().toDoubleCents();
        if (isFRBSession) {
            getLog().debug("activeFrbWin: {}, alreadySitInWin: {}", activeFrbWin, alreadySitInWin);
            alreadySitInWin += activeFrbWin;
        }
        int betLevel = 1;
        if (alreadySeat != null) {
            betLevel = alreadySeat.getBetLevel();
        }
        return getTOFactoryService().createGetRoomInfoResponse(getCurrentTime(), getId(), requestId,
                getName(), getMaxSeats(), minBuyIn, stake, stake,
                getState(), getTransportSeats(), getTimeToNextState(), roomInfo.getWidth(), roomInfo.getHeight(),
                getTransportEnemies(), getLiveRoomEnemies(),
                alreadySeat == null ? -1 : getSeatNumber(alreadySeat),
                alreadySeat == null ? 0 : getAmmoAmount(alreadySeat),
                alreadySeat == null ? 0 : getBalance(alreadySeat),
                alreadySitInWin,
                gameState.getCurrentMapId(),
                gameState.getSubround().name(),
                GameType.getAmmoValues(getRoomInfo().getMoneyType(), getRoomInfo().getStake().toFloatCents()),
                null, gameState.getFreezeTimeRemaining(), false, getRoomInfo().getRoundId(),
                null, activeCashBonusSession, activeTournamentSession,
                betLevel,
                null, Collections.emptySet(), null, getReels(), null);
    }


    /**
     * Tries to make sit out player from room.
     * @param seat seat of player
     * @param ammoAmount amount of ammo for player. For multi node rooms is not used.
     * @param activeCashBonusSession cash bonus session
     * @param bulletsConvertedToMoney
     * @param socketClient game socket client
     * @param client  game socket client
     * @param request request ISitOut from client
     * @param serverId serverId
     * @param seatNumber number of seat (not used)
     * @param oldSeatNumber  number of seat (not used)
     * @param tournamentSession tournament session
     * @param accountId accountId of player
     * @throws CommonException if any unexpected error occur
     */
    @Override
    protected void processSitOutForNonFrbMode(SEAT seat, int ammoAmount, IActiveCashBonusSession activeCashBonusSession,
                                              boolean bulletsConvertedToMoney,
                                              IGameSocketClient socketClient, IGameSocketClient client,
                                              ISitOut request, int serverId, int seatNumber, int oldSeatNumber,
                                              ITournamentSession tournamentSession, long accountId) throws CommonException {

        int seatAmmoAmount = getAmmoAmount(seat);
        final Money roundWin = seat.retrieveRoundWin();
        final Money returnedBet = getReturnedBet(seat);
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        playerInfo.setPendingOperation(true, "sitOut, roundWin=" + roundWin.toCents() + ", ammoAmount=" + ammoAmount + ", returnedBet=" + returnedBet);

        playerInfoService.put(playerInfo);
        Money correctedRoundWin = roundWin;
        Money correctedReturnedBet = returnedBet;
        if (seat.getRebuyFromWin().toCents() > 0) {
            if (seat.getRebuyFromWin().lessOrEqualsTo(returnedBet)) {
                correctedRoundWin = roundWin.add(seat.getRebuyFromWin());
                correctedReturnedBet = returnedBet.subtract(seat.getRebuyFromWin());
            } else {
                correctedRoundWin = roundWin.add(returnedBet);
                correctedReturnedBet = Money.ZERO;
            }
            getLog().debug("processSitOutForNonFrbMode: Found rebuyFromWin={}, need correcting amounts. roundWin={}, " +
                            "returnedBet={}, correctedRoundWin={}, correctedReturnedBet={}",
                    seat.getRebuyFromWin().toCents(), roundWin.toCents(),
                    returnedBet.toCents(), correctedRoundWin.toCents(), correctedReturnedBet.toCents());
        }

        IPlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
        updateStatOnEndRound(seat, roundInfo);
        IPlayerBet newPlayerBet = playerInfo.createNewPlayerBet();
        final IPlayerBet playerBet = roundInfo.getPlayerBet(newPlayerBet, seatAmmoAmount);

        try {
            IPlayerProfile playerProfile = playerProfileService.load(playerInfo.getBankId(), seat.getAccountId());
            if (activeCashBonusSession != null) {
                processSitOutCashBonusSession(activeCashBonusSession, bulletsConvertedToMoney,
                        correctedRoundWin, correctedReturnedBet, seat, playerProfile, playerBet, playerInfo,
                        socketClient, client, request, roundWin, ammoAmount, serverId, seatNumber, oldSeatNumber);
            } else if (tournamentSession != null) {
                processSitOutTournamentSession(tournamentSession, bulletsConvertedToMoney, correctedRoundWin,
                        correctedReturnedBet, seat, playerProfile, playerBet, playerInfo, socketClient,
                        client, request, roundWin, ammoAmount, serverId, seatNumber, oldSeatNumber);
            } else {
                try {
                    long gameId = roomInfo.getGameType().getGameId();
                    Set<IQuest> allQuests = playerQuestsService.getAllQuests(seat.getBankId(),
                            seat.getAccountId(), MoneyType.REAL.ordinal(), gameId);
                    Map<Long, Map<Integer, Integer>> allWeapons = weaponService.getAllWeaponsLong(
                            seat.getBankId(), seat.getAccountId(), MoneyType.REAL.ordinal(), gameId);

                    socketService.sendMQDataSync(serverId, seat, null, playerProfile,
                            gameId, allQuests, allWeapons);
                } catch (Exception e) {
                    getLog().error("processSitOutForNonFrbMode: sendMQDataSync error, profile={}", playerProfile, e);
                }

                IBattlegroundRoundInfo bgRoundInfo = null;

                if (playerInfo instanceof IBattlegroundRoomPlayerInfo) {
                    IBattlegroundRoomPlayerInfo castedPlayerInfo = (IBattlegroundRoomPlayerInfo) playerInfo;
                    bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();

                    long battleBet = 0;
                    long winAmount = 0;

                    if (bgRoundInfo == null) {
                        castedPlayerInfo.createBattlegroundRoundInfo(roomInfo.getStake().toCents(), 0,
                                0, 0, null, 0,
                                null, seat.getAccountId(), 1, playerInfo.getGameSessionId(), seat.getTotalScore().getLongAmount(),
                                roomInfo.getRoundId(), roomInfo.getLastRoundStartDate(), 0.0, roomInfo.getPrivateRoomId());
                        bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();
                        getLog().debug("processSitOutForNonFrbMode: BattlegroundRoundInfo was null, created new instance RoundInfo={}", bgRoundInfo);
                    } else {
                        getLog().debug("processSitOutForNonFrbMode: BattlegroundRoundInfo is bgRoundInfo={}", bgRoundInfo);
                        battleBet = bgRoundInfo.getBuyIn();
                        winAmount = bgRoundInfo.getWinAmount();
                    }

                    roundInfo.setBattleBet(battleBet);
                    roundInfo.setBattleWin(winAmount);

                    playerBet.setBet(battleBet);
                    playerBet.setWin(winAmount);
                }

                IPendingOperation pendingOperation = pendingOperationService.get(accountId);
                if (pendingOperation == null) {
                    if (roundWin.toCents() > 0 || returnedBet.toCents() > 0) {
                        IAddWinResult result =  socketService.addWinWithSitOutSync(serverId, playerInfo.getSessionId(), playerInfo.getGameSessionId(),
                                roundWin, returnedBet, playerInfo.getExternalRoundId(), getId(), accountId, playerBet,
                                bgRoundInfo, true);
                        if (!result.isSuccess()) {
                            setPendingWinForPlayer(seat, roundWin, ammoAmount, returnedBet.toCents());
                        }
                    } else {
                        socketService.closeGameSession(serverId, playerInfo.getSessionId(), accountId, playerInfo.getGameSessionId(),
                                getId(), getGameType().getGameId(), playerInfo.getBankId(), roomInfo.getStake().toCents());
                    }
                }else {
                    getLog().warn("processSitOutForNonFrbMode: skip call closeGameSession, found pendingOperation={}", pendingOperation);
                }

                checkResultAndFinishSitOut(null, seat, playerInfo, client, request, roundWin, ammoAmount,
                        socketClient, null, serverId, seatNumber, oldSeatNumber);
            }
        } catch (Exception e) {
            handleSitOutError(e, seat, roundWin, ammoAmount, playerInfo, request, seatNumber, oldSeatNumber, client);
        }
    }


    /**
     * Adds batch request for sending of win results of players to gs side for next processing.
     * @param winRequests Set of results of round for all players
     * @return {@code Map<Long, IAddWinResult>} result of processing from gs side.
     */
    protected Map<Long, IAddWinResult> addBatchWin(Set<IAddWinRequest> winRequests) {
        IRoomInfo roomInfo = getRoomInfo();
        return socketService.addBatchWin(roomInfo.getId(), roomInfo.getRoundId(), getGameType().getGameId(), winRequests, roomInfo.getBankId(),
                TimeUnit.SECONDS.toMillis(3));
    }

    /**
     * Locks room by shared game state
     */
    @Override
    public void lock() {
        getLog().debug("lock start");
        getSharedGameStateService().lock(getId());
        getLog().debug("lock end");
    }

    /**
     * Unlock room by shared game state
     */
    @Override
    public void unlock() {
        getLog().debug("unlock");
        getSharedGameStateService().unlock(getId());
    }

    @Override
    public boolean isLocked() {
        return getSharedGameStateService().isLocked(getId());
    }

    /**
     * Tries to acquire the lock for SharedGameState.
     * @param timeout time
     * @param timeUnit timeUnit
     * @return true if the lock was acquired, false if the waiting time elapsed before the lock was acquired
     * @throws InterruptedException if any unexpected error occur
     */
    @Override
    public boolean tryLock(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return getSharedGameStateService().tryLock(getId(), timeout, timeUnit);
    }

    @Override
    public void removeSeatsWithPendingOperations() {
        // no need now
    }

    @Override
    public boolean hasNotReadyNotKickedSeat() {

        List<Long> readySeatsAccountId = getRealSeatsAccountId();

        return getSeats().stream()
                .anyMatch(seat ->
                        seat != null && seat.getSocketClient() != null
                                && !seat.getSocketClient().isKicked()
                                && !readySeatsAccountId.contains(seat.getAccountId())
                );

    }

    public int getTotalObserversCountFromState() {
        SharedCrashGameState sharedCrashGameState = getSharedGameStateService().get(roomInfo.getId(), SharedCrashGameState.class);
        return sharedCrashGameState.getTotalObservers();
    }
}
