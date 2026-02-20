package com.betsoft.casino.mp.maxcrashgame.model;

import com.betsoft.casino.mp.common.AbstractMultiNodeGameRoom;
import com.betsoft.casino.mp.common.AbstractPlayGameState;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyRange;
import com.betsoft.casino.mp.maxcrashgame.model.math.EnemyType;
import com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfig;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.RNG;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.betsoft.casino.mp.utils.ErrorCodes.TOO_MANY_PLAYER;

//cannot make this class abstract for prevent Kryo serialization errors
@SuppressWarnings({"rawtypes", "unchecked"})
public class AbstractCrashGameRoom extends AbstractMultiNodeGameRoom<EnemyGame, GameMap, Seat, GameRoomSnapshot, Enemy, EnemyType, IMultiNodeRoomInfo,
        ICrashGameRoomPlayerInfo> {
    public static final double DEFAULT_MIN_MULTIPLIER = 1.01;
    public static final double DEFAULT_MAX_MULTIPLIER = 10000000.00;
    private final List<ITransportEnemy> possibleEnemies;
    private static final long NO_ACTIVITY_TIME = TimeUnit.SECONDS.toMillis(600);
    private final transient ScriptEngine scriptEngine;
    private final transient ICrashGameSettingsService settingsService;
    private static final Scheduler crashScheduler = Schedulers.newParallel("CRASH_SCHEDULER", 200);

    @SuppressWarnings("rawtypes")
    public AbstractCrashGameRoom(ApplicationContext context, Logger logger, IMultiNodeRoomInfo roomInfo, GameMap map,
                                 IPlayerStatsService playerStatsService, IWeaponService weaponService,
                                 IExecutorService remoteExecutorService, IPlayerQuestsService playerQuestsService,
                                 IPlayerProfileService playerProfileService,
                                 IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                                 IActiveCashBonusSessionService activeCashBonusSessionService,
                                 ITournamentService tournamentService,
                                 IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(context, logger, roomInfo,
                new EnemyGame(logger, gameConfigService, gameConfigProvider, spawnConfigProvider), map,
                playerStatsService, playerQuestsService, weaponService,
                remoteExecutorService, playerProfileService, gameConfigService, activeFrbSessionService,
                activeCashBonusSessionService, tournamentService);
        possibleEnemies = convertEnemies(EnemyType.values());
        scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        settingsService = (ICrashGameSettingsService) context.getBean("crashGameSettingsService");
    }

    @SuppressWarnings("rawtypes")
    public AbstractCrashGameRoom(ApplicationContext context, Logger logger, IMultiNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService,
                                 IWeaponService weaponService, IExecutorService remoteExecutorService, GameRoomSnapshot snapshot,
                                 IPlayerQuestsService playerQuestsService,
                                 IPlayerProfileService playerProfileService, IGameConfigService gameConfigService,
                                 IActiveFrbSessionService activeFrbSessionService,
                                 IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService,
                                 IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(context, logger, roomInfo, new EnemyGame(logger, gameConfigService, gameConfigProvider, spawnConfigProvider), snapshot.getMap(),
                playerStatsService, playerQuestsService, weaponService, remoteExecutorService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService,
                tournamentService);
        this.nextMapId = snapshot.getNextMapId();
        this.gameState = restoreGameState();
        possibleEnemies = convertEnemies(EnemyType.values());
        scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        settingsService = (ICrashGameSettingsService) context.getBean("crashGameSettingsService");
    }

    private IGameState restoreGameState() {
        ISharedGameStateService sharedGameStateService = getSharedGameStateService();
        SharedCrashGameState crashGameState = sharedGameStateService != null ? sharedGameStateService.get(getId(), SharedCrashGameState.class) : null;
        if (crashGameState == null) {
            getLog().warn("restoreGameState: SharedCrashGameState not found, set WaitingPlayersGameState");
            return new WaitingPlayersGameState(this);
        }
        RoomState state = crashGameState.getState();
        if (state == RoomState.PLAY) {
            return new PlayGameState(this);
        } else if (state == RoomState.QUALIFY) {
            return new QualifyGameState(this, getMapId(), 3000, crashGameState.getRoundStartTime(), crashGameState.getRoundEndTime());
        } else {
            return new WaitingPlayersGameState(this);
        }
    }

    public Scheduler getCrashScheduler() {
        return crashScheduler;
    }

    @Override
    public boolean shutdownRoomIfEmpty() {
        return false;
    }

    public boolean isValidMultiplier(double multiplier) {
        return multiplier >= DEFAULT_MIN_MULTIPLIER && multiplier <= DEFAULT_MAX_MULTIPLIER;
    }

    @Override
    protected IRoundResult createRoundResult(double winAmount, double realWinAmount, long balance, int level, int prevLevel, long totalKillsXP,
                                             Seat seat, List<ITransportSeat> transportSeats, IPlayerRoundInfo roundInfo) {
        IRoundResult result = super.createRoundResult(winAmount, realWinAmount, balance, level, prevLevel, totalKillsXP, seat, transportSeats,
                roundInfo);
        SharedCrashGameState crashGameState = getSharedGameStateService().get(getId(), SharedCrashGameState.class);
        if (crashGameState != null && crashGameState.getMaxCrashData() != null) {
            result.setCrashMultiplier(crashGameState.getMaxCrashData().getCrashMult());
        }
        return result;
    }

    @Override
    public void convertBulletsToMoney() {
        lock();
        try {
            super.convertBulletsToMoney();
        } finally {
            unlock();
        }
    }

    @Override
    protected WaitingPlayersGameState getWaitingPlayersGameState() {
        return new WaitingPlayersGameState(this);
    }

    void setPossibleEnemies(EnemyRange possibleEnemies) {
        getMap().setPossibleEnemies(possibleEnemies);
    }

    @Override
    public GameRoomSnapshot getSnapshot() {
        return null;
    }

    @Override
    public GameRoomSnapshot shutdown() throws CommonException {
        getLog().debug("Shutdown started");
        lock();
        try {
            this.tableStart.set(false);
            stopTimer();
            eventManager.shutdown();
        } finally {
            unlock();
        }
        getLog().debug("Shutdown finished");
        return null;
    }

    @Override
    public Seat createSeat(ICrashGameRoomPlayerInfo playerInfo, IGameSocketClient socketClient, double currentRate) {
        Seat seat = new Seat(playerInfo, socketClient, currentRate, getGameType().getGameId());
        seat.getCurrentPlayerRoundInfo().setRoomRoundId(getRoomInfo().getRoundId());
        return seat;
    }

    public ICrashGameSetting getCrashGameSetting() {
        IMultiNodeRoomInfo info = getRoomInfo();
        return settingsService.getSettings(info.getBankId(), getGameType().getGameId(), info.getCurrency());
    }

    ICurrencyRateService getCurrencyRateService() {
        return currencyRateService;
    }

    @Override
    public int getAllowedPlayers() {
        return getCrashGameSetting().getMaxRoomPlayers();
    }

    @Override
    public int processSitIn(Seat seat, ISitIn request) throws CommonException {
        if (isRoomFull()) {
            return TOO_MANY_PLAYER;
        }
        return super.processSitIn(seat, request);
    }

    @Override
    public boolean isRoomFull() {
        return getSeatsCount() >= getAllowedPlayers();
    }

    @Override
    public Logger getLog() {
        return logger;
    }

    @Override
    protected List<ITransportEnemy> getTransportEnemies() {
        return possibleEnemies;
    }

    @Override
    public void sendNewEnemyMessage(Enemy enemy) {
        sendChanges(getTOFactoryService().createNewEnemy(getCurrentTime(), convert(enemy, true)));
    }

    @Override
    public GameType getGameType() {
        //must be overriden
        throw new RuntimeException("Undefined gameType");
    }

    @Override
    public IRoomEnemy convert(Enemy enemy, boolean fillTrajectory) {
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        Trajectory trajectory = EnemyType.ROCKET.equals(enemyType)
                ? convertFullTrajectory(enemy.getTrajectory())
                : convertTrajectory(enemy.getTrajectory(), System.currentTimeMillis());

        return getTOFactoryService().createRoomEnemy(
                enemy.getId(),
                enemyType.getId(),
                enemyType.isBoss(),
                enemy.getSpeed(),
                enemy.getAwardedPrizesAsString(),
                enemy.getAwardedSum().toDoubleCents(),
                getHP(enemy),
                enemy.getSkin(),
                fillTrajectory ? trajectory : null,
                enemy.getParentEnemyId(),
                0,
                enemy.getMembers(),
                enemy.getSwarmId(),
                enemy.getSwarmType()
        );
    }

    private double getHP(Enemy enemy) {
        return enemy.getLives() + 1.;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractCrashGameRoom gameRoom = (AbstractCrashGameRoom) o;
        return Objects.equals(roomInfo, gameRoom.roomInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomInfo);
    }

    @Override
    protected void calculateWeaponsSurplusCompensation(Seat seat) {
        //nop
    }

    @Override
    protected List<Integer> getWeaponLootBoxPrices() {
        return Collections.emptyList();
    }

    @Override
    protected boolean isBossImmortal(IGameSocketClient client) {
        return false;
    }

    /**
     *
     * @param requestId requestId from client
     * @param client socket client of player
     * @param playerCurrency player currency
     * @return {@code IGetRoomInfoResponse} detail room info
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public IGetRoomInfoResponse getRoomInfoResponse(int requestId, IGameSocketClient client, String playerCurrency) throws CommonException {
        IGetRoomInfoResponse response = super.getRoomInfoResponse(requestId, client, playerCurrency);
        response.setEndTime(gameState.getEndRoundTime());
        IGameState gameState = getGameState();
        if (gameState instanceof AbstractPlayGameState) {
            response.setNeedWaitingWhenEnemiesLeave(((PlayGameState) gameState).isNeedWaitingWhenEnemiesLeave());
        }
        return response;
    }

    @Override
    protected List<ITransportSeat> getTransportSeats() {
        List<ITransportSeat> result = new ArrayList<>();
        for (Seat seat : getAllSeats()) {
            if (seat != null) {
                IActiveFrbSession frbSession = seat.getPlayerInfo().getActiveFrbSession();
                long roundWin = frbSession != null
                        ? frbSession.getWinSum() + seat.getRoundWin().toCents()
                        : seat.getRoundWin().add(seat.getRebuyFromWin()).toCents();
                result.add(getTOFactoryService().createSeat(getSeatNumber(seat), seat.getNickname(),
                        seat.getJoinDate(), seat.getTotalScore() == null ? 0 : seat.getTotalScore().getAmount(),
                        seat.getCurrentScore() == null ? 0 : seat.getCurrentScore().getAmount(),
                        seat.getAvatar(), 0, seat.getLevel(),
                        0, seat.getCurrentPlayerRoundInfo().getTotalDamage(),
                        seat.getBetLevel(), roundWin));
            }
        }
        return result;
    }

    @Override
    public ITransportObject getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client) {
        return createCrashGameInfo(request == null ? -1 : request.getRid(), client);
    }

    @Override
    protected int getMaxObservers() {
        return getAllowedPlayers();
    }

    @Override
    public boolean isRoomFullOrManyObservers() {
        short seatsCount = getSeatsCount();
        int allowedPlayers = getAllowedPlayers();
        if (seatsCount >= allowedPlayers) {
            return true;
        }
        return getObserverCount() >= allowedPlayers || getTotalObserversCountFromState() >= allowedPlayers;
    }

    @Override
    public void removeObserverByAccountId(long accountId) {

        super.removeObserverByAccountId(accountId);

        ISharedGameStateService sharedGameStateService = getSharedGameStateService();

        SharedCrashGameState sharedCrashGameState = sharedGameStateService
                .get(roomInfo.getId(), SharedCrashGameState.class);

        if (sharedCrashGameState != null) {
            sharedCrashGameState.updateNumberObservers(serverConfigService.getServerId(), getObserverCount());
            sharedGameStateService.put(sharedCrashGameState);
        }
    }

    @Override
    public ITransportObject processOpenRoom(IGameSocketClient client, IOpenRoom request, String currency) throws CommonException {
        getLog().debug("processOpenRoom: client={}, request={}", client, request);
        checkAndStartRoom();

        boolean locked;
        try {
            locked = tryLock(5, TimeUnit.SECONDS);
            if (!locked) {
                getLog().debug("processOpenRoom: Cannot obtain room lock for room: {}", getRoomInfo().getId());
                return getTOFactoryService().createError(ErrorCodes.CANNOT_OBTAIN_LOCK, "Cannot obtain room lock", getCurrentTime(), request.getRid());
            }
        } catch (InterruptedException e) {
            throw new CommonException("Cannot lock");
        }
        try {
            removeDisconnectedObservers();
            SharedCrashGameState sharedCrashGameState = getSharedGameStateService().get(roomInfo.getId(), SharedCrashGameState.class);
            int totalObserversCount = sharedCrashGameState.getTotalObservers();
            if (totalObserversCount >= getMaxObservers()) {
                getLog().debug("processOpenRoom: too many observers={}, maxObservers={}, totalObserversCount: {}",
                        observePlayers.size(), getMaxObservers(), totalObserversCount);
                return getTOFactoryService().createError(ErrorCodes.TOO_MANY_OBSERVERS, "Too many observers", getCurrentTime(), request.getRid());
            }

            observePlayers.put(client.getAccountId(), client);

            for (Seat seat : getAllSeats()) {
                if (seat != null && seat.getAccountId() == client.getAccountId()) {
                    seat.setSocketClient(client);
                }
            }
        } finally {
            unlock();
        }
        return createCrashGameInfo(request.getRid(), client);
    }

    public void removeDisconnectedObservers() {
        super.removeDisconnectedObservers();
        SharedCrashGameState sharedCrashGameState = getSharedGameStateService().get(roomInfo.getId(), SharedCrashGameState.class);
        sharedCrashGameState.updateNumberObservers(serverConfigService.getServerId(), getObserverCount());
        getSharedGameStateService().put(sharedCrashGameState);
    }

    @Override
    public void addSeatFromOtherServer(Seat seat) {
        IMultiNodeRoomInfo info = getRoomInfo();
        ISitInResponse sitInResponse = getTOFactoryService().createSitInResponse(getCurrentTime(), getSeatNumber(seat), seat.getNickname(), seat.getJoinDate(),
                0, 0, seat.getAvatar(), null, null, false, 0,
                false, 0, info.getMoneyType().name(), 0);
        ICrashGameSetting settings = settingsService.getSettings(info.getBankId(), getGameType().getGameId(), info.getCurrency());
        if (settings != null) {
            sitInResponse.setMaxMultiplier(settings.getMaxMultiplier());
            sitInResponse.setMaxPlayerProfitInRound(settings.getMaxPlayerProfitInRound());
            sitInResponse.setTotalPlayersProfitInRound(settings.getTotalPlayersProfitInRound());
        }
        sendChanges(sitInResponse);
    }

    private ICrashGameInfo createCrashGameInfo(int rid, IGameSocketClient client) {
        GameConfig config = getGame().getConfig(getId());
        IMultiNodeRoomInfo nodeRoomInfo = getRoomInfo();
        ICrashGameInfo info = getTOFactoryService().createCrashGameInfo(System.currentTimeMillis(), rid, getId(), getMapId(),
                gameState.getStartRoundTime(), gameState.getRoomState(), getTransportSeats(), nodeRoomInfo.getRoundId(),
                getMap().getMultHistory(), nextMapId, gameState.getTimeToNextState(), config.getFunction(), false, 0, null);
        ICrashGameSetting settings = settingsService.getSettings(nodeRoomInfo.getBankId(), getGameType().getGameId(), nodeRoomInfo.getCurrency());
        if (settings != null) {
            info.setMaxMultiplier(settings.getMaxMultiplier());
            info.setMaxPlayerProfitInRound(settings.getMaxPlayerProfitInRound());
            info.setTotalPlayersProfitInRound(settings.getTotalPlayersProfitInRound());
        }
        for (Seat seat : getSeats()) {
            if (seat.getAccountId() == client.getAccountId()) {
                info.setCanceledBetAmount(seat.getCanceledBetAmount());
                IRoomPlayerInfo playerInfo = playerInfoService.get(client.getAccountId());
                if (playerInfo != null) {
                    info.setPending(playerInfo.isPendingOperation());
                }
            }
            for (Map.Entry<String, ICrashBetInfo> betInfo : seat.getCrashBets().entrySet()) {
                ICrashBetInfo crashBetInfo = betInfo.getValue();
                info.addBet(betInfo.getKey(), seat.getNickname(), crashBetInfo.getCrashBetAmount(),
                        crashBetInfo.isAutoPlay(), crashBetInfo.getMultiplier(), crashBetInfo.getEjectTime(),
                        crashBetInfo.getAutoPlayMultiplier(), crashBetInfo.isReserved());
            }
        }
        if (RoomState.PLAY.equals(gameState.getRoomState())) {
            SharedCrashGameState crashGameState = getSharedGameStateService().get(getId(), SharedCrashGameState.class);
            if (crashGameState != null && crashGameState.getMaxCrashData() != null) {
                info.setCurrentMult(crashGameState.getMaxCrashData().getCurrentMult());
                info.setCrash(crashGameState.getMaxCrashData().getCurrentMult() >= crashGameState.getMaxCrashData().getCrashMult());
            }
        }
        return info;
    }

    @Override
    protected Money getReturnedBet(Seat seat) {
        return Money.fromCents(seat.retrieveCanceledBetAmount());
    }

    @Override
    protected void rollbackSeatWin(Seat seat, Money roundWin, int ammoAmount, Money returnedBet) {
        super.rollbackSeatWin(seat, roundWin, ammoAmount, Money.ZERO);
        seat.setCanceledBetAmount(returnedBet.toCents());
    }

    @Override
    public void toggleMap() {
        map.getMapShape().setId(nextMapId);
        generateNextMapId();
    }

    @Override
    protected void generateNextMapId() {
        nextMapId = RNG.nextInt(100000);
    }

    @Override
    public Class<Seat> getSeatClass() {
        return Seat.class;
    }

    @Override
    protected boolean isSitOutNotAllowed(Seat seat) {
        return seat.getCrashBetsCount() > 0;
    }

    @Override
    public short getRealSeatsCount() {
        short count = 0;
        for (Seat seat : getAllSeats()) {
            if (seat == null) {
                continue;
            }
            if (seat.getCrashBetsCount() > 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void makeBuyInForCashBonus(Seat seat) {
        //nop
    }

    @Override
    public void makeBuyInForTournament(Seat seat) {
        //nop
    }

    @Override
    protected boolean isNoActivityInRound(Seat seat, IPlayerBet playerBet) {
        return seat.getCrashBetsCount() == 0 && seat.isDisconnected() && isNoActivityLongTime(seat);
    }

    private boolean isNoActivityLongTime(Seat seat) {
        return System.currentTimeMillis() - seat.getLastActivityDate() > NO_ACTIVITY_TIME;
    }

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    @Override
    public boolean isNotAllowPlayWithAnyPendingPlayers() {
        return false;
    }

    @Override
    protected Long getRoundStartTime(IGameState gameState) {
        if (gameState.getRoomState() == RoomState.PLAY) {
            SharedCrashGameState crashGameState = getSharedGameStateService().get(getId(), SharedCrashGameState.class);
            lastRoundStartTime = crashGameState.getRoundStartTime();
            return lastRoundStartTime;
        }

        lastRoundStartTime= null;
        return null;
    }

    @Override
    public void startUpdateTimer() {
        if (updateTimer != null) {
            getLog().debug("startUpdateTimer: need stop stopUpdateTimer");
            stopUpdateTimer();
        }
        checkAndStartTimer();
    }

    public int allowedBetsCount() {
        return 3;
    }

    @Override
    public void sendStartNewRoundToAllPlayers(List<ISeat> seats) {
        //nop, not required call to GS. playerRoundId already set on buyIn
    }

    Mono<IBuyInResult> processReservedBetsForSeat(Seat seat) {
        return Mono.create(sink -> {
            long accountId = seat.getAccountId();
            getLog().debug("processReservedBetsForSeat: started for accountId={}", accountId);
            ICrashGameRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            boolean locked = false;
            try {
                IBuyInResult buyInResult = null;
                getLog().debug("processReservedBetsForSeat tryLockSeat, accountId={}", accountId);
                locked = tryLockSeat(accountId, 5, TimeUnit.SECONDS);
                if (locked) {
                    getLog().debug("processReservedBetsForSeat lockSeat, accountId={}", accountId);
                    Seat actualSeat = getSeatByAccountId(seat.getAccountId());
                    if (actualSeat == null) {
                        getLog().error("processReservedBetsForSeat: actualSeat is null");
                    } else {
                        Money amount = Money.fromCents(actualSeat.getReservedCrashBets().values().stream()
                                .mapToLong(ICrashBetInfo::getCrashBetAmount)
                                .sum());
                        Money correctedBetAmount = actualSeat.correctAmountWithCanceledBets(amount);
                        saveSeat(0, actualSeat);
                        if (correctedBetAmount.greaterThan(Money.ZERO)) {
                            IGameSocketClient client = actualSeat.getSocketClient();
                            int serverId = client != null ? client.getServerId() :
                                    IRoom.extractServerId(playerInfo.getSessionId());
                            String sessionId = client != null ? client.getSessionId() : playerInfo.getSessionId();
                            IRoomPlayerInfoService playerInfoService = getPlayerInfoService();
                            playerInfo.setPendingOperation(true, "BuyIn, amount=" + amount);
                            playerInfoService.put(playerInfo);
                            buyInResult = socketService.buyIn(serverId, playerInfo.getId(),
                                    sessionId, correctedBetAmount, playerInfo.getGameSessionId(),
                                    playerInfo.getRoomId(), playerInfo.getBuyInCount(), null, null);
                        } else {
                            getLog().warn("processReservedBetsForSeat: correctedBetAmount is zero, seat={}", seat);
                        }
                    }
                } else {
                    getLog().error("processReservedBetsForSeat: cannot lock account, accountId={}", accountId);
                }
                sink.success(buyInResult);
            } catch (Exception e) {
                playerInfo.setPendingOperation(false);
                playerInfoService.put(playerInfo);
                getLog().error("processReservedBetsForSeat Cannot lock player {}, error: {}, error message: {} stacktrace:{}", accountId,
                        e.getClass().getName(), e.getMessage(), e.getStackTrace());
                sink.error(e);
            } finally {
                getLog().debug("processReservedBetsForSeat: completed for accountId={}, locked={}", accountId, locked);
                if (locked) {
                    unlockSeat(accountId);
                    getLog().debug("processReservedBetsForSeat unlockSeat, accountId={}", accountId);
                }
            }
        });
    }

    public void processBuyInForReservedBets() {
        try {
            List<Seat> seats = getSeats();
            CountDownLatch buyInLatch = new CountDownLatch(seats.size());
            getLog().debug("processBuyInForReservedBets: buyInLatch count={}", buyInLatch.getCount());
            for (Seat seat : seats) {
                if (seat.getReservedCrashBets().size() == 0) {
                    buyInLatch.countDown();
                    continue;
                }
                long canceledBetAmount = seat.getCanceledBetAmount();
                long accountId = seat.getAccountId();
                processReservedBetsForSeat(seat).doOnSuccess(buyInResult -> {
                            try {
                                getLog().debug("processBuyInForReservedBets: doOnSuccess for account={}, buyInLatch count={}", accountId,
                                        buyInLatch.getCount());
                                if (buyInResult != null) {
                                    handleBuyInResult(buyInResult, canceledBetAmount, accountId);
                                }
                            } finally {
                                buyInLatch.countDown();
                            }
                        })
                        .doOnError(error -> {
                            try {
                                getLog().debug("processBuyInForReservedBets: doOnError for account={}, buyInLatch count={}", accountId,
                                        buyInLatch.getCount());
                                handleBuyInError(error, canceledBetAmount, accountId, -1);
                            } finally {
                                buyInLatch.countDown();
                            }
                        })
                        .doOnCancel(() -> {
                            buyInLatch.countDown();
                            getLog().error("processBuyInForReservedBets: doOnCancel for accountId={}", accountId);
                        })
                        .subscribeOn(getCrashScheduler())
                        .subscribe();
            }
            boolean success = buyInLatch.await(120, TimeUnit.SECONDS);
            if (!success) {
                getLog().error("processBuyInForReservedBets: buyInLatch timeout, buyInLatch count={}", buyInLatch.getCount());
            } else {
                getLog().debug("processBuyInForReservedBets: buyInLatch.await() success");
            }
        } catch (InterruptedException e) {
            getLog().error("Cannot process buyIn for reserved Bets roomId={}, roundId={}", getId(), roomInfo.getRoundId(), e);
        }
    }

    private void handleBuyInResult(IBuyInResult buyInResult, long canceledBetAmount, long accountId) {
        Seat actualSeat = getSeatByAccountId(accountId);
        ICrashGameRoomPlayerInfo playerInfo = actualSeat.getPlayerInfo();
        ILobbySession session = lobbySessionService.get(actualSeat.getAccountId());
        if (buyInResult.isSuccess()) {
            playerInfo.makeBuyIn(buyInResult.getPlayerRoundId(), buyInResult.getAmount());
            playerInfo.setPendingOperation(false);
            if (buyInResult.getAmount() > 0) {
                playerInfo.incrementBuyInCount();
            }
            playerInfoService.put(playerInfo);
            actualSeat.switchReservedFlag();
            actualSeat.updatePlayerRoundInfo(buyInResult.getPlayerRoundId());
            if (session == null) {
                getLog().warn("lobbySession is null, accountId={}", actualSeat.getAccountId());
            } else {
                session.setBalance(buyInResult.getBalance());
                lobbySessionService.add(session);
            }
            actualSeat.setPlayerInfo(playerInfo);
            saveSeat(getSeatNumber(actualSeat), actualSeat);
            ITransportObject currentSeatResponse = getTOFactoryService().createCrashAllBetsResponse(
                    System.currentTimeMillis(), TObject.SERVER_RID, getSeatNumber(actualSeat),
                    actualSeat.getNickname(), buyInResult.getBalance(), buyInResult.getAmount()
            );
            actualSeat.sendMessage(currentSeatResponse);
            if(roomInfoService != null) {
                executeOnAllMembers(createSendSeatMessageTask(actualSeat.getAccountId(), currentSeatResponse));
            }
            getLog().debug("handleBuyInResult sending message, seat isDisconnected={}", actualSeat.isDisconnected());
        } else {
            Throwable exception = new BuyInFailedException(buyInResult.getErrorDescription(), buyInResult.isFatalError(),
                    buyInResult.getErrorCode());
            handleBuyInError(exception, canceledBetAmount, accountId, session.getBalance());
        }
    }

    private void handleBuyInError(Throwable e, long canceledBetAmount, long accountId, long balance) {
        Seat actualSeat = getSeatByAccountId(accountId);
        ICrashGameRoomPlayerInfo playerInfo = actualSeat.getPlayerInfo();
        getLog().error("Failed to perform buy in", e);
        playerInfo.setPendingOperation(false);
        playerInfoService.put(playerInfo);
        //rollback changes
        actualSeat.setCanceledBetAmount(canceledBetAmount);
        int errorCode = getBuyInFailedErrorCode(e);
        actualSeat.getReservedCrashBets()
                .forEach((id, crashBetInfo) -> actualSeat.cancelCrashBet(id));
        saveSeat(getSeatNumber(actualSeat), actualSeat);
        ICrashAllBetsRejected currentSeatResponse = getTOFactoryService().createCrashAllBetsRejectedDetailedResponse(
                System.currentTimeMillis(), TObject.SERVER_RID, getSeatNumber(actualSeat),
                actualSeat.getNickname(), errorCode, "BuyIn failed, reason: " + e.getMessage()
        );
        if (balance >= 0) {
            currentSeatResponse.setBalance(balance);
        }
        ITransportObject allSeatsResponse = getTOFactoryService().createCrashAllBetsRejectedResponse(
                System.currentTimeMillis(), TObject.SERVER_RID, getSeatNumber(actualSeat),
                actualSeat.getNickname()
        );
        if(roomInfoService != null) {
            executeOnAllMembers(createSendSeatMessageTask(actualSeat.getAccountId(), currentSeatResponse));
        }
        actualSeat.sendMessage(currentSeatResponse);
        executeOnAllMembers(createSendSeatsMessageTask(accountId, true,
                -1, allSeatsResponse, true));
    }

    public boolean isSendRealBetWin() {
        IMultiNodeRoomInfo info = getRoomInfo();
        ICrashGameSetting settings = settingsService.getSettings(info.getBankId(), getGameType().getGameId(), info.getCurrency());
        return settings.isSendRealBetWin();
    }

    public void clearReservedBets(Seat seat) {
        if (seat != null) {
            Map<String, ICrashBetInfo> reservedBets = seat.getReservedCrashBets();
            if (!reservedBets.isEmpty()) {
                for (String key : reservedBets.keySet()) {
                    getGameState().processCancelCrashMultiplier(seat.getAccountId(), key, -1,
                            false, null);
                }
                getLog().error("clearReservedBets: accountId={} Impossible situation. " +
                        "Check why reserved bets weren't sent for seat={}", seat.getAccountId(), seat);
            }
        }
    }

    public void clearReservedBetsAllSeats(){
        for (Seat seat : getAllSeats()) {
            clearReservedBets(seat);
        }
    }

    public int getBuyInFailedErrorCode(Throwable e) {
        if (e instanceof BuyInFailedException) {
            BuyInFailedException bfExc = (BuyInFailedException) e;
            if (bfExc.getErrorCode() > 0) {
                return ErrorCodes.translateGameServerErrorCode(bfExc.getErrorCode());
            }
            return bfExc.isFatal() ? ErrorCodes.BAD_BUYIN : ErrorCodes.NOT_FATAL_BAD_BUYIN;
        }
        return ErrorCodes.INTERNAL_ERROR;
    }
}
