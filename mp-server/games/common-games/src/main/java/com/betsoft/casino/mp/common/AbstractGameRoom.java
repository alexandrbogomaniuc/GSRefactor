package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.events.ITimedEvent;
import com.betsoft.casino.mp.events.TimedEvent;
import com.betsoft.casino.mp.events.TimedEventManager;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.battleground.ITransportObserver;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.model.movement.BezierTrajectory;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.privateroom.Player;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.IServerMessage;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.InboundObject;
import com.betsoft.casino.utils.TObject;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.mp.TransactionErrorCodes;
import com.abs.casino.common.util.ILongIdGenerator;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.utils.TObject.SERVER_RID;

/**
 * User: flsh
 * Date: 06.02.19.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractGameRoom<GAME extends IGame, MAP extends IMap,
        SEAT extends ISeat, SNAPSHOT extends IGameRoomSnapshot,
        ENEMY extends IEnemy, ENEMY_TYPE extends IEnemyType, ROOM_INFO extends IRoomInfo, RPI extends IRoomPlayerInfo>
        implements IRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI> {

    private static final Logger LOG = LogManager.getLogger(AbstractGameRoom.class);
    protected static final long MAX_LOCK_TIMEOUT = 100000;
    protected static final int MAX_OBSERVERS = 30;
    protected final transient Logger logger;
    protected ROOM_INFO roomInfo;
    protected List<SEAT> removedSeats;
    protected final transient GAME game;
    protected transient MAP map;
    protected int nextMapId;
    protected final transient GameMapStore gameMapStore;
    protected final transient IRoomPlayerInfoService playerInfoService;
    protected final transient ISocketService socketService;
    protected final transient IPlayerStatsService playerStatsService;
    protected final transient IPlayerQuestsService playerQuestsService;
    protected final transient ILongIdGenerator idGenerator;
    protected final transient IWeaponService weaponService;
    protected final transient IActiveFrbSessionService activeFrbSessionService;
    protected final transient IActiveCashBonusSessionService activeCashBonusSessionService;
    protected final transient ITournamentService tournamentService;
    protected final transient IRoomInfoService roomInfoService;
    protected final transient AtomicBoolean tableStart = new AtomicBoolean(false);
    protected final transient IExecutorService remoteExecutorService;
    protected final transient ICurrencyRateService currencyRateService;
    protected final transient IPlayerProfileService playerProfileService;
    protected final transient ITransportObjectsFactoryService toFactoryService;
    protected final transient IAnalyticsDBClientService analyticsDBClientService;
    protected final transient IAsyncExecutorService asyncExecutorService;
    //
    protected int defaultTimeMillis = 100;
    protected final int roundDuration;
    protected transient TimedEventManager eventManager;
    protected GameTimer timer;
    protected transient IGameState gameState;
    //key is accountId
    protected final transient ConcurrentMap<Long, IGameSocketClient> observePlayers = new ConcurrentHashMap<>();
    protected transient Disposable updateTimer;
    protected final transient IGameConfigService gameConfigService;
    protected final transient IServerConfigService serverConfigService;
    protected final transient ILobbySessionService lobbySessionService;

    protected final transient Set<IRoomStateChangedListener> stateChangedListeners = new HashSet<>(1);
    protected final transient Set<ISeatsCountChangedListener> seatsCountChangedListeners = new HashSet<>(1);
    protected final transient Set<IRoomOpenedListener> roomOpenedListeners = new HashSet<>(1);
    protected final transient Set<IGameRoomStartListener> gameRoomStartListeners = new HashSet<>(1);
    protected final transient Set<IRoomClosedListener> roomClosedListeners = new HashSet<>(1);

    protected final transient IPendingOperationService pendingOperationService;
    
    private static final Scheduler sch = Schedulers.newElastic("ROOM_SCHEDULER", 60 * 10);

    protected Long lastRoundStartTime = null;

    protected AbstractGameRoom(ApplicationContext context, Logger logger, ROOM_INFO roomInfo, GAME game,
                               MAP map, IPlayerStatsService playerStatsService,
                               IPlayerQuestsService playerQuestsService,
                               IWeaponService weaponService, IExecutorService remoteExecutorService,
                               IPlayerProfileService playerProfileService,
                               IGameConfigService gameConfigService,
                               IActiveFrbSessionService activeFrbSessionService,
                               IActiveCashBonusSessionService activeCashBonusSessionService,
                               ITournamentService tournamentService) {
        this.logger = logger;
        this.roomInfo = roomInfo;
        this.removedSeats = new ArrayList<>();
        this.game = game;
        this.map = map;
        if (map != null) {
            map.setLogger(logger);
        }
        generateNextMapId();
        this.toFactoryService = (ITransportObjectsFactoryService) context.getBean("transportObjectsFactoryService");
        this.gameMapStore = context.getBean("gameMapStore", GameMapStore.class);
        this.playerInfoService = (IRoomPlayerInfoService) context.getBean("playerInfoService");
        this.socketService = (ISocketService) context.getBean("socketService");
        this.playerStatsService = playerStatsService;
        this.playerQuestsService = playerQuestsService;
        this.idGenerator = (ILongIdGenerator) context.getBean("idGenerator");
        this.weaponService = weaponService;
        this.remoteExecutorService = remoteExecutorService;
        this.roomInfoService = getRoomInfoService(context);
        this.currencyRateService = (ICurrencyRateService) context.getBean("currencyRateService");
        this.playerProfileService = playerProfileService;
        //todo: fix kryo-validation, roomInfo, gameType, map may be null
        GameType gameType = roomInfo == null ? null : roomInfo.getGameType();
        this.roundDuration = roomInfo == null ? 290 : roomInfo.getRoundDuration();
        this.gameConfigService = gameConfigService;
        this.lobbySessionService = (ILobbySessionService) context.getBean("lobbySessionService");
        if (remoteExecutorService == null) {
            getLog().debug("remoteExecutorService is null, distributed calls not available");
        }
        this.activeFrbSessionService = activeFrbSessionService;
        this.activeCashBonusSessionService = activeCashBonusSessionService;
        this.tournamentService = tournamentService;
        this.serverConfigService = (IServerConfigService) context.getBean("serverConfigService");
        if (roomInfoService != null) {
            gameRoomStartListeners.add(roomInfoService);
        }
        this.pendingOperationService = (IPendingOperationService) context.getBean("pendingOperationService");
        this.analyticsDBClientService = (IAnalyticsDBClientService) context.getBean("analyticsDBClientService");
        this.asyncExecutorService = (IAsyncExecutorService) context.getBean("bigQueryAsyncExecutor");
    }

    @Override
    public ILobbySessionService getLobbySessionService() {
        return this.lobbySessionService;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public Scheduler getScheduler() {
        return sch;
    }

    protected void rememberRemovedSeat(SEAT seat){
        this.removedSeats.add(seat);
    }

    protected void cleanRemovedSeats(){
        this.removedSeats.clear();
    }

    protected abstract IRoomInfoService getRoomInfoService(ApplicationContext context);
    protected abstract List<ITransportSeat> getTransportSeats();

    @Override
    public void executeOnAllMembers(Runnable task) {
        if (remoteExecutorService != null && !remoteExecutorService.isShutdown()) {
            try {
                remoteExecutorService.executeOnAllMembers(task);
            } catch (RejectedExecutionException e) {
                getLog().warn("Skip sending {}, remoteExecutorService not started", task.getClass().getSimpleName());
            }
        }
    }

    @Override
    public Runnable createSendSeatOwnerMessageTask(ITransportObject message) {
        return roomInfoService.createSendSeatOwnerMessageTask(getId(), getGameType(), serverConfigService.getServerId(), message);
    }

    @Override
    public Runnable createSendSeatsMessageTask(Long relatedAccountId, boolean notSendToRelatedAccountId, long relatedRequestId,
                                               ITransportObject message, boolean sendToAllObservers) {
        return roomInfoService.createSendSeatsMessageTask(getId(), getGameType(), serverConfigService.getServerId(), relatedAccountId,
                notSendToRelatedAccountId, relatedRequestId, message, sendToAllObservers);
    }

    @Override
    public Runnable createUpdateCrashHistoryTask(ICrashRoundInfo crashRoundInfo) {
        return roomInfoService.createUpdateCrashHistoryTask(getId(), getGameType(), serverConfigService.getServerId(), crashRoundInfo);
    }

    @Override
    public Runnable createSendSeatMessageTask(Long accountId, ITransportObject message) {
        return roomInfoService.createSendSeatMessageTask(getId(), getGameType(), serverConfigService.getServerId(), accountId, message);
    }

    @Override
    public Runnable createSendAllObserversNoSeatMessageTask(ITransportObject message) {
        return roomInfoService.createSendAllObserversNoSeatMessageTask(getId(), getGameType(), serverConfigService.getServerId(), message);
    }

    @Override
    public boolean isBuyInAllowed(SEAT seat) {
        if (seat == null) {
            return getGameState() instanceof AbstractWaitingPlayersGameState;
        }
        //noinspection unchecked
        return getGameState().isBuyInAllowed(seat);
    }

    @Override
    public boolean tryChangeBetLevel(long accountId, int betLevel) {
        return false;
    }

    protected abstract IGameState getWaitingPlayersGameState();

    protected abstract List<ITransportEnemy> getTransportEnemies();

    protected abstract void calculateWeaponsSurplusCompensation(SEAT seat);

    protected abstract List<Integer> getWeaponLootBoxPrices();

    /**
     * Converts enemies to transport objects for sending to client side
     * @param enemyTypes array enemies
     * @return array of transport enemies
     */
    protected List<ITransportEnemy> convertEnemies(IEnemyType[] enemyTypes) {
        List<ITransportEnemy> enemies = new ArrayList<>(enemyTypes.length);
        if (getTOFactoryService() != null) {
            for (IEnemyType enemy : enemyTypes) {
                enemies.add(getTOFactoryService().createEnemy(enemy.getId(), enemy.getWidth(),
                        enemy.getHeight(), enemy.getSkin(1).getSpeed(), enemy.getPayTable().size(), enemy.getSumAward(),
                        enemy.getSkins().size(), enemy.isBoss()));
            }
        } else {
            //may be for kryo serialization tests
            getLog().error("convertEnemies: TransportObjectsFactoryService is null, cannot convert enemies");
        }
        return Collections.unmodifiableList(enemies);
    }

    @Override
    public GAME getGame() {
        return game;
    }

    @Override
    public MAP getMap() {
        return map;
    }

    @Override
    public RoomState getState() {
        return gameState.getRoomState();
    }

    @Override
    public IGameState getGameState() {
        return gameState;
    }

    @Override
    public void removeAllEnemies() {
        getMap().removeAllEnemies();
    }

    /**
     * Starts room if first player try to enter to room.
     * @throws CommonException if any unexpected error occur
     */
    public void start() throws CommonException {
        getLog().info("Start GameRoom: {}, roundId={}", getId(), getRoomInfo().getRoundId());
        if (isRoomStarted()) {
            getLog().warn("GameRoom already started");
            return;
        }
        lock();
        try {
            this.tableStart.set(true);
            this.eventManager = new TimedEventManager(getId());
            this.timer = new GameTimer(600000, false);
            registerEvent(this.timer);
            eventManager.start();
            if (gameState == null) { //may be not null if restored from snapshot
                gameState = this.getWaitingPlayersGameState();
                gameState.init();
            } else {
                getLog().debug("Start from snapshot, gameState found: {}", gameState);
                gameState.restoreGameRoom(this);
            }
        } finally {
            unlock();
        }
        for (IGameRoomStartListener listener : gameRoomStartListeners) {
            listener.notifyRoomStarted(this);
        }
    }

    /**
     * Stops room of no activity or if server is in process of stop.
     * @return  snapshot of room for saving to Cassandra database
     * @throws CommonException if any unexpected error occur
     */
    public SNAPSHOT shutdown() throws CommonException {
        getLog().debug("Shutdown started");
        lock();
        try {
            this.tableStart.set(false);
            stopTimer();
            eventManager.shutdown();
            SNAPSHOT snapshot = null;
            if (gameState instanceof AbstractWaitingPlayersGameState && getSeatsCount() <= 0) {
                getLog().debug("Skip make snapshot, empty room");
            } else {
                snapshot = getSnapshot();
            }
            getLog().debug("Shutdown finished");
            return snapshot;
        } finally {
            unlock();
        }
    }

    @Override
    public ILongIdGenerator getIdGenerator() {
        return idGenerator;
    }

    /**
     * Converts points of trajectory for client side
     * @param origin original trajectory
     * @return converted trajectory
     */
    @Override
    public Trajectory convertFullTrajectory(Trajectory origin) {
        Trajectory trajectory = new Trajectory(origin.getSpeed());
        for (Point point : origin.getPoints()) {
            trajectory.addPoint(point.convert(map.getCoords()));
        }
        return trajectory;
    }

    /**
     * Converts points of trajectory for client side with cut old points
     * @param origin original trajectory
     * @param time current time
     * @return converted trajectory
     */
    @Override
    public Trajectory convertTrajectory(Trajectory origin, long time) {
        List<Point> points = origin.getPoints();
        Trajectory trajectory = origin instanceof BezierTrajectory ?
                new BezierTrajectory(origin.getSpeed(), points) : new Trajectory(origin.getSpeed());

        trajectory.setMaxSize(origin.getMaxSize());

        int i = 1;
        while (i < points.size() && points.get(i).getTime() < time) {
            i++;
        }
        for (int j = i - 1; j < points.size(); j++) {
            Point point = points.get(j);
            trajectory.addPoint(point.convert(map.getCoords()));
        }
        return trajectory;
    }

    /**
     * Converts enemy for client side
     * @param enemy room enemy
     * @param fillTrajectory true if you need convert trajectory
     * @return converted room enemy
     */
    @Override
    public IRoomEnemy convert(ENEMY enemy, boolean fillTrajectory) {
        IEnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        int currentPercent = (int) (enemy.getEnergy() * 100 / enemy.getFullEnergy());

        return getTOFactoryService().createRoomEnemy(
                enemy.getId(),
                enemyType.getId(),
                enemyType.isBoss(),
                enemy.getSpeed(),
                enemy.getAwardedPrizesAsString(),
                enemy.getAwardedSum().toDoubleCents(),
                currentPercent,
                enemy.getSkin(),
                fillTrajectory ? convertTrajectory(enemy.getTrajectory(), System.currentTimeMillis()) : null,
                enemy.getParentEnemyId(),
                100,
                enemy.getMembers(),
                enemy.getSwarmId(),
                enemy.getSwarmType()
        );
    }

    @Override
    public ROOM_INFO getRoomInfo() {
        if(roomInfo == null) {
            getLog().warn("getRoomInfo: roomInfo requested is null, in the room object:{}", this);
        }
        return roomInfo;
    }

    @Override
    public int getObserverCount() {
        return observePlayers.size();
    }

    @Override
    public Collection<IGameSocketClient> getObservers() {
        return observePlayers.values();
    }

    @Override
    public IGameSocketClient getObserver(Long accountId) {
        if(observePlayers == null || observePlayers.values() == null || accountId == null) {
            return null;
        }

        return observePlayers.get(accountId);
    }

    @Override
    public IGameSocketClient getObserver(String nickname) {
        if(observePlayers == null || observePlayers.values() == null || StringUtils.isTrimmedEmpty(nickname)) {
            return null;
        }

        IGameSocketClient observer = observePlayers.values().stream()
                .filter(socketClient -> socketClient != null && !StringUtils.isTrimmedEmpty(socketClient.getNickname())
                        && socketClient.getNickname().equals(nickname))
                .findFirst()
                .orElse(null);

        return observer;
    }

    /**
     * Returns list of real players (seats) in room
     * @return list of seats
     */
    @Override
    public List<SEAT> getSeats() {
        List<SEAT> result = new ArrayList<>();
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                result.add(seat);
            }
        }
        return result;
    }

    @Override
    public void clearSeatDataFromPreviousRound() {
        //nop
    }

    /**
     * Gets seat of player by number. For action games only.
     * @param number number of place of seat
     * @return seat or null
     */
    @Override
    public SEAT getSeat(int number) {
        List<SEAT> allSeats = getAllSeats();
        if (number < 0 || number + 1 > allSeats.size()) {
            return null;
        }
        return allSeats.get(number);
    }

    /**
     * Gets seat of player by accountId
     * @param accountId accountId of player
     * @return seat or null
     */
    @Override
    public SEAT getSeatByAccountId(long accountId) {
        for (SEAT seat : getAllSeats()) {
            if (seat != null && seat.getAccountId() == accountId) {
                return seat;
            }
        }
        return null;
    }

    /**
     * Returns count of seats in room
     * @return count of seats in room
     */
    @Override
    public short getSeatsCount() {
        short count = 0;
        for (ISeat seat : getAllSeats()) {
            if (seat != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets duration time of round in room
     * @return duration time of round (ms)
     */
    @Override
    public int getRoundDuration() {
        return roundDuration;
    }

    /**
     * Switches the map before the next round
     */
    @Override
    public void toggleMap() {
        map.setMapShape(gameMapStore.getMap(nextMapId));
        generateNextMapId();
    }

    protected void generateNextMapId() {
        int totalMaps = getGameType().getMaps().size();
        if (totalMaps == 1) {
            nextMapId = getGameType().getMaps().get(0);
        } else {
            int id = nextMapId;
            while (id == nextMapId) {
                id = getGameType().getMaps().get(RNG.nextInt(totalMaps));
            }
            nextMapId = id;
        }
    }

    public boolean isRoomStarted() {
        return tableStart.get();
    }

    protected void assertRoomStarted() {
        if (!isRoomStarted()) {
            throw new RuntimeException("Room is not started, id=" + getId());
        }
    }

    protected void checkAndStartRoom() throws CommonException {
        if (!isRoomStarted()) {
            start();
        }
    }

    public boolean shutdownRoomIfEmpty() throws CommonException {
        boolean isRoomStarted = isRoomStarted();
        boolean isRoomWaitState = roomInfo.getState() != null && roomInfo.getState().equals(RoomState.WAIT);
        boolean noObservers = observePlayers.isEmpty();
        boolean allSeatsEmpty = getSeatsCount() == 0;

        getLog().info("shutdownRoomIfEmpty: isRoomStarted={}, isRoomWaitState={}, noObservers={}, allSeatsEmpty={}",
                isRoomStarted, isRoomWaitState, noObservers, allSeatsEmpty);

        if (isRoomStarted && isRoomWaitState && noObservers && allSeatsEmpty) {
            getLog().info("shutdownRoomIfEmpty: Room is empty and waiting, shutdown");
            shutdown();
            return true;
        }
        return false;
    }

    protected long getTotalKillsXP(SEAT seat) {
        return seat.getCurrentScore().getLongAmount();
    }

    protected void updateStatOnEndRound(SEAT seat, IPlayerRoundInfo roundInfo) {
        //nop by default
    }
    @Override
    public void sendRoundResults() {
        List<ITransportSeat> transportSeats = getTransportSeats();

        List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs = new ArrayList<>();

        List<SEAT> seats = getAllSeats();

        for (SEAT seat : seats) {
            if (seat != null) {

                IRoundResult roundResult = getRoundResult(seat, transportSeats);

                seatsRoundResultsPairs.add(new Pair<>(seat, roundResult));

                if (!seat.isDisconnected()) {
                    seat.sendMessage(roundResult);
                } else {
                    getLog().warn("sendRoundResults: Cannot send round result from this server, seat is disconnected or " +
                            "on other server, try send remote");
                    if (roomInfoService != null) {
                        executeOnAllMembers(createSendSeatMessageTask(seat.getAccountId(), roundResult));
                    }
                }
            }
        }

        for (SEAT seat : this.removedSeats) {
            if (seat != null && seat.getCurrentPlayerRoundInfo() != null && seat.getCurrentPlayerRoundInfo().getPlayerRoundId() > 0) {
                IRoundResult roundResult = getRoundResult(seat, transportSeats);
                seatsRoundResultsPairs.add(new Pair<>(seat, roundResult));
            }
        }

        cleanRemovedSeats();

        getLog().debug("sendRoundResults: seatsRoundResultsPairs={}", seatsRoundResultsPairs);

        List<Map<String, Object>> roundResults = analyticsDBClientService.prepareRoundResult(seatsRoundResultsPairs, this);

        asyncExecutorService.execute(() ->
                analyticsDBClientService.saveRoundResults(roundResults)
        );

        getLog().debug("sendRoundResults end");

        if (this instanceof ICAFRoom) {

            IRoomInfo roomInfo = getRoomInfo();

            if (roomInfo == null) {
                getLog().error("sendRoundResults: roomInfo is null, for roomId: {}", getId());
            } else {
                if (roomInfo.isPrivateRoom()) {
                    List<IGameSocketClient> clients = seats.stream()
                            .filter(s -> s != null && s.getSocketClient() != null)
                            .map(ISeat::getSocketClient)
                            .collect(Collectors.toList());

                    getLog().debug("sendRoundResults: call updatePlayersStatus to set Status.WAITING for clients={}", clients);

                    if(this instanceof AbstractMultiNodeGameRoom) {
                        ((ICAFRoom)this).updatePlayersStatus(clients, Status.WAITING, false, true);
                    } else if(this instanceof AbstractSingleNodeGameRoom) {
                        ((ICAFRoom)this).updatePlayersStatus(clients, Status.WAITING, false, false);
                    }
                } else {
                    getLog().debug("sendRoundResults: Room is not private skip updatePlayersStatus");
                }
            }
        }
    }

    private IRoundResult getRoundResult(SEAT seat, List<ITransportSeat> transportSeats) {

        getLog().debug("getRoundResult: seat={}, transportSeats={}", seat, transportSeats);

        IPlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
        updateStatOnEndRound(seat, roundInfo);

        IExperience prevScore = seat.getPlayerInfo().getPrevXP();
        int prevLevel = AchievementHelper.getPlayerLevel(prevScore);
        int level = AchievementHelper.getPlayerLevel(seat.getTotalScore());

        long totalKillsXP = getTotalKillsXP(seat);
        long balance = getBalance(seat);
        double winAmount = seat.getRoundWin().add(seat.getRebuyFromWin()).toDoubleCents();
        double realWinAmount = winAmount;

        IActiveCashBonusSession cashBonusSession = seat.getPlayerInfo().getActiveCashBonusSession();

        if (cashBonusSession != null && cashBonusSession.getMaxWinLimit() > 0 &&
                cashBonusSession.getAmountToRelease() <= 0) {
            realWinAmount = Money.fromCents(cashBonusSession.getMaxWinLimit()).toDoubleCents();
            getLog().debug("getRoundResult: found limit for cashBonus. realWinAmount={}, winAmount={}", realWinAmount, winAmount);
        }

        IRoundResult roundResult = this.createRoundResult(
                winAmount,
                realWinAmount,
                balance,
                level,
                prevLevel,
                totalKillsXP,
            seat,
            transportSeats,
                roundInfo);

        getLog().debug("getRoundResult: roundResult={}", roundResult);

        return roundResult;
    }

    protected int getUnplayedFreeShots(SEAT seat) {
        return 0;
    }

    protected IRoundResult createRoundResult(double winAmount, double realWinAmount, long balance, int level, int prevLevel, long totalKillsXP,
                                             SEAT seat, List<ITransportSeat> transportSeats, IPlayerRoundInfo roundInfo) {
        return getTOFactoryService().createRoundResult(getCurrentTime(), SERVER_RID,
                winAmount,
                seat.getRebuyFromWin().toDoubleCents(),
                balance,
                seat.getCurrentScore().getLongAmount(), seat.getTotalScore().getLongAmount(),
                0, 0, nextMapId, transportSeats, 0, seat.getRoundWinInCredits(), 0, 0, 0,
                AchievementHelper.getXP(level), Collections.emptyList(), totalKillsXP, 0, 0,
                getTOFactoryService().createLevelInfo(prevLevel, seat.getPlayerInfo().getPrevXP().getLongAmount(),
                        AchievementHelper.getXP(prevLevel), AchievementHelper.getXP(prevLevel + 1)),
                getTOFactoryService().createLevelInfo(level, seat.getTotalScore().getLongAmount(),
                        AchievementHelper.getXP(level), AchievementHelper.getXP(level + 1)),
                0, seat.getQuestsCompletedCount(), seat.getQuestsPayouts(),
                getRoomInfo().getRoundId(), Collections.emptyList(), 0,
                realWinAmount, roundInfo.getFreeShotsWon(), roundInfo.getMoneyWheelCompleted(),
                roundInfo.getMoneyWheelPayouts(), roundInfo.getTotalDamage(), null
        );
    }
    @Override
    public SEAT tryReconnect(SEAT seat) throws CommonException {
        checkAndStartRoom();
        for (SEAT currentSeat : getAllSeats()) {
            if (currentSeat != null && currentSeat.getAccountId() == seat.getAccountId()) {
                getLog().info("processSitIn [tryReconnect]: found already seat: {}, refresh reference to " +
                        "socket client, seat={}", currentSeat, seat);
                IGameSocketClient gameSocketClient = seat.getSocketClient();
                //noinspection unchecked
                currentSeat.setSocketClient(gameSocketClient);
                gameSocketClient.setSeatNumber(getSeatNumber(currentSeat));
                currentSeat.setWantSitOut(false);
                IActiveCashBonusSession activeCashBonusSession = currentSeat.getPlayerInfo().getActiveCashBonusSession();
                if (activeCashBonusSession != null && activeCashBonusSession.isActive()) {
                    getLog().debug("tryReconnect: activeCashBonusSession={}", activeCashBonusSession);
                    updateCashBonus(currentSeat, activeCashBonusSession.getBalance(), activeCashBonusSession.getBetSum());
                }
                if (currentSeat instanceof IMultiNodeSeat) {
                    saveSeat(getSeatNumber(currentSeat), currentSeat);
                }
                return currentSeat;
            }
        }
        return null;
    }

    protected void finishSitIn(SEAT seat) throws CommonException {
        //noinspection unchecked
        getGameState().processSitIn(seat);
        seat.setJoinDate(getCurrentTime());
        seat.getPlayerInfo().setPrevXP(seat.getPlayerInfo().getTotalScore());
        for (ISeatsCountChangedListener listener : seatsCountChangedListeners) {
            listener.notifySeatAdded(this, seat);
        }
    }

    @Override
    public void rollbackSitIn(SEAT seat) {
        assertRoomStarted();
        int seatNumber = getSeatNumber(seat);
        if (seatNumber >= 0) {
            removeSeat(seatNumber, seat);
        }
        try {
            //noinspection unchecked
            getGameState().processSitOut(seat);
        } catch (CommonException e) {
            getLog().error("rollbackSitIn: error, seat={}", seat);
        }
        IGameSocketClient socketClient = seat.getSocketClient();
        socketClient.setSeatNumber(-1);
        seat.setJoinDate(-1);
    }

    @Override
    public SEAT processSitOut(IGameSocketClient client, ISitOut request, int seatNumber, long accountId,
                              boolean updateStats) throws CommonException {
        boolean locked;

        try {
            locked = tryLock(5, TimeUnit.SECONDS);
            getLog().debug("processSitOut: accountId={}, locked={}, client={}, request={}, seatNumber={}, updateStats={}",
                    accountId, locked, client, request, seatNumber, updateStats);
        } catch (InterruptedException e) {
            if (request != null) {
                client.sendMessage(getTOFactoryService().createError(ErrorCodes.CANNOT_OBTAIN_LOCK,
                        "Cannot obtain room lock", System.currentTimeMillis(), request.getRid()), request);
            }
            throw new CommonException("room lock failed, reason=" + e.getMessage());
        }

        if (!locked) {
            getLog().error("processSitOut: room lock failed by timeout for accountId={}", accountId);
            if (request != null) {
                client.sendMessage(getTOFactoryService().createError(ErrorCodes.CANNOT_OBTAIN_LOCK,
                        "room lock failed by timeout", System.currentTimeMillis(), request.getRid()), request);
            }
            throw new CommonException("room lock failed by timeout");
        }

        try {
            return processSitOut(client, request, seatNumber, accountId, updateStats, false);
        } finally {
            unlock();
        }
    }

    protected boolean isSitOutNotAllowed(SEAT seat) {
        return false;
    }

    protected SEAT processSitOut(IGameSocketClient client, ISitOut request, int seatNumber, long accountId,
                                 boolean updateStats, boolean bulletsConvertedToMoney) {
        assertRoomStarted();

        getLog().debug("processSitOut: seatNumber: {}, accountId: {}", seatNumber, accountId);

        if (seatNumber < 0) {
            getLog().warn("processSitOut: seat already sitOut, accountId={}", accountId);
            return null;
        }

        SEAT seat = getSeatByAccountId(accountId);

        getLog().debug("processSitOut: seat: {}", seat);

        if (seat != null) {

            getLog().info("processSitOut: seat found={}", seat);

            if (playerInfoService.isLocked(accountId)) {
                getLog().warn("processSitOut: found account lock, this may be dead lock");
            }

            boolean locked = false;

            IGameSocketClient socketClient = seat.getSocketClient();

            getLog().debug("processSitOut: socketClient: {}", socketClient);

            try {

                locked = playerInfoService.tryLock(accountId, 20, TimeUnit.SECONDS);

                getLog().debug("processSitOut: accountId={}, locked={}", accountId, locked);

                if (!locked) {
                    throw new CommonException("Cannot obtain lock by account");
                }

                if (isSitOutNotAllowed(seat)) {

                    getLog().debug("processSitOut: SitOut not allowed, just return, seat={}, seat.playerInfo={}",
                            seat, seat.getPlayerInfo());

                    if (socketClient != null) {

                        int errorCode = roomInfo.isPrivateRoom() ? ErrorCodes.SIT_OUT_NOT_ALLOWED : ErrorCodes.BAD_REQUEST;
                        socketClient.sendMessage(getTOFactoryService().createError(errorCode,
                                "SitOut not allowed", getCurrentTime(),
                                request != null ? request.getRid() : TObject.SERVER_RID));
                    }

                    return null;
                }

                seat.setSitOutStarted(true);

                IRoomPlayerInfo playerInfo = seat.getPlayerInfo();

                getLog().debug("processSitOut: playerInfo={}", playerInfo);

                compensateUnusedFeatures(seat);

                boolean isBonusSession = getRoomInfo().getMoneyType().equals(MoneyType.CASHBONUS) ||
                        getRoomInfo().getMoneyType().equals(MoneyType.FRB) || getRoomInfo().getMoneyType().equals(MoneyType.TOURNAMENT);

                if (isBonusSession && (!getGameType().equals(GameType.PIRATES)
                        && !getGameType().equals(GameType.AMAZON) && !getGameType().equals(GameType.MISSION_AMAZON))) {
                    clearCrossRoundSeatStats(seat);
                }

                if (updateStats) {
                    updateSeatStats(seat);
                } else {
                    persistCrossRoundSeatStats(seat);
                }

                int serverId = client != null ? client.getServerId() : IRoom.extractServerId(playerInfo.getSessionId());
                int oldSeatNumber = getSeatNumber(seat);
                final int ammoAmount = getAmmoAmount(seat);

                IActiveFrbSession activeFrbSession = playerInfo.getActiveFrbSession();
                IActiveCashBonusSession activeCashBonusSession = playerInfo.getActiveCashBonusSession();
                ITournamentSession tournamentSession = playerInfo.getTournamentSession();
                if (seat.getSpecialModeId() != null) {
                    updateSpecialModeQuests(seat);
                }
                if (activeFrbSession == null) {
                    getLog().debug("processSitOut: call processSitOutForNonFrbMode seat={}", seat);
                    processSitOutForNonFrbMode(seat, ammoAmount, activeCashBonusSession, bulletsConvertedToMoney,
                            socketClient, client, request, serverId, seatNumber, oldSeatNumber, tournamentSession, accountId);
                } else {
                    getLog().debug("processSitOut: call processSitOutFrbSession seat={}, activeFrbSession={}", seat, activeFrbSession);
                    processSitOutFrbSession(activeFrbSession, seat, ammoAmount, playerInfo, socketClient, accountId, serverId,
                            request, seatNumber, oldSeatNumber, client);
                }
            } catch (Exception exc) {
                getLog().error("processSitOut: error, seat={}", seat, exc);
            } finally {
                if (locked) {
                    playerInfoService.unlock(accountId);
                    getLog().debug("processSitOut: unlocked, accountId={}", accountId);
                }
            }
        } else{
            getLog().debug("processSitOut, seat not found for accountId: {}", accountId);
        }
        return seat;
    }

    protected int getAmmoAmount(SEAT seat) {
        return 0;
    }

    protected Money getReturnedBet(SEAT seat) {
        return Money.ZERO;
    }

    protected void compensateUnusedFeatures(SEAT seat) {
        //nop by default;
    }

    protected void persistCrossRoundSeatStats(SEAT seat) {
        //nop by default;
    }

    protected void clearCrossRoundSeatStats(SEAT seat) {
        //nop by default;
    }

    protected void processSitOutForNonFrbMode(SEAT seat, int ammoAmount, IActiveCashBonusSession activeCashBonusSession,
                                              boolean bulletsConvertedToMoney,
                                              IGameSocketClient socketClient, IGameSocketClient client,
                                              ISitOut request, int serverId, int seatNumber, int oldSeatNumber,
                                              ITournamentSession tournamentSession, long accountId) throws CommonException {

        int seatAmmoAmount = getAmmoAmount(seat);
        final Money roundWin;
        final Money returnedBet;

        roundWin = seat.retrieveRoundWin();
        returnedBet = getReturnedBet(seat);
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
            getLog().debug("processSitOut: Found rebuyFromWin={}, need correcting amounts. roundWin={}, " +
                            "returnedBet={}, correctedRoundWin={}, correctedReturnedBet={}",
                    seat.getRebuyFromWin().toCents(), roundWin.toCents(),
                    returnedBet.toCents(), correctedRoundWin.toCents(), correctedReturnedBet.toCents());
        }

        IPlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
        updateStatOnEndRound(seat, roundInfo);
        final IPlayerBet playerBet = seat.getCurrentPlayerRoundInfo().getPlayerBet(
                playerInfo.createNewPlayerBet(), seatAmmoAmount);
        try {
            IPlayerProfile playerProfile = playerProfileService.load(playerInfo.getBankId(), seat.getAccountId());
            ISitOutResult sitOutResult;
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
                    getLog().error("sendMQDataSync error, profile={}", playerProfile, e);
                }
                IBattlegroundRoundInfo bgRoundInfo = null;
                if (playerInfo instanceof IBattlegroundRoomPlayerInfo) {
                    IBattlegroundRoomPlayerInfo castedPlayerInfo = (IBattlegroundRoomPlayerInfo) playerInfo;
                    bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();
                    if (bgRoundInfo == null) {
                        castedPlayerInfo.createBattlegroundRoundInfo(roomInfo.getStake().toCents(), 0,
                                0, 0, null, 0,
                                null, seat.getAccountId(), 1, playerInfo.getGameSessionId(), seat.getTotalScore().getLongAmount(),
                                roomInfo.getRoundId(), roomInfo.getLastRoundStartDate(), 0.0, roomInfo.getPrivateRoomId());
                        bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();
                    }
                }
                sitOutResult = socketService.sitOut(serverId, playerInfo.getSessionId(),
                        playerInfo.getGameSessionId(),
                        correctedRoundWin,
                        correctedReturnedBet,
                        playerInfo.getExternalRoundId(),
                        getRoomInfo().getId(),
                        accountId,
                        playerBet,
                        bgRoundInfo);

                checkResultAndFinishSitOut(sitOutResult, seat, playerInfo, client, request, roundWin, ammoAmount,
                        socketClient, null, serverId, seatNumber, oldSeatNumber);
            }
        } catch (Exception e) {
            handleSitOutError(e, seat, roundWin, ammoAmount, playerInfo, request, seatNumber, oldSeatNumber, client);
        }
    }

    protected void handleSitOutError(Exception e, SEAT seat, Money roundWin, int ammoAmount, IRoomPlayerInfo playerInfo,
                                     ISitOut request, int seatNumber, int oldSeatNumber, IGameSocketClient client) throws CommonException {
        getLog().error("processSitOut: failed, rollback roundWin={} and ammoAmount={}",
                roundWin, ammoAmount, e);
        if (getRoomInfo().getMoneyType() == MoneyType.FREE) {
            getLog().info("processSitOut failed, but room is free mode. " +
                    "Ignore error and force sitOut");
            playerInfo.setPendingOperation(false);
            savePlayerInfo(playerInfo);
            finishSitOut(request, seatNumber, seat, oldSeatNumber, -1, false, false);
        } else {
            seat.rollbackRoundWinAndAmmo(roundWin, ammoAmount);
            seat.setSitOutStarted(false);
            savePlayerInfo(playerInfo);
            if (client != null) {
                client.sendMessage(getTOFactoryService().createError(ErrorCodes.INTERNAL_ERROR, "SitOut failed",
                        getCurrentTime(),
                        request != null ? request.getRid() : TObject.SERVER_RID));
            }
        }
        getGameState().processSitOut(seat);
    }

    protected void processSitOutCashBonusSession(IActiveCashBonusSession activeCashBonusSession, boolean bulletsConvertedToMoney,
                                                 Money correctedRoundWin, Money correctedReturnedBet,
                                                 SEAT seat, IPlayerProfile playerProfile, IPlayerBet playerBet,
                                                 IRoomPlayerInfo playerInfo, IGameSocketClient socketClient,
                                                 IGameSocketClient client,
                                                 ISitOut request, Money roundWin, int ammoAmount,
                                                 int serverId, int seatNumber, int oldSeatNumber) throws CommonException {
        if (activeCashBonusSession.isActive()) {
            long remainingBalance = activeCashBonusSession.getBalance();
            long balance = bulletsConvertedToMoney ? remainingBalance :
                    correctedRoundWin.toCents() + correctedReturnedBet.toCents() + remainingBalance;
            getLog().debug("processSitOut: before change, activeCashBonusSession={}, " +
                            "new bonus balance={}, bulletsConvertedToMoney={}, remainingBalance={}",
                    activeCashBonusSession, balance, bulletsConvertedToMoney, remainingBalance);
            activeCashBonusSession.setBalance(balance > 0 ? balance : 0);
            updateCashBonus(seat, activeCashBonusSession.getBalance(),
                    activeCashBonusSession.getBetSum());

            getLog().debug("processSitOut: after change, activeCashBonusSession={}",
                    activeCashBonusSession);
            persistCashBonusSession(activeCashBonusSession);
        }
        ISitOutCashBonusSessionResult sitOutCashBonusResult = socketService.sitOutCashBonusSession(
                seat.getAccountId(), seat.getNickname(), playerInfo.getSessionId(), playerInfo.getGameSessionId(),
                getGameType().getGameId(), seat.getTotalScore().getAmount(), activeCashBonusSession, playerInfo.getStats(),
                playerProfile, Collections.emptySet(), Collections.emptyMap(), playerBet, playerInfo.getExternalRoundId());
        IActiveCashBonusSession savedCashBonus = sitOutCashBonusResult.getCashBonus();
        String oldStatus = activeCashBonusSession.getStatus();
        if (savedCashBonus == null) {
            getLog().error("Bonus not found on GS side, process as cancelled, " +
                    "activeCashBonusSession={}", activeCashBonusSession);
            activeCashBonusSession.setStatus("CANCELLED");
        } else {
            activeCashBonusSession = savedCashBonus;
        }
        persistCashBonusSession(activeCashBonusSession);
        ILobbySession lobbySession = null;
        if (socketClient != null && socketClient.getSessionId() != null) {
            lobbySession = lobbySessionService.get(socketClient.getSessionId());
        }

        if (lobbySession != null) {
            lobbySession.setActiveCashBonusSession(activeCashBonusSession);
            lobbySessionService.add(lobbySession);
            getLog().debug("Cash Bonus lobbySession updated: {}", lobbySession);
        }

        String status = activeCashBonusSession.getStatus();
        //small fix for prevent excption on client side, RELEASING is unknown
        if ("RELEASING".equalsIgnoreCase(status)) {
            status = "RELEASED";
        }
        if (socketClient != null && !oldStatus.equalsIgnoreCase(activeCashBonusSession.getStatus())) {
            socketClient.sendMessage(getTOFactoryService().createBonusStatusChangedMessage(
                    activeCashBonusSession.getId(), oldStatus, status,
                    "", BonusType.CASHBONUS.name()));
        }

        checkResultAndFinishSitOut(sitOutCashBonusResult, seat, playerInfo, client, request, roundWin, ammoAmount,
                socketClient, activeCashBonusSession, serverId, seatNumber, oldSeatNumber);
    }

    protected void processSitOutTournamentSession(ITournamentSession tournamentSession, boolean bulletsConvertedToMoney,
                                                  Money correctedRoundWin, Money correctedReturnedBet,
                                                  SEAT seat, IPlayerProfile playerProfile, IPlayerBet playerBet,
                                                  IRoomPlayerInfo playerInfo, IGameSocketClient socketClient,
                                                  IGameSocketClient client,
                                                  ISitOut request, Money roundWin, int ammoAmount,
                                                  int serverId, int seatNumber, int oldSeatNumber) throws CommonException {
        if (tournamentSession.isActive()) {
            long remainingBalance = tournamentSession.getBalance();
            long balance = bulletsConvertedToMoney ? remainingBalance :
                    correctedRoundWin.toCents() + correctedReturnedBet.toCents() + remainingBalance;
            getLog().debug("processSitOut: before change, tournamentSession={}, " +
                            "new tournament balance={}, bulletsConvertedToMoney={}, remainingBalance={}",
                    tournamentSession, balance, bulletsConvertedToMoney, remainingBalance);
            tournamentSession.setBalance(balance > 0 ? balance : 0);
            updateTournamentSession(seat, tournamentSession.getBalance(), 0);

            getLog().debug("processSitOut: after change, tournamentSession={}",
                    tournamentSession);
            persistTournamentSession(tournamentSession);
        }
        ISitOutTournamentSessionResult sitOutTournamentResult = socketService.sitOutTournamentSession(seat.getAccountId(), seat.getNickname(),
                playerInfo.getSessionId(), playerInfo.getGameSessionId(), getGameType().getGameId(), seat.getTotalScore().getAmount(),
                tournamentSession, playerInfo.getStats(), playerProfile,
                Collections.emptySet(), Collections.emptyMap(), playerBet, playerInfo.getExternalRoundId());
        String oldState = tournamentSession.getState();
        //only state may be changed on GS side, primary source is MQ side
        ITournamentSession gsSideSession = sitOutTournamentResult.getTournamentSession();
        if (gsSideSession == null) {
            tournamentSession.setState("CANCELLED");
        } else {
            tournamentSession.setState(gsSideSession.getState());
        }
        persistTournamentSession(tournamentSession);
        ILobbySession lobbySession = null;
        if (socketClient != null && socketClient.getSessionId() != null)
            lobbySession = lobbySessionService.get(socketClient.getSessionId());

        if (lobbySession != null) {
            lobbySession.setTournamentSession(tournamentSession);
            //noinspection unchecked
            lobbySessionService.add(lobbySession);
            getLog().debug("Tournament lobbySession updated: {}", lobbySession);
        }
        if (socketClient != null && !oldState.equalsIgnoreCase(tournamentSession.getState())) {
            socketClient.sendMessage(getTOFactoryService().createTournamentStateChangedMessage(
                    tournamentSession.getTournamentId(), oldState, tournamentSession.getState(),
                    ""));
        }

        checkResultAndFinishSitOut(sitOutTournamentResult, seat, playerInfo, client, request, roundWin, ammoAmount,
                socketClient, null, serverId, seatNumber, oldSeatNumber);
    }

    protected void checkResultAndFinishSitOut(ISitOutResult sitOutResult, SEAT seat, IRoomPlayerInfo playerInfo, IGameSocketClient client,
                                              ISitOut request, Money roundWin, int ammoAmount, IGameSocketClient socketClient,
                                              IActiveCashBonusSession activeCashBonusSession,
                                              int serverId, int seatNumber, int oldSeatNumber) throws CommonException {
        boolean finishSitOut = true;
        if (sitOutResult != null && !sitOutResult.isSuccess()) {
            getLog().warn("External call to sitOut return error={}, seat={}", sitOutResult, seat);
            if (sitOutResult.getErrorCode() == TransactionErrorCodes.FOUND_PENDING_TRANSACTION) {
                getLog().error("processSitOut: failed, but pending transaction created, " +
                        "rollback not required");
                playerInfo.setPendingOperation(false);
                savePlayerInfo(playerInfo);
                if (client != null) {
                    client.sendMessage(getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION,
                            "Payment operation in progress", getCurrentTime(),
                            request != null ? request.getRid() : TObject.SERVER_RID));
                }
            } else { //process same as doOnError
                getLog().error("processSitOut: failed, rollback roundWin={} and ammoAmount={}",
                        roundWin, ammoAmount);
                seat.rollbackRoundWinAndAmmo(roundWin, ammoAmount);
                seat.setSitOutStarted(false);
                savePlayerInfo(playerInfo);
                finishSitOut = false;
                if (client != null) {
                    client.sendMessage(getTOFactoryService().createError(ErrorCodes.INTERNAL_ERROR,
                            "SitOut failed: errorCode=" + sitOutResult.getErrorCode(),
                            getCurrentTime(),
                            request != null ? request.getRid() : TObject.SERVER_RID));
                }
            }
        } else {
            playerInfo.setPendingOperation(false);
            savePlayerInfo(playerInfo);
        }
        if (finishSitOut) {
            long nextRoomId = -1;
            Long activeFRBonusId = null;

            if (activeCashBonusSession != null && !activeCashBonusSession.isActive()) {
                activeFRBonusId = ((ISitOutCashBonusSessionResult) sitOutResult).getActiveFRBonusId();
                IRoomInfo bestRoomInfo = getNextRoomId(seat, serverId, activeFRBonusId);
                if (bestRoomInfo != null) {
                    nextRoomId = bestRoomInfo.getId();
                }
                getLog().debug("processSitOut: Cash Bonus  is not active, nextRoomId for " +
                        "real mode={}, activeFRBonusId={}", nextRoomId, activeFRBonusId);
            }

            finishSitOut(request, seatNumber, seat, oldSeatNumber, nextRoomId, activeFRBonusId != null, false);
            long balance = getBalance(seat);
            getLog().debug("processSitOut: sendNotifyRoundCompleted sid={}, balance={}, xp={}",
                    playerInfo.getSessionId(), balance, seat.getTotalScore());
            int level = AchievementHelper.getPlayerLevel(seat.getTotalScore());
            executeOnAllMembers(lobbySessionService.createRoundCompletedNotifyTask(
                    playerInfo.getSessionId(), getId(), playerInfo.getId(),
                    balance,
                    playerInfo.getStats().getKillsCount(),
                    playerInfo.getStats().getTreasuresCount(),
                    playerInfo.getStats().getRounds(),
                    playerInfo.getTotalScore().getLongAmount(),
                    AchievementHelper.getXP(level),
                    AchievementHelper.getXP(level + 1),
                    level));
        }
        //noinspection unchecked
        getGameState().processSitOut(seat);
    }

    private void processSitOutFrbSession(IActiveFrbSession activeFrbSession, SEAT seat, int ammoAmount, IRoomPlayerInfo playerInfo,
                                         IGameSocketClient socketClient, long accountId, int serverId, ISitOut request,
                                         int seatNumber, int oldSeatNumber, IGameSocketClient client) {
        boolean sessionWasClosed = "CANCELLED".equalsIgnoreCase(activeFrbSession.getStatus())
                || "EXPIRED".equalsIgnoreCase(activeFrbSession.getStatus());

        long delta = seat.retrieveRoundWin().toCents();
        if (!sessionWasClosed) {
            activeFrbSession.setCurrentAmmoAmount(ammoAmount);
            activeFrbSession.incrementWinSum(delta);
        }

        getLog().info("processSitOut: frb mode, activeFrbSession={}, ammoAmount={}, sessionWasClosed: {}, win: {}",
                activeFrbSession, ammoAmount, sessionWasClosed, delta);

        long gameSessionId = playerInfo.getGameSessionId();
        IPlayerBet playerBet = seat.getCurrentPlayerRoundInfo()
                .getPlayerBet(playerInfo.createNewPlayerBet(), -1);
        String sessionId = socketClient != null ? socketClient.getSessionId() : playerInfo.getSessionId();

        boolean noActivity = isNoActivityInRound(seat, playerBet);

        if (!sessionWasClosed) {
            savePlayerBetForFRB(accountId, seat, playerInfo, serverId, gameSessionId, playerBet, sessionId, noActivity);
        }

        activeFrbSessionService.persist(activeFrbSession);

        ILobbySession lobbySession = lobbySessionService.get(sessionId);
        if (lobbySession != null) {
            IActiveFrbSession lobbySessionActiveFrbSession = lobbySession.getActiveFrbSession();
            lobbySessionActiveFrbSession.setCurrentAmmoAmount(getAmmoAmount(seat));
            lobbySessionActiveFrbSession.setWinSum(activeFrbSession.getWinSum());
        }

        if (ammoAmount <= 0 || sessionWasClosed) {
            try {

                List<Long> stakes = lobbySession != null ? lobbySession.getStakes() : Collections.emptyList();
                long gameId = roomInfo.getGameType().getGameId();
                clearCrossRoundSeatStats(seat);
                persistCrossRoundSeatStats(seat);

                IPlayerProfile playerProfile = playerProfileService.load(playerInfo.getBankId(), seat.getAccountId());

                socketService.sendMQDataSync(serverId, seat, activeFrbSession, playerProfile, gameId,
                        Collections.emptySet(), Collections.emptyMap());
                IFrbCloseResult frbCloseResult = socketService.closeFRBonusAndSession(serverId, accountId,
                        playerInfo.getSessionId(), playerInfo.getGameSessionId(),
                        getRoomInfo().getGameType().getGameId(), activeFrbSession.getBonusId(),
                        activeFrbSession.getWinSum());
                getLog().debug("processSitOut: success close activeFrbSession");

                boolean needMoveTorRealMode = !frbCloseResult.isHasNextFrb();

                IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
                lobbySession = setBalance(seat, frbCloseResult.getBalance());
                playerInfo.setPendingOperation(false);
                activeFrbSessionService.remove(activeFrbSession.getBonusId());
                playerInfoService.put(roomPlayerInfo);
                seat.setPlayerInfo(roomPlayerInfo);
                if (lobbySession != null) {
                    lobbySession.setActiveFrbSession(null);
                }

                sitOutFrbPlayer(request, seatNumber, seat, oldSeatNumber, needMoveTorRealMode, serverId,
                        stakes, frbCloseResult.isHasNextFrb());
            } catch (Exception e) {
                getLog().error("processSitOut: FRB mode failed", e);
                seat.setSitOutStarted(false);
                if (client != null) {
                    client.sendMessage(getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION,
                            "Close FRB failed", getCurrentTime(),
                            request != null ? request.getRid() : TObject.SERVER_RID));
                }
            }
        } else {
            sitOutFrbPlayer(request, seatNumber, seat, oldSeatNumber, false, serverId,
                    lobbySession != null ? lobbySession.getStakes() : Collections.emptyList(), false);
        }

        if (lobbySession != null) {
            lobbySessionService.add(lobbySession);
        }

        seat.setSitOutStarted(false);
    }

    protected IRoomInfo getNextRoomId(SEAT seat, int serverId, Long activeFRBonusId) {
        return activeFRBonusId == null ? roomInfoService.getBestRoomInfo(seat.getBankId(), serverId, MoneyType.REAL,
                getGameType(), seat.getStake(), seat.getPlayerInfo().getCurrency().getCode()) : null;
    }

    protected void savePlayerBetForFRB(long accountId, SEAT seat, IRoomPlayerInfo playerInfo, int serverId,
                                     long gameSessionId, IPlayerBet playerBet, String sessionId, boolean noActivity) {
        if (!noActivity) {
            long oldPlayerRoundId = seat.getCurrentPlayerRoundInfo().getPlayerRoundId();
            Boolean playerBetForFRBisSaved = socketService.savePlayerBetForFRB(serverId, sessionId, gameSessionId,
                    playerInfo.getExternalRoundId(), accountId, playerBet);
            if (playerBetForFRBisSaved) {
                getLog().debug("savePlayerBetForFRB: success in activeFrbSession," +
                                " reset seat player info data, old playerRoundInfo:{}, oldPlayerRoundId: {} ",
                        seat.getCurrentPlayerRoundInfo(), oldPlayerRoundId);
                seat.initCurrentRoundInfo(playerInfo);
                seat.getCurrentPlayerRoundInfo().setPlayerRoundId(oldPlayerRoundId);
                getLog().debug("savePlayerBetForFRB: new playerRoundInfo: {}", seat.getCurrentPlayerRoundInfo());
            } else {
                getLog().debug("savePlayerBetForFRB: error in activeFrbSession");
            }
        }
    }

    @Override
    public void updateStats() {
        MoneyType moneyType = getRoomInfo().getMoneyType();
        if (MoneyType.REAL.equals(moneyType) || MoneyType.FREE.equals(moneyType)) {
            for (SEAT seat : getSeats()) {
                seat.getPlayerInfo().getRoundStats().addRound();
                updateSeatStats(seat);
            }
        } else {
            for (SEAT seat : getSeats()) {
                updateSpecialModeQuests(seat);
            }
        }
    }

    private void updateSpecialModeQuests(SEAT seat) {
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        Set<IQuest> quests = playerInfo.getPlayerQuests().getQuests();
        updateQuestPlayerInfo(getId(), seat, quests);
        Long tournamentBonusId = seat.getSpecialModeId();
        if (playerQuestsService != null) {
            getLog().debug("updateSpecialModeQuests: aid={}, tournamentBonusId={}, quests={}",
                    seat.getAccountId(), tournamentBonusId, quests);
            playerQuestsService.updateSpecialModeQuests(tournamentBonusId, seat.getBankId(),
                    getGameType().getGameId(), seat.getAccountId(), quests, seat.getStake(),
                    roomInfo.getMoneyType().ordinal());
        }
    }

    @Override
    public void resetRoundResults() {
        for (SEAT seat : getSeats()) {
            resetSeatRoundResult(seat);
        }
    }

    protected void resetSeatRoundResult(SEAT seat) {
        if (seat.getPlayerInfo() == null) {
            getLog().warn("resetRoundResults: possible bug, found seat without playerInfo,. Seat={}", seat);
        } else {
            seat.resetCurrentScore();
        }
    }
    protected void lockWithoutPendingOperationWithWait(long accountId) throws CommonException {
        long timeoutTime = System.currentTimeMillis() + getLockTimeout();
        while (System.currentTimeMillis() < timeoutTime) {
            try {
                boolean locked = playerInfoService.tryLock(accountId, 3, TimeUnit.SECONDS);
                if (locked) {
                    IRoomPlayerInfo playerInfo = playerInfoService.get(accountId);
                    if (playerInfo == null) {
                        playerInfoService.unlock(accountId);
                        throw new CommonException("Cannot lock, playerInfo not found, may be already sitOut");
                    } else if (!playerInfo.isPendingOperation()) {
                        return;
                    }
                    playerInfoService.unlock(accountId);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new CommonException("Cannot lock, interrupted", e);
            }
        }
        getLog().error("Cannot lock without pending operation, playerInfo={}", playerInfoService.get(accountId));
        throw new CommonException("Cannot lock without pending operation");
    }

    public long getLockTimeout() {
        return MAX_LOCK_TIMEOUT;
    }

    @Override
    public void convertBulletsToMoney() {
        assertRoomStarted();
        long now = System.currentTimeMillis();
        Set<SEAT> seatsForProcess = new HashSet<>(getSeats());
        Set<SEAT> wantSitOutCandidates = new HashSet<>();
        CountDownLatch asyncCallLatch = new CountDownLatch(seatsForProcess.size());
        boolean isAllSeatsWithoutShoot = isAllSeatsWithoutShoot(seatsForProcess);
        for (SEAT seat : seatsForProcess) {
            convertBulletsToMoneyForSeat(seat, asyncCallLatch, wantSitOutCandidates, isAllSeatsWithoutShoot);
        }
        try {
            getLog().debug("convertBulletsToMoney: before asyncCallLatch.await()");
            asyncCallLatch.await();
            getLog().debug("convertBulletsToMoney: after asyncCallLatch.await()");
            for (SEAT sitOutCandidate : wantSitOutCandidates) {
                getLog().info("convertBulletsToMoney: sitOut disconnected seat={}", sitOutCandidate);
                processSitOut(sitOutCandidate.getSocketClient(), null, getSeatNumber(sitOutCandidate),
                        sitOutCandidate.getAccountId(), false, true);
            }
            StatisticsManager.getInstance().updateRequestStatistics("GameRoom::convertBulletsToMoney",
                    System.currentTimeMillis() - now, "" + getRoomInfo().getId() + ":" + getRoomInfo().getRoundId());
            getLog().debug("convertBulletsToMoney: all seats process");
        } catch (InterruptedException e) {
            getLog().error("Interrupted", e);
        }
    }

    protected boolean isAllSeatsWithoutShoot(Set<SEAT> seatsForProcess) {
        return true;
    }

    protected void convertBulletsToMoneyForSeat(SEAT seat, CountDownLatch asyncCallLatch, Set<SEAT> wantSitOutCandidates,
                                                boolean isAllSeatsWithoutShoot) {
        IGameSocketClient socketClient = seat.getSocketClient();
        long accountId = seat.getAccountId();
        getLog().debug("convertBulletsToMoneyForSeat: seat.getPlayerInfo(): {}", seat.getPlayerInfo());
        boolean locked = false;
        try {
            lockWithoutPendingOperationWithWait(accountId);
            locked = true;
            final IRoomPlayerInfo playerInfoFromService = playerInfoService.get(accountId);
            //may be already sitOut
            if (playerInfoFromService == null) {
                getLog().debug("convertBulletsToMoneyForSeat: after lock player not found in playerInfoService, " +
                        "accountId={}", accountId);
                playerInfoService.unlock(accountId);
                locked = false;
                asyncCallLatch.countDown();
                return;
            } else if (playerInfoFromService.getRoomId() != getRoomInfo().getId()) {
                getLog().debug("convertBulletsToMoneyForSeat: after lock player found playerInfo for other room, " +
                        "playerInfoFromService={}", playerInfoFromService);
                playerInfoService.unlock(accountId);
                locked = false;
                asyncCallLatch.countDown();
                return;
            }
            final IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            int seatNumber = getSeatNumber(seat);
            IActiveFrbSession activeFrbSession = playerInfo.getActiveFrbSession();
            IActiveCashBonusSession activeCashBonusSession = playerInfo.getActiveCashBonusSession();
            ITournamentSession tournamentSession = playerInfo.getTournamentSession();

            int serverId = socketClient != null ? socketClient.getServerId() :
                    IRoom.extractServerId(playerInfo.getSessionId());
            String sessionId = socketClient != null ? socketClient.getSessionId() : playerInfo.getSessionId();
            long gameSessionId = playerInfo.getGameSessionId();
            getLog().debug("convertBulletsToMoneyForSeat: seat={}", seat);

            IPlayerBet playerBet = seat.getCurrentPlayerRoundInfo().getPlayerBet(playerInfo.createNewPlayerBet(), -1);
            playerBet.setStartRoundTime(getGameState().getStartRoundTime());

            final int ammoAmount = getAmmoAmount(seat);

            boolean noActivity = isNoActivityInRound(seat, playerBet);

            if (noActivity) {
                getLog().debug("convertBulletsToMoneyForSeat: has no activity in round");
                playerBet.setData("");
            }
            IPlayerProfile playerProfile = playerProfileService.load(playerInfo.getBankId(), seat.getAccountId());
            if (activeFrbSession != null) {
                convertBulletsForFrbSeat(activeFrbSession, seat, accountId, playerInfo, serverId, gameSessionId,
                        sessionId, playerBet, noActivity, socketClient, playerProfile, wantSitOutCandidates, asyncCallLatch);
            } else {
                final Money roundWin = seat.retrieveRoundWin();
                final Money returnedBet = getReturnedBet(seat);

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
                    getLog().debug("convertBulletsToMoneyForSeat: found reBuyFromWin={}, need correcting amounts. roundWin={}, returnedBet={}, " +
                                    "correctedRoundWin={}, correctedReturnedBet={}",
                            seat.getRebuyFromWin().toCents(), roundWin.toCents(),
                            returnedBet.toCents(), correctedRoundWin.toCents(), correctedReturnedBet.toCents());
                }
                if (activeCashBonusSession != null) {
                    convertBulletsForCashBonusSeat(seat, activeCashBonusSession, correctedRoundWin, correctedReturnedBet,
                            playerProfile, playerBet, playerInfo, socketClient, accountId, wantSitOutCandidates,
                            sessionId, asyncCallLatch);
                } else if (tournamentSession != null) {
                    convertBulletsToMoneyForTournamentSeat(seat, tournamentSession, correctedRoundWin, correctedReturnedBet,
                            playerProfile, playerBet, playerInfo, socketClient, accountId, sessionId, asyncCallLatch);
                } else {
                    IBattlegroundRoundInfo bgRoundInfo = null;
                    if (playerInfo instanceof IBattlegroundRoomPlayerInfo) {
                        IBattlegroundRoomPlayerInfo castedPlayerInfo = (IBattlegroundRoomPlayerInfo) playerInfo;
                        bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();
                    }
                    socketService.addWin(serverId,
                                    sessionId,
                                    gameSessionId,
                                    correctedRoundWin,
                                    correctedReturnedBet,
                                    playerInfo.getExternalRoundId(),
                                    getRoomInfo().getId(),
                                    accountId,
                                    playerBet,
                                    bgRoundInfo)
                            .doOnSuccess(addWinResult -> handleAddWinResult(addWinResult, seat, socketClient, accountId,
                                    roundWin, ammoAmount, seatNumber, wantSitOutCandidates, sessionId,
                                    returnedBet, asyncCallLatch))
                            .doOnError(error -> handleAddWinError(error, seat, wantSitOutCandidates, roundWin,
                                    ammoAmount, returnedBet, accountId, socketClient, asyncCallLatch))
                            .subscribe();
                }
            }
        } catch (Exception e) {
            getLog().error("convertBulletsToMoneyForSeat failed for accountId={}", accountId, e);
            if (locked && playerInfoService.isLocked(accountId)) {
                getLog().error("convertBulletsToMoneyForSeat: found locked account={}", accountId);
                try {
                    playerInfoService.unlock(accountId);
                } catch (Exception e1) {
                    //may be already locked other thread
                    getLog().error("convertBulletsToMoneyForSeat: unlock error in catch (this may be normal)");
                }
            }
            asyncCallLatch.countDown();
            boolean playerInfoServiceLocked = playerInfoService.isLocked(accountId);
            getLog().debug("convertBulletsToMoneyForSeat: error , asyncCallLatch :{}, locked: {}, playerInfoServiceLocked: {} ",
                    asyncCallLatch, locked, playerInfoServiceLocked);
            if (playerInfoServiceLocked) {
                playerInfoService.forceUnlock(accountId);
                getLog().debug("convertBulletsToMoneyForSeat 11 forceUnlock : {}", accountId);
            }
        }
        addToWantSitOutCandidatesIfNeed(wantSitOutCandidates, seat);
    }

    protected void addToWantSitOutCandidatesIfNeed(Set<SEAT> wantSitOutCandidates, SEAT seat) {
        if (seat.isWantSitOut() || isSeatClientDisconnected(seat)) {
            IGameSocketClient gameSocketClient = seat.getSocketClient();
            getLog().debug("convertBulletsToMoneyForSeat: add to wantSitOutCandidates seat.isWantSitOut()={}, gameSocketClient={}",
                    seat.isWantSitOut(), gameSocketClient == null ? null : gameSocketClient.isDisconnected());
            wantSitOutCandidates.add(seat);
        }
    }

    protected boolean isSeatClientDisconnected(SEAT seat) {
        IGameSocketClient gameSocketClient = seat.getSocketClient();
        return gameSocketClient == null || gameSocketClient.isDisconnected();
    }

    protected boolean isNoActivityInRound(SEAT seat, IPlayerBet playerBet) {
        return playerBet.getBet() == 0 && playerBet.getWin() == 0 && seat.getRoundWin().equals(Money.ZERO);
    }

    protected void convertBulletsForFrbSeat(IActiveFrbSession activeFrbSession, SEAT seat, long accountId,
                                          IRoomPlayerInfo playerInfo, int serverId, long gameSessionId,
                                          String sessionId, IPlayerBet playerBet, boolean noActivity,
                                          IGameSocketClient socketClient, IPlayerProfile playerProfile,
                                          Set<SEAT> wantSitOutCandidates, CountDownLatch asyncCallLatch) throws Exception {
        boolean sessionWasClosed = "CANCELLED".equalsIgnoreCase(activeFrbSession.getStatus())
                || "EXPIRED".equalsIgnoreCase(activeFrbSession.getStatus());

        long seatRoundWin = seat.retrieveRoundWin().toCents();
        getLog().debug("convertBulletsToMoney, seatRoundWin: {}, activeFrbSession: {}, sessionWasClosed: {}",
                seatRoundWin, activeFrbSession, sessionWasClosed);
        if (!sessionWasClosed) {
            savePlayerBetForFRB(accountId, seat, playerInfo, serverId,
                    gameSessionId, playerBet, sessionId, noActivity);
            activeFrbSession.setCurrentAmmoAmount(getAmmoAmount(seat));
            activeFrbSession.incrementWinSum(seatRoundWin);
            seat.getPlayerInfo().setActiveFrbSession(activeFrbSession);
            activeFrbSessionService.persist(activeFrbSession);
            playerInfoService.put(playerInfo);
            getLog().debug("FRB activeFrbSession updated: {}", activeFrbSession);
        }

        ILobbySession lobbySession = null;
        if (socketClient != null && socketClient.getSessionId() != null) {
            lobbySession = lobbySessionService.get(socketClient.getSessionId());
        }

        if (lobbySession != null) {
            IActiveFrbSession lobbySessionActiveFrbSession = lobbySession.getActiveFrbSession();
            lobbySessionActiveFrbSession.setCurrentAmmoAmount(getAmmoAmount(seat));
            lobbySessionActiveFrbSession.setWinSum(activeFrbSession.getWinSum());
            lobbySessionService.add(lobbySession);
            getLog().debug("FRB lobbySession updated: {}", lobbySession);
        }

        socketService.sendMQDataSync(serverId, seat, activeFrbSession, playerProfile,
                roomInfo.getGameType().getGameId(), Collections.emptySet(), Collections.emptyMap());

        //need check for finalize FRB

        boolean achievedWinLimit = activeFrbSession.getMaxWinLimit() != -1 &&
                activeFrbSession.getWinSum() >= activeFrbSession.getMaxWinLimit();
        if ((activeFrbSession.getCurrentAmmoAmount() <= 0) || sessionWasClosed || achievedWinLimit) {
            getLog().debug("convertBulletsToMoney: found conditions for close FRB, seat={}, " +
                            "activeFrbSession={}, sessionWasClosed: {}, achievedWinLimit: {}",
                    seat, activeFrbSession, sessionWasClosed, achievedWinLimit);
            closeFRB(serverId, accountId, sessionId, gameSessionId, activeFrbSession, seat, socketClient,
                    playerInfo, wantSitOutCandidates, asyncCallLatch);
        } else {
            asyncCallLatch.countDown();
            playerInfoService.forceUnlock(accountId);
            getLog().debug("convertBulletsToMoney 3 forceUnlock : {}", accountId);
        }
    }

    protected void closeFRB(int serverId, long accountId, String sessionId, long gameSessionId,
                          IActiveFrbSession activeFrbSession, SEAT seat, IGameSocketClient socketClient,
                          IRoomPlayerInfo playerInfo, Set<SEAT> wantSitOutCandidates, CountDownLatch asyncCallLatch) {
        try {
            IFrbCloseResult frbCloseResult = socketService.closeFRBonusAndSession(serverId, accountId, sessionId, gameSessionId,
                    getRoomInfo().getGameType().getGameId(), activeFrbSession.getBonusId(),
                    activeFrbSession.getWinSum());
            //need change locker before put
            playerInfoService.forceUnlock(accountId);
            getLog().debug("convertBulletsToMoney 1 forceUnlock : {}", accountId);
            playerInfoService.lock(accountId);
            getLog().debug("convertBullet 1 HS lock: {}", accountId);
            getLog().debug("convertBulletsToMoney: success close activeFrbSession");
            activeFrbSessionService.remove(activeFrbSession.getBonusId());

            clearCrossRoundSeatStats(seat);
            persistCrossRoundSeatStats(seat);

            if (socketClient != null) {
                String closeReason = "Completed";
                long winSum = activeFrbSession.getWinSum();
                if (frbCloseResult.getErrorCode() > 0) {
                    winSum = 0;
                    closeReason = (frbCloseResult.getErrorCode() == 3 || frbCloseResult.getErrorCode() == 6)
                            ? "Cancelled" : "Expired";
                }

                IFRBEnded frbEnded = getTOFactoryService().createFRBEnded(getCurrentTime(),
                        winSum,
                        closeReason,
                        frbCloseResult.isHasNextFrb(),
                        frbCloseResult.getRealWinSum());

                socketClient.sendMessage(frbEnded);
                IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
                setBalance(seat, frbCloseResult.getBalance());
                createRoundCompletedTask(seat, sessionId, roomPlayerInfo, frbCloseResult.getBalance());
            }
            //sitOut later
            seat.setWantSitOut(true);
            wantSitOutCandidates.add(seat);
        } catch (Exception e) {
            if (socketClient != null) {
                socketClient.sendMessage(getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION,
                        "Close FRB failed", getCurrentTime(), TObject.SERVER_RID));
            }
        } finally {
            asyncCallLatch.countDown();
            playerInfoService.forceUnlock(accountId);
            getLog().debug("convertBulletsToMoney 2 forceUnlock : {}", accountId);
        }
    }

    protected void convertBulletsForCashBonusSeat(SEAT seat, IActiveCashBonusSession activeCashBonusSession,
                                                Money correctedRoundWin, Money correctedReturnedBet,
                                                IPlayerProfile playerProfile, IPlayerBet playerBet,
                                                IRoomPlayerInfo playerInfo, IGameSocketClient socketClient,
                                                long accountId, Set<SEAT> wantSitOutCandidates, String sessionId,
                                                CountDownLatch asyncCallLatch) throws CommonException {
        long balance = getBalance(seat);
        getLog().debug("convertBulletsToMoney: before change, activeCashBonusSession={}, " +
                "balance: {}", activeCashBonusSession, balance);
        activeCashBonusSession.setBalance(correctedRoundWin.toCents() + correctedReturnedBet.toCents()
                + balance);
        getLog().debug("convertBulletsToMoney: after change, activeCashBonusSession={}",
                activeCashBonusSession);
        persistCashBonusSession(activeCashBonusSession);

        String oldStatus = activeCashBonusSession.getStatus();

        if (activeCashBonusSession.isActive()) {
            IActiveCashBonusSession savedCashBonus = socketService.saveCashBonusRoundResult(
                    getGameType().getGameId(), seat, activeCashBonusSession, playerProfile,
                    Collections.emptySet(), Collections.emptyMap(), playerBet,
                    playerInfo.getExternalRoundId());
            persistCashBonusSession(savedCashBonus);
            activeCashBonusSession = savedCashBonus;
        }
        getLog().debug("convertBulletsToMoney: success, reset seat player info data, old playerRoundInfo: " +
                "{}", seat.getCurrentPlayerRoundInfo());
        seat.initCurrentRoundInfo(playerInfo);
        getLog().debug("convertBulletsToMoney: new playerRoundInfo: {}", seat.getCurrentPlayerRoundInfo());

        socketClient.sendMessage(getTOFactoryService().createBalanceUpdated(
                getCurrentTime(), activeCashBonusSession.getBalance(), getAmmoAmount(seat)));
        ILobbySession lobbySession = lobbySessionService.get(socketClient.getSessionId());
        //lobby session may be already removed
        if (lobbySession != null) {
            lobbySession.setActiveCashBonusSession(activeCashBonusSession);
            lobbySessionService.add(lobbySession);
        }
        //need change locker before put
        playerInfoService.forceUnlock(accountId);
        getLog().debug("convertBulletsToMoney 4 forceUnlock : {}", accountId);
        playerInfoService.lock(accountId);
        try {
            getLog().debug("convertBullet 2 HS lock: {}", accountId);
            IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
            roomPlayerInfo.setActiveCashBonusSession(activeCashBonusSession);
            roomPlayerInfo.finishCurrentRound();
            if (activeCashBonusSession.isActive()) {
                updateCashBonus(seat, activeCashBonusSession.getBalance(),
                        activeCashBonusSession.getBetSum());
            } else {
                activeCashBonusSessionService.remove(activeCashBonusSession.getId());
                //sitOut later
                seat.setWantSitOut(true);
                wantSitOutCandidates.add(seat);
            }
            if (!oldStatus.equalsIgnoreCase(activeCashBonusSession.getStatus())) {
                socketClient.sendMessage(getTOFactoryService().createBonusStatusChangedMessage(
                        activeCashBonusSession.getId(), oldStatus, activeCashBonusSession.getStatus(),
                        "", BonusType.CASHBONUS.name()));
            }
            playerInfoService.put(roomPlayerInfo);
            seat.setPlayerInfo(roomPlayerInfo);
            createRoundCompletedTask(seat, sessionId, roomPlayerInfo, activeCashBonusSession.getBalance());
            asyncCallLatch.countDown();
        } finally {
            playerInfoService.forceUnlock(accountId);
            getLog().debug("convertBulletsToMoney 5 forceUnlock : {}", accountId);
        }
    }

    private void convertBulletsToMoneyForTournamentSeat(SEAT seat, ITournamentSession tournamentSession,
                                                        Money correctedRoundWin, Money correctedReturnedBet,
                                                        IPlayerProfile playerProfile, IPlayerBet playerBet,
                                                        IRoomPlayerInfo playerInfo, IGameSocketClient socketClient,
                                                        long accountId, String sessionId,
                                                        CountDownLatch asyncCallLatch) throws CommonException {
        long balance = getBalance(seat);
        getLog().debug("convertBulletsToMoney: before change, tournament={}, balance={}",
                tournamentSession, balance);
        tournamentSession.setBalance(correctedRoundWin.toCents() + correctedReturnedBet.toCents()
                + balance);
        getLog().debug("convertBulletsToMoney: after change, tournament={}", tournamentSession);
        persistTournamentSession(tournamentSession);
        socketService.saveTournamentRoundResult(getGameType().getGameId(), seat, tournamentSession,
                playerProfile, Collections.emptySet(), Collections.emptyMap(), playerBet,
                playerInfo.getExternalRoundId());
        getLog().debug("convertBulletsToMoney: success, reset seat player info data, old playerRoundInfo: " +
                "{}", seat.getCurrentPlayerRoundInfo());
        seat.initCurrentRoundInfo(playerInfo);
        getLog().debug("convertBulletsToMoney: new playerRoundInfo: {}", seat.getCurrentPlayerRoundInfo());

        socketClient.sendMessage(getTOFactoryService().createBalanceUpdated(
                getCurrentTime(), tournamentSession.getBalance(), getAmmoAmount(seat)));
        ILobbySession lobbySession = lobbySessionService.get(seat.getPlayerInfo().getSessionId());
        if (lobbySession != null) {
            lobbySession.setTournamentSession(tournamentSession);
            lobbySessionService.add(lobbySession);
        }
        playerInfoService.forceUnlock(accountId);
        getLog().debug("convertBulletsToMoney 6 forceUnlock : {}", accountId);
        playerInfoService.lock(accountId);
        try {
            getLog().debug("convertBullet 3 HS lock: {}", accountId);
            IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
            roomPlayerInfo.setTournamentSession(tournamentSession);
            roomPlayerInfo.finishCurrentRound();
            playerInfoService.put(roomPlayerInfo);
            seat.setPlayerInfo(roomPlayerInfo);
            createRoundCompletedTask(seat, sessionId, roomPlayerInfo, tournamentSession.getBalance());
            asyncCallLatch.countDown();
        } finally {
            playerInfoService.forceUnlock(accountId);
            getLog().debug("convertBulletsToMoney 7 forceUnlock : {}", accountId);
        }
    }

    protected void handleAddWinResult(IAddWinResult addWinResult, SEAT seat, IGameSocketClient socketClient,
                                      long accountId, Money roundWin, int ammoAmount, int seatNumber, Set<SEAT> wantSitOutCandidates,
                                      String sessionId, Money returnedBet, CountDownLatch asyncCallLatch) {
        try {
            boolean processAsSuccess = true;
            if (!addWinResult.isSuccess()) {
                getLog().warn("External call to addWin return error={}, seat={}", addWinResult, seat);
                if (addWinResult.getErrorCode() == TransactionErrorCodes.FOUND_PENDING_TRANSACTION) {
                    getLog().error("processSitOut: failed, but found pending payment transaction, " +
                            "rollback not required");
                    if (socketClient != null) {
                        socketClient.sendMessage(getTOFactoryService().createError(
                                ErrorCodes.FOUND_PENDING_OPERATION,
                                "Payment operation in progress", getCurrentTime(),
                                TObject.SERVER_RID));
                    }
                } else {
                    processAsSuccess = false;
                    getLog().error("addWin: failed, rollback roundWin={} and ammoAmount={}",
                            roundWin, ammoAmount);
                    seat.rollbackRoundWinAndAmmo(roundWin, ammoAmount);
                    //need change locker before put
                    playerInfoService.forceUnlock(accountId);
                    getLog().debug("convertBulletsToMoney 8 forceUnlock : {}", accountId);
                    playerInfoService.lock(accountId);
                    getLog().debug("convertBullet 4 HS lock: {}", accountId);
                    IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
                    roomPlayerInfo.setPendingOperation(true, "addWin, roundWin=" + roundWin.toCents() +
                            ", ammoAmount=" + ammoAmount);

                    playerInfoService.put(roomPlayerInfo);

                    socketClient.sendMessage(getTOFactoryService().createError(
                            ErrorCodes.FOUND_PENDING_OPERATION,
                            "Send player win failed: errorCode=" + addWinResult.getErrorCode(),
                            getCurrentTime(), TObject.SERVER_RID));
                }
                setPendingWinForPlayer(seat, roundWin, ammoAmount, returnedBet.toCents());
            }
            if (processAsSuccess) {
                processSuccessAddWin(seat, accountId, addWinResult, socketClient, seatNumber, wantSitOutCandidates, sessionId, returnedBet);
            }
        } catch (Exception e) {
            getLog().error("Cannot post-process addWin for seat: {}", seat, e);
        } finally {
            if (asyncCallLatch != null) {
                asyncCallLatch.countDown();
            }
            //need force because lock owner is other thread
            playerInfoService.forceUnlock(accountId);
            getLog().debug("convertBulletsToMoney 10 forceUnlock : {}", accountId);
        }
    }

    public void setPendingWinForPlayer(SEAT seat, Money roundWin, int ammoAmount, long returnedBet) {
        getLog().warn("addWin: found pending for seat {}", seat);
        long accountId = seat.getAccountId();
        try {
            playerInfoService.lock(accountId);
            getLog().debug("setPendingWinForPlayer lock: {}", accountId);
            sendErrorMessage(seat, ErrorCodes.FOUND_PENDING_OPERATION, "Send player win is pending");
            getLog().debug("Found player with pending operation, accountId: {} need remove seat", seat.getAccountId());
            int seatNumber = getSeatNumber(seat);
            finishSitOut(null, seatNumber, seat, seatNumber, -1, false, false);
            gameState.firePlayersCountChanged();
        } catch (Exception e){
            getLog().error("setPendingWinForPlayer error: {}", accountId, e);
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("setPendingWinForPlayer unlock: {}", accountId);
        }
    }

    protected void sendErrorMessage(SEAT seat, int errorCode, String message) {
        IError errorMessage = getTOFactoryService().createError(errorCode, message, getCurrentTime(), TObject.SERVER_RID);
        if (!seat.isDisconnected()) {
            seat.sendMessage(errorMessage);
        } else {
            executeOnAllMembers(createSendSeatMessageTask(seat.getAccountId(), errorMessage));
        }
    }

    protected void processSuccessAddWin(SEAT seat, long accountId, IAddWinResult addWinResult, IGameSocketClient socketClient, int seatNumber,
                                        Set<SEAT> wantSitOutCandidates, String sessionId, Money returnedBet) {
        getLog().debug("addWin: success, reset seat player info data, old playerRoundInfo: " +
                "{}", seat.getCurrentPlayerRoundInfo());
        seat.initCurrentRoundInfo(seat.getPlayerInfo());
        getLog().debug("addWin: new playerRoundInfo: {}", seat.getCurrentPlayerRoundInfo());

        IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
        if (roomPlayerInfo == null) {
            getLog().error("convertBulletsToMoney: cannot load roomPlayerInfo from " +
                    "playerInfoService, accountId={}", accountId);
            //roomPlayerInfo = seat.getPlayerInfo();
            //getLog().error("convertBulletsToMoney: loaded roomPlayerInfo from seat");
        } else {
            roomPlayerInfo.finishCurrentRound();
            setBalance(seat, addWinResult.getBalance());
            if (addWinResult.isSuccess()) {
                roomPlayerInfo.setPendingOperation(false);
            }
            if (socketClient != null) {
                setBalance(seat, addWinResult.getBalance());
                try {
                    socketClient.sendMessage(getTOFactoryService().createBalanceUpdated(
                            getCurrentTime(), addWinResult.getBalance(), getAmmoAmount(seat)));
                } catch (Exception exc) {
                    getLog().error("Cannot send BalanceUpdated message", exc);
                }
            }
            getLog().debug("convertBulletsToMoney: after addWin, roomPlayerInfo={}, " +
                    "addWinResult={}", roomPlayerInfo, addWinResult);
            if (addWinResult.isPlayerOffline() || seat.isWantSitOut()) {
                getLog().warn("convertBulletsToMoney: seat '{}' is offline, " +
                        "need force sitOut", seatNumber);
                wantSitOutCandidates.add(seat);
            }
            //need change locker before put
            playerInfoService.forceUnlock(accountId);
            getLog().debug("convertBulletsToMoney 9 forceUnlock : {}", accountId);
            playerInfoService.lock(accountId);
            getLog().debug("convertBullet 5 HS lock: {}", accountId);
            playerInfoService.put(roomPlayerInfo);
            seat.setPlayerInfo(roomPlayerInfo);
            saveSeat(seatNumber, seat);
            createRoundCompletedTask(seat, sessionId, roomPlayerInfo, addWinResult.getBalance());
        }
    }

    protected void rollbackSeatWin(SEAT seat, Money roundWin, int ammoAmount, Money returnedBet) {
        seat.rollbackRoundWinAndAmmo(roundWin, ammoAmount);
    }

    protected void handleAddWinError(Throwable error, SEAT seat, Set<SEAT> wantSitOutCandidates,
                                     Money roundWin, int ammoAmount, Money returnedBet, long accountId,
                                     IGameSocketClient socketClient, CountDownLatch asyncCallLatch) {
        try {
            getLog().error("addWin: failed for seat: {}", seat, error);
            if (getRoomInfo().getMoneyType() == MoneyType.FREE) {
                getLog().info("addWin failed, but room is free mode. " +
                        "Ignore error and force sitOut");
                wantSitOutCandidates.add(seat);
            } else {
                rollbackSeatWin(seat, roundWin, ammoAmount, returnedBet);
                seat.rollbackRoundWinAndAmmo(roundWin, ammoAmount);
                //need change locker before put
                playerInfoService.forceUnlock(accountId);
                getLog().debug("convertBulletsToMoney 11 forceUnlock : {}", accountId);
                playerInfoService.lock(accountId);
                getLog().debug("convertBullet 6 HS lock: {}", accountId);
                IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
                roomPlayerInfo.setPendingOperation(true, "addWin, roundWin=" + roundWin.toCents() +
                        ", ammoAmount=" + ammoAmount);
                playerInfoService.put(roomPlayerInfo);
            }
        } finally {
            if (asyncCallLatch != null) {
                asyncCallLatch.countDown();
            }
            //need force because lock owner is other thread
            playerInfoService.forceUnlock(accountId);
            getLog().debug("convertBulletsToMoney 12 forceUnlock : {}", accountId);
            getLog().debug("ConvertBullets 2 HS forceUnlock: {}", accountId);
        }
        if (socketClient != null) {
            socketClient.sendMessage(getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION,
                    "Send player win failed", getCurrentTime(), TObject.SERVER_RID));
        }
    }

    protected void addSitOutCandidatesIfFoundAnyPendingOperation(Set<SEAT> wantSitOutCandidates) {
        boolean hasPendingOperation = false;
        for (SEAT seat : getAllSeats()) {
            if (seat != null && seat.getPlayerInfo() != null && seat.getPlayerInfo().isPendingOperation()) {
                hasPendingOperation = true;
                break;
            }
        }
        if (hasPendingOperation) {
            for (SEAT seat : getAllSeats()) {
                if (seat != null && seat.getPlayerInfo() != null && !seat.getPlayerInfo().isPendingOperation() &&
                        !wantSitOutCandidates.contains(seat)) {
                    wantSitOutCandidates.add(seat);
                    getLog().warn("addSitOutCandidatesIfFoundAnyPendingOperation: found pending operation, " +
                            "need sitout: {}", seat);
                }
            }
        }
    }

    @Override
    public void persistCashBonusSession(IActiveCashBonusSession session) {
        activeCashBonusSessionService.persist(session);
        getLog().debug("persistCashBonusSession: {}", session);
    }

    @Override
    public void persistTournamentSession(ITournamentSession session) {
        tournamentService.persist(session);
        getLog().debug("persistTournamentSession: {}", session);
    }

    @Override
    public long getBalance(SEAT seat) {
        String sessionId = seat.getPlayerInfo().getSessionId();
        ILobbySession lobbySession = lobbySessionService == null ? null :
                lobbySessionService.get(sessionId);
        if (lobbySession == null) {
            getLog().error("getBalance: LobbySession not found for seat={}, try get from seat", seat);
            IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            if (playerInfo.getActiveCashBonusSession() != null) {
                getLog().info("getBalance: activeCashBonusSession found for seat={}, session={}", seat,
                        playerInfo.getActiveCashBonusSession());
                return playerInfo.getActiveCashBonusSession().getBalance();
            } else if (playerInfo.getTournamentSession() != null) {
                getLog().info("getBalance: tournamentSession found for seat={}, session={}", seat,
                        playerInfo.getTournamentSession());
                return playerInfo.getTournamentSession().getBalance();
            } else {
                getLog().error("getBalance: activeCashBonusSession  or tournamentSession not found " +
                        "for seat={}, return 0;", seat);
                return 0;
            }
        } else {
            return lobbySession.getBalance();
        }
    }

    @Override
    public ILobbySession setBalance(SEAT seat, long balance) {
        String sessionId = seat.getPlayerInfo().getSessionId();
        ILobbySession lobbySession = lobbySessionService == null ? null :
                lobbySessionService.get(sessionId);
        if (lobbySession != null) {
            getLog().debug("setBalance: sessionId={}, balance={}", sessionId, balance);
            lobbySession.setBalance(balance);
            return lobbySessionService.add(lobbySession);
        }
        return lobbySession;
    }

    @Override
    public ILobbySession updateCashBonus(SEAT seat, long balance, long betSum) {
        String sessionId = seat.getPlayerInfo().getSessionId();
        ILobbySession lobbySession = lobbySessionService == null ? null :
                lobbySessionService.get(sessionId);
        if (lobbySession != null) {
            getLog().debug("updateCashBonus: sessionId={}, balance={}, betSum={}", sessionId, balance, betSum);
            lobbySession.setBalance(balance);
            IActiveCashBonusSession cashBonusSession = lobbySession.getActiveCashBonusSession();
            if (cashBonusSession != null) {
                cashBonusSession.setBetSum(betSum);
            }
            return lobbySessionService.add(lobbySession);
        }
        return lobbySession;
    }

    @Override
    public ILobbySession updateTournamentSession(SEAT seat, long balance, long betSum) {
        String sessionId = seat.getPlayerInfo().getSessionId();
        ILobbySession lobbySession = lobbySessionService == null ? null :
                lobbySessionService.get(sessionId);
        if (lobbySession != null) {
            getLog().debug("updateTournamentSession: sessionId={}, balance={}, betSum={}", sessionId, balance, betSum);
            lobbySession.setBalance(balance);
            ITournamentSession tournamentSessionSession = lobbySession.getTournamentSession();
            if (tournamentSessionSession != null) {
                //tournamentSessionSession.setBetSum(betSum);
            }
            return lobbySessionService.add(lobbySession);
        }
        return lobbySession;
    }

    protected void createRoundCompletedTask(SEAT seat, String sessionId, IRoomPlayerInfo roomPlayerInfo, long balance) {
        executeOnAllMembers(lobbySessionService.createRoundCompletedNotifyTask(
                sessionId, getId(), roomPlayerInfo.getId(),
                balance,
                roomPlayerInfo.getStats().getKillsCount(),
                roomPlayerInfo.getStats().getTreasuresCount(),
                roomPlayerInfo.getStats().getRounds(),
                roomPlayerInfo.getStats().getScore().getLongAmount(),
                AchievementHelper.getXP(seat.getLevel()),
                AchievementHelper.getXP(seat.getLevel() + 1), seat.getLevel()));
    }

    protected int getMaxObservers() {
        return MAX_OBSERVERS;
    }

    @Override
    public ITransportObject processOpenRoom(IGameSocketClient client, IOpenRoom request, String currency)
            throws CommonException {

        boolean isSameBattleObserver = getGameType().isBattleGroundGame() &&
                observePlayers.containsKey(client.getAccountId());

        getLog().debug("processOpenRoom {}, {}: client={}, request={}, getMaxObservers(): {}, observePlayers.size(): {}, isSameBattleObserver: {}",
                client.getNickname(), client.getSessionId(), client, request, getMaxObservers(), observePlayers.size() , isSameBattleObserver);

        this.checkAndStartRoom();

        this.removeDisconnectedObservers();

        if (client.isPrivateRoom() && observePlayers.size() >= getMaxObservers() && !isSameBattleObserver) {

            getLog().warn("processOpenRoom {}, {}: room is private, observePlayers.size() >= getMaxObservers() is true, getMaxObservers(): {}, observePlayers.size(): {}, isSameBattleObserver: {}",
                    client.getNickname(), client.getSessionId(), getMaxObservers(), observePlayers.size() , isSameBattleObserver);

            observePlayers.forEach(
                    (aLong, iGameSocketClient) ->
                            getLog().debug("processOpenRoom {}, {}: observer accountId: {}, iGameSocketClient.getSeatNumber(): {} ",
                                    client.getNickname(), client.getSessionId(), aLong, iGameSocketClient.getSeatNumber())
            );

            IError error = getTOFactoryService().createError(ErrorCodes.TOO_MANY_OBSERVERS, "Too many observers",
                            getCurrentTime(), request.getRid());
            return error;
        }

        if (observePlayers.size() >= getMaxObservers() && !isSameBattleObserver) {

            getLog().warn("processOpenRoom {}, {}: observePlayers.size() >= getMaxObservers() is true, getMaxObservers(): {}, observePlayers.size(): {}, isSameBattleObserver: {}",
                    client.getNickname(), client.getSessionId(), getMaxObservers(), observePlayers.size() , isSameBattleObserver);

            observePlayers.forEach(
                    (aLong, iGameSocketClient) ->
                            getLog().debug("processOpenRoom {}, {}: observer accountId: {}, iGameSocketClient.getSeatNumber(): {} ",
                                    client.getNickname(), client.getSessionId(), aLong, iGameSocketClient.getSeatNumber())
            );

            IError error = getTOFactoryService().createError(ErrorCodes.TOO_MANY_OBSERVERS, "Too many observers",
                    getCurrentTime(), request.getRid());
            return error;
        }

        boolean isOnePlayerGame = getRoomInfo().getMaxSeats() == 1;

        if (isOnePlayerGame) {

            for (IGameSocketClient observerClient : observePlayers.values()) {

                if (observerClient != null && observerClient.getAccountId() != null &&
                        !observerClient.getAccountId().equals(client.getAccountId())) {

                    getLog().warn("processOpenRoom {}, {}: only one observer allowed for singlePlayer mode. observer={}, " +
                            "newPlayer={}", client.getNickname(), client.getSessionId(), observerClient.getAccountId(), client.getAccountId());

                    IError error = getTOFactoryService().createError(ErrorCodes.TOO_MANY_OBSERVERS, "Too many observers",
                            getCurrentTime(), request.getRid());

                    return error;
                }
            }

            SEAT seater = this.getAllSeats().get(0);

            if (seater != null && seater.getAccountId() != client.getAccountId()) {

                getLog().warn("processOpenRoom {}, {}: cannot add observer for singlePlayer mode. room is occupied={}, ",
                        client.getNickname(), client.getSessionId(), seater);

                IError error = getTOFactoryService().createError(ErrorCodes.TOO_MANY_OBSERVERS, "Too many observers",
                        getCurrentTime(), request.getRid());

                return error;
            }
        }

        observePlayers.put(client.getAccountId(), client);

        for (SEAT seat : this.getAllSeats()) {
            if (seat != null && seat.getAccountId() == client.getAccountId()) {
                seat.setSocketClient(client);
            }
        }

        IGetRoomInfoResponse roomInfoResponse = this.getRoomInfoResponse(request.getRid(), client, currency);

        for (IRoomOpenedListener listener : roomOpenedListeners) {
            listener.notifyRoomOpened(this, client);
        }

        return roomInfoResponse;
    }

    @Override
    public void processCloseRoom(IGameSocketClient client, ICloseRoom request) throws CommonException {
        lock();
        try {
            getLog().debug("processCloseRoom: client={}, CloseRoomRequest={}", client, request);
            assertRoomStarted();
            removeObserverByAccountId(client.getAccountId());
            client.sendMessage(getTOFactoryService().getOkResponse(getCurrentTime(), request.getRid()));
            removeAllEnemiesIfEmptyRoom();
            shutdownRoomIfEmpty();

            for (IRoomClosedListener listener : roomClosedListeners) {
                listener.notifyRoomClosed(this, client);
            }

        } finally {
            unlock();
        }
    }

    @Override
    public void processCloseRoom(long accountId) throws CommonException {
        IGameSocketClient client = this.getObserver(accountId);
        lock();
        try {
            getLog().debug("processCloseRoom: accountId={}", accountId);
            assertRoomStarted();
            removeObserverByAccountId(accountId);
            removeAllEnemiesIfEmptyRoom();
            shutdownRoomIfEmpty();

            if(client != null) {
                for(IRoomClosedListener listener: roomClosedListeners) {
                    listener.notifyRoomClosed(this, client);
                }
            }

        } finally {
            unlock();
        }
    }

    private void removeAllEnemiesIfEmptyRoom() {
        if (roomInfo.getState().equals(RoomState.PLAY) && allSeatsAreFree() && observePlayers.isEmpty()) {
            try {
                map.removeAllEnemies();
                ((AbstractPlayGameState) gameState).doFinishWithLock();
            } catch (Exception e) {
                getLog().error("Failed to close empty room", e);
            }
        }
    }

    @Override
    public void removeDisconnectedObservers() {
        for (Map.Entry<Long, IGameSocketClient> client : observePlayers.entrySet()) {
            if (client.getValue().isDisconnected()) {
                getLog().debug("removeDisconnectedObservers: remove socketClient with accountId={}", client.getKey());
                removeObserverByAccountId(client.getKey());
            }
        }
    }

    @SuppressWarnings("unused")
    protected boolean isBossImmortal(IGameSocketClient client) {
        return false;
    }

    public void sendChangesToSeats(TObject message) {
        for (SEAT seat : getAllSeats()) {
            if (isRoomStarted()) {
                if (message instanceof IServerMessage) {
                    seat.sendMessage(message);
                } else if (seat != null) {
                    //todo: implement clone and send logic
                    getLog().warn("Found personal seat message, need clone object and customize cloned before send: {}", message);
                }
            }
        }
    }

    public void sendNewEnemyMessage(ENEMY enemy) {
        sendChanges(getTOFactoryService().createNewEnemy(getCurrentTime(), convert(enemy, true)));
    }

    public void sendNewEnemiesMessage(List<ENEMY> enemies) {
        if (!enemies.isEmpty()) {
            sendChanges(convertNewEnemies(getCurrentTime(), enemies));
        }
    }

    public INewEnemies convertNewEnemies(long time, List<ENEMY> enemies) {
        List<IRoomEnemy> roomEnemies = new ArrayList<>();
        for (ENEMY enemy : enemies) {
            roomEnemies.add(convert(enemy, true));
        }
        return getTOFactoryService().createNewEnemies(time, roomEnemies);
    }

    @Override
    public void sendMessageToPlayer(ITransportObject message, long seatAccountId) {
        for (Map.Entry<Long, IGameSocketClient> entry : observePlayers.entrySet()) {
            IGameSocketClient client = entry.getValue();
            if (client.getAccountId() == seatAccountId) {
                client.sendMessage(message);
            }
        }
    }

    @Override
    public void sendChanges(ITransportObject message) {
        for (Map.Entry<Long, IGameSocketClient> entry : observePlayers.entrySet()) {
            IGameSocketClient client = entry.getValue();
            client.sendMessage(message);
        }
    }

    @Override
    public void sendChangesToObserversOnly(ITransportObject message) {
        for (Map.Entry<Long, IGameSocketClient> entry : observePlayers.entrySet()) {
            Long accountId = entry.getKey();
            IGameSocketClient client = entry.getValue();
            if (getSeatByAccountId(accountId) == null) {
                getLog().debug("accountId {} is observer, sent message {}", accountId, message);
                client.sendMessage(message);
            }
        }
    }

    @Override
    public void sendChanges(ITransportObject allMessage, ITransportObject seatMessage, long seatAccountId, InboundObject inboundMessage) {
        for (Map.Entry<Long, IGameSocketClient> entry : observePlayers.entrySet()) {
            long accountId = entry.getKey();
            IGameSocketClient client = entry.getValue();
            if (accountId == seatAccountId) {
                if (inboundMessage != null) {
                    client.sendMessage(seatMessage, inboundMessage);
                } else {
                    client.sendMessage(seatMessage);
                }
            } else if (allMessage != null) {
                client.sendMessage(allMessage);
            }
        }
    }

    public void setDefaultTimeMillis(int defaultTimeMillis) {
        this.defaultTimeMillis = defaultTimeMillis;
    }

    public void finish(boolean forceFinishRound) {
        getLog().warn("Forced round finish by debug request");
        if (gameState.getRoomState().equals(RoomState.PLAY)) {
            AbstractPlayGameState gameState = (AbstractPlayGameState) this.gameState;
            gameState.onTimerWithLock(true);
            gameState.setNeedForceFinishRound(forceFinishRound);
        }
    }

    @Override
    public int getMapId() {
        return getMap().getId();
    }

    @Override
    public int getNextMapId() {
        return nextMapId;
    }

    protected List<IRoomEnemy> getLiveRoomEnemies() {
        ReentrantLock lockEnemy = getMap().getLockEnemy();
        lockEnemy.lock();
        try {
            Collection<ENEMY> enemies = getMap().getItems();
            List<IRoomEnemy> result = new ArrayList<>(enemies.size());
            for (ENEMY enemy : enemies) {
                result.add(convert(enemy, true));
            }
            return result;
        } finally {
            lockEnemy.unlock();
        }
    }

    protected boolean allSeatsAreFree() {
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                return false;
            }
        }
        return true;
    }

    protected void sendSitOutMessage(SEAT seat, ISitOut request, int oldSeatNumber, long nextRoomId, boolean hasNextFrb, boolean frbSitOut) {
        sendChanges(getTOFactoryService().createSitOutResponse(getCurrentTime(), TObject.SERVER_RID, oldSeatNumber,
                        seat.getNickname(), getCurrentTime(), 0,
                        0, 0, nextRoomId, hasNextFrb),
                getTOFactoryService().createSitOutResponse(getCurrentTime(),
                        request != null ? request.getRid() : TObject.SERVER_RID,
                        oldSeatNumber, seat.getNickname(), getCurrentTime(), 0,
                        0, 0, nextRoomId, hasNextFrb), seat.getAccountId(), request
        );
    }

    private void sitOutFrbPlayer(ISitOut request, int seatNumber, SEAT seat, int oldSeatNumber,
                                 boolean needMoveToRealMode, int serverId, List<Long> stakes, boolean hasNextFrb) {

        long nextRoomId = -1;
        Long stakeFRB = seat.getStake().toCents();
        if (needMoveToRealMode && stakes.contains(stakeFRB)) {
            IRoomInfo bestRoomInfo = getNextRoomId(seat, serverId, null);
            if (bestRoomInfo != null) {
                nextRoomId = bestRoomInfo.getId();
            }
            getLog().debug("processSitOut: FRB Bonus, nextRoomId for real mode: {}", nextRoomId);
        }

        finishSitOut(request, seatNumber, seat, oldSeatNumber, nextRoomId, hasNextFrb, true);
        seat.setSitOutStarted(false);
        try {
            getGameState().processSitOut(seat);
        } catch (CommonException e) {
            //impossible, but need log
            getLog().error("Failed sitOut error", e);
        }
    }

    protected void finishSitOut(ISitOut request, int seatNumber, SEAT seat, int oldSeatNumber, long nextRoomId,
                                boolean hasNextFrb, boolean frbSitOut) {
        getLog().debug("finishSitOut: request={}, seatNumber={}, seat={}, oldSeatNumber={}, " +
                        "hasNextFrb={}, frbSitOut={}", request, seatNumber, seat, oldSeatNumber, hasNextFrb, frbSitOut);
        seat.setSitOutStarted(false);
        removeSeat(seatNumber, seat);
        setSeatNumber(seat, -1);
        IGameSocketClient socketClient = seat.getSocketClient();
        if (socketClient != null) {
            socketClient.setSeatNumber(-1);
        }
        playerInfoService.remove(roomInfoService, getRoomInfo().getId(), seat.getAccountId());
        sendSitOutMessage(seat, request, oldSeatNumber, nextRoomId, hasNextFrb, frbSitOut);

        if (seat.isBot()) {
            getLog().debug("processSitOut: remove bot from lobbySessionService sid={}, accountId: {}", seat.getSocketClient().getSessionId(),
                    seat.getAccountId());
            lobbySessionService.remove(seat.getSocketClient().getSessionId());
        }

        for (ISeatsCountChangedListener listener : seatsCountChangedListeners) {
            listener.notifySeatRemoved(this, seat);
        }

        if (getRoomInfo().getMaxSeats() == 1 && getAllSeats().get(0) == null) {
            try {
                finish(false);
            } catch (Exception e) {
                getLog().error("Cannot finish round", e);
            }
        }
    }

    protected void savePlayerInfo(IRoomPlayerInfo player) {
        playerInfoService.lock(player.getId());
        getLog().debug("savePlayerInfo lock: {}", player.getId());
        try {
            playerInfoService.put(player);
        } finally {
            playerInfoService.unlock(player.getId());
            getLog().debug("savePlayerInfo unlock: {}", player.getId());
        }
    }

    private void updateSeatStats(SEAT seat) {
        assertRoomStarted();
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        if (playerInfo == null) {
            getLog().warn("updateSeatStats: playerInfo not found for seat: {}", seat);
            playerInfo = playerInfoService.get(seat.getAccountId());
            if (playerInfo == null) {
                getLog().error("updateSeatStats: playerInfo not found in playerInfoService, cannot update. Exit. " +
                        "Please fix");
                return;
            } else {
                if (playerInfo.getId() != seat.getAccountId()) {
                    getLog().error("updateSeatStats: Very strange error, seat and playerInfo is not same account. " +
                            "playerInfo={}", playerInfo);
                    return;
                }
                seat.setPlayerInfo(playerInfo);
            }
        }
        getLog().debug("updateSeatStats for seat: {}", seat);
        playerInfoService.lock(seat.getAccountId());
        getLog().debug("updateSeatStats lock: {}", seat.getAccountId());
        try {
            persistCrossRoundSeatStats(seat);

            long gameId = getGameType().getGameId();
            IPlayerStats roundStats = playerInfo.getRoundStats();
            playerInfo.setPrevXP(playerInfo.getTotalScore());
            Set<IQuest> quests = playerInfo.getPlayerQuests().getQuests();
            updateQuestPlayerInfo(getId(), seat, quests);
            if (getRoomInfo().getMoneyType() == MoneyType.REAL) {
                getLog().debug("Adding stats for {}: {}", seat.getAccountId(), roundStats);
                playerInfo.setStats(playerStatsService.addStats(seat.getBankId(), gameId, seat.getAccountId(), roundStats));
                if (playerQuestsService != null) {
                    getLog().debug("aid: {}  save quests: {}", seat.getAccountId(), quests);
                    playerQuestsService.updateQuests(seat.getBankId(), gameId, seat.getAccountId(), quests, seat.getStake(),
                            roomInfo.getMoneyType().ordinal());
                }
                IRoomPlayerInfo playerInfoFromService = playerInfoService.get(seat.getAccountId());
                if (playerInfoFromService != null) {
                    boolean isPending = playerInfoFromService.isPendingOperation();
                    playerInfo.setPendingOperation(isPending);
                }
                playerInfoService.put(playerInfo);
            } else {
                //Player stats for not real mode is temporary, but also need update
                IPlayerStats playerStats = playerInfo.getStats();
                if (playerStats == null) {
                    playerStats = playerInfo.setNewPlayerStats();
                }
                playerStats.addRoundStats(roundStats);
                Map<Integer, Long> kills = playerStats.getKills();
                for (Map.Entry<Integer, Long> entry : roundStats.getKills().entrySet()) {
                    kills.put(entry.getKey(), kills.getOrDefault(entry.getKey(), 0L) + entry.getValue());
                }
                Map<Integer, Long> treasures = playerStats.getTreasures();
                for (Map.Entry<Integer, Long> entry : roundStats.getTreasures().entrySet()) {
                    treasures.put(entry.getKey(), treasures.getOrDefault(entry.getKey(), 0L) + entry.getValue());
                }
            }
        } finally {
            playerInfoService.unlock(seat.getAccountId());
            getLog().debug("updateSeatStats unlock: {}", seat.getAccountId());
        }
    }

    public void updateXPStats(long xp, SEAT seat) {
        IPlayerStats diff = getTOFactoryService().createPlayerStats();
        diff.addScore(xp);
        getLog().debug("Adding stats for {}: {}", seat.getAccountId(), diff);
        playerStatsService.addStats(seat.getBankId(), getGameType().getGameId(), seat.getAccountId(), diff);
    }

    private void updateQuestPlayerInfo(long roomId, SEAT seat, Set<IQuest> quests) {
        playerInfoService.lock(seat.getAccountId());
        getLog().debug("updateQuestPlayerInfo lock: {}", seat.getAccountId());
        try {
            IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(seat.getAccountId());
            if (roomPlayerInfo != null) {
                roomPlayerInfo.getPlayerQuests().setQuests(quests);
                playerInfoService.put(roomPlayerInfo);
            } else {
                getLog().warn("updateQuestPlayerInfo: roomPlayerInfo not found for roomId={}, seatNumber={}",
                        roomId, getSeatNumber(seat));
                //playerInfoService.remove(roomId, seat.getAccountId());
            }
        } finally {
            playerInfoService.unlock(seat.getAccountId());
            getLog().debug("updateQuestPlayerInfo unlock: {}", seat.getAccountId());
        }
    }

    @Override
    public void restartTimer() throws CommonException {
        timer.restart();
    }

    @Override
    public void startTimer() throws CommonException {
        timer.startTimer();
    }

    @Override
    public void stopTimer() throws CommonException {
        timer.stopTimer();
        stopUpdateTimer();
    }

    @Override
    public void startUpdateTimer() {
        ReentrantLock lockEnemy = getMap().getLockEnemy();
        if (lockEnemy != null) {
            boolean locked = lockEnemy.isLocked();
            getLog().debug("startUpdateTimer: lockEnemy state {}", locked);
            if (locked) {
                if (!lockEnemy.isHeldByCurrentThread()) {
                    getLog().warn("Locked by other thread, need try change lock owner");
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        lockEnemy.tryLock(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        getLog().error("Cannot lock", e);
                    }
                }
                //if lock not success, this unlock fail with IllegalMonitorStateException
                lockEnemy.unlock();
            }
        }
        if (updateTimer != null) {
            getLog().debug("startUpdateTimer: need stop stopUpdateTimer");
            stopUpdateTimer();
        }
        updateRoom();
        checkAndStartTimer();
    }

    protected void checkAndStartTimer() {
        if (updateTimer == null) {
            lock();
            try {
                if (updateTimer == null) {
                    updateTimer = Flux.interval(Duration.ofMillis(defaultTimeMillis), getScheduler())
                            .subscribe(i -> updateRoom(), error -> getLog().warn("updateTimer error ", error));
                    getLog().debug("startUpdateTimer: subscribe, defaultTimeMillis: {}", defaultTimeMillis);
                }
            } finally {
                unlock();
            }
        }
    }

    @Override
    public void stopUpdateTimer() {
        getLog().debug("stopUpdateTimer, updateTimer is null={}", updateTimer == null);
        if (updateTimer != null) {
            updateTimer.dispose();
            updateTimer = null;
        }
    }

    @Override
    public boolean isTimerStopped() {
        return timer == null || timer.isStopped();
    }

    @Override
    public void setTimerTime(long time) throws CommonException {
        timer.setTime(time);
    }

    @Override
    public long getTimerTime() {
        return timer.getTime();
    }

    @Override
    public long getTimerElapsed() {
        return timer.getElapsed();
    }

    @Override
    public long getTimerRemaining() {
        return timer.getTime() - timer.getElapsed();
    }

    @Override
    public void setGameState(IGameState newState) throws CommonException {
        timer.stopTimer();
        RoomState oldRoomState = this.gameState.getRoomState();
        getLog().debug("setGameState: old={}, new={}", oldRoomState, newState);
        initGameState(newState);
        long timeToNextState = newState.getTimeToNextState();
        long roundId = getRoomInfo().getRoundId();
        notifyOnGameStateChanged(newState, oldRoomState, timeToNextState, roundId);
    }

    protected void initGameState(IGameState newState) throws CommonException {
        this.gameState = newState;
        newState.init();
    }

    protected void notifyOnGameStateChanged(IGameState newState,
                                            RoomState oldRoomState,
                                            long timeToNextState,
                                            long roundId) {
        sendChanges(getTOFactoryService().createGameStateChanged(getCurrentTime(), newState.getRoomState(),
                timeToNextState, roundId, getRoundStartTime(newState)));
        for (IRoomStateChangedListener listener : stateChangedListeners) {
            listener.notifyStateChanged(this, oldRoomState, newState.getRoomState());
        }
    }

    protected Long getRoundStartTime(IGameState gameState) {
        lastRoundStartTime = gameState.getRoomState() == RoomState.PLAY ? gameState.getStartRoundTime() : null;
        return lastRoundStartTime;
    }

    public Long getLastRoundStartTime() {
        return lastRoundStartTime;
    }

    public void occurGameTimer(IGameState gameState) throws CommonException {
        if (gameState != null) {
            gameState.onTimer(false);
        }
    }

    public void updateRoom() {
        getLog().debug("updateRoom()");
        assertRoomStarted();
        try {
            gameState.update();
        } catch (Exception e) {
            getLog().debug("updateRoom unexpected error", e);
        }
    }

    @Override
    public void updateRoomInfo(IRoomInfoUpdater updater) {
        if (roomInfoService == null) {
            return;
        }
        long roomId = roomInfo.getId();
        roomInfoService.lock(roomId);
        try {
            reloadRoomInfo();
            updater.update(roomInfo);
            roomInfoService.update(roomInfo);
        } finally {
            roomInfoService.unlock(roomId);
        }
    }
    @Override
    public void reloadRoomInfo() {
        roomInfo = (ROOM_INFO) roomInfoService.getRoom(roomInfo.getId());
    }

    @Override
    public long getCurrentTime() {
        //todo: may be from SynchroTimeProvider ?
        return System.currentTimeMillis();
    }

    public int getTimeToNextState() {
        return 0;
    }

    public void registerEvent(ITimedEvent event) {
        eventManager.add(event);
    }

    @Override
    public double getExpScale(SEAT seat) {
        return seat.getStake().toDoubleCents() * seat.getCurrentRate();
    }

    class GameTimer extends TimedEvent {
        public GameTimer(long time, boolean periodical) {
            super(time, periodical);
        }

        public void occur() {
            try {
                //getLog().debug("GameTime: occur()");
                if (gameState != null && isRoomStarted()) {
                    occurGameTimer(gameState);
                } else {
                    //todo: implement logic
                    getLog().error("Table is shutdown, gameState={}", gameState);
                }
            } catch (Exception e) {
                getLog().error("unexpected error", e);
            }
        }
    }

    @Override
    public void sendStartNewRoundToAllPlayers(List<ISeat> seats) {
        try {
            List<ISeat> noPendingSeats = seats.stream().filter((seat) -> {
                IRoomPlayerInfo playerInfo = playerInfoService.get(seat.getAccountId());
                return playerInfo != null && !playerInfo.isPendingOperation();
            }).collect(Collectors.toList());

            List<IStartNewRoundResult> iStartNewRoundResults = socketService.startNewRoundForManyPlayers(noPendingSeats, roomInfo.getId(),
                    roomInfo.getRoundId(), roomInfo.getLastRoundStartDate(), roomInfo.isBattlegroundMode(), roomInfo.getStake().toCents());
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

    @Override
    public IGameConfigService getGameConfigService() {
        return gameConfigService;
    }

    @Override
    public IRoomPlayerInfoService getPlayerInfoService() {
        return playerInfoService;
    }

    @Override
    public ITransportObjectsFactoryService getTOFactoryService() {
        return toFactoryService;
    }

    @Override
    public void changeBonusStatus(IActiveCashBonusSession newCashBonusSession) throws CommonException {
        getLog().debug("changeBonusStatus, newCashBonusSession: {}", newCashBonusSession);
        long accountId = newCashBonusSession.getAccountId();
        boolean needFinishBonus = false;
        playerInfoService.lock(accountId);
        getLog().debug("changeBonusStatus lock: {}", accountId);
        try {
            SEAT seatByAccountId = getSeatByAccountId(accountId);
            IRoomPlayerInfo playerInfo = seatByAccountId.getPlayerInfo();
            IActiveCashBonusSession activeCashBonusSession = playerInfo.getActiveCashBonusSession();
            String status = newCashBonusSession.getStatus();
            if ("CANCELLED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)
                    || "CANCELLING".equalsIgnoreCase(status)) {
                needFinishBonus = true;
                seatByAccountId.getSocketClient().sendMessage(getTOFactoryService().createBonusStatusChangedMessage(
                        activeCashBonusSession.getId(), activeCashBonusSession.getStatus(), newCashBonusSession.getStatus(),
                        "", BonusType.CASHBONUS.name()));
            }
            playerInfo.setActiveCashBonusSession(newCashBonusSession);
            playerInfoService.put(playerInfo);
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("changeBonusStatus lock: {}", accountId);
        }
        if (needFinishBonus) {
            getLog().debug("changeBonusStatus, start finish");
            finish(false);
        }
    }

    @Override
    public void changeFRBBonusStatus(IActiveFrbSession activeFrbSession) throws CommonException {
        getLog().debug("changeFRBBonusStatus, activeFrbSession: {}", activeFrbSession);
        long accountId = activeFrbSession.getAccountId();
        boolean needFinishBonus = false;
        playerInfoService.lock(accountId);
        getLog().debug("changeFRBBonusStatus lock: {}", accountId);
        try {
            SEAT seatByAccountId = getSeatByAccountId(accountId);
            IRoomPlayerInfo playerInfo = seatByAccountId.getPlayerInfo();
            String status = activeFrbSession.getStatus();
            if ("CANCELLED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
                needFinishBonus = true;
            }
            playerInfo.setActiveFrbSession(activeFrbSession);
            playerInfoService.put(playerInfo);
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("changeFRBBonusStatus lock: {}", accountId);
        }

        if (needFinishBonus) {
            getLog().debug("changeFRBBonusStatus, start finish");
            finish(false);
        }
    }

    @Override
    public void changeTournamentState(ITournamentSession tournamentSession) throws CommonException {
        getLog().debug("changeTournamentState, tournamentSession: {}", tournamentSession);
        long accountId = tournamentSession.getAccountId();
        boolean needFinish = false;
        playerInfoService.lock(accountId);
        getLog().debug("changeTournamentState lock: {}", accountId);
        try {
            SEAT seatByAccountId = getSeatByAccountId(accountId);
            IRoomPlayerInfo playerInfo = seatByAccountId.getPlayerInfo();
            String status = tournamentSession.getState();
            ITournamentSession currentTournamentSession = playerInfo.getTournamentSession();
            if ("QUALIFICATION".equalsIgnoreCase(status) || "FINISHED".equalsIgnoreCase(status) ||
                    "CANCELLED".equalsIgnoreCase(status)) {
                needFinish = true;
                seatByAccountId.getSocketClient().sendMessage(getTOFactoryService().
                        createTournamentStateChangedMessage(tournamentSession.getTournamentId(),
                                currentTournamentSession.getState(), tournamentSession.getState(),
                                ""));
            }
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("changeTournamentState lock: {}", accountId);
        }
        if (needFinish) {
            getLog().debug("changeTournamentState, start finish");
            finish(false);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fireReBuyAccepted(SEAT seat) throws CommonException {
        getGameState().fireReBuyAccepted(seat);
    }

    public Disposable getUpdateTimer() {
        return updateTimer;
    }

    public void updateWeaponPrices() {
        IGameConfig gameConfig = getGame().getGameConfig(getId());
        if (gameConfig == null) {
            return;
        }
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                seat.sendMessage(getTOFactoryService().createUpdateWeaponPaidMultiplierResponse(getCurrentTime(), -1,
                        gameConfig.getWeaponPrices()));
            }
        }
    }

    public Map<Integer, List<Integer>> getReels() {
        Map<Integer, List<Integer>> reels = new HashMap<>();
        int[][] configReels = getConfigReels();
        if (configReels != null) {
            int i = 1;
            for (int[] reel : configReels) {
                reels.put(i++, Arrays.stream(reel).boxed().collect(Collectors.toList()));
            }
        }
        return reels;
    }

    public int[][] getConfigReels() {
        return null;
    }

    public int getDefaultBetLevel() {
        return 1;
    }

    @Override
    public void registerStateChangedListener(IRoomStateChangedListener listener) {
        stateChangedListeners.add(listener);
    }

    @Override
    public void registerSeatsCountChangedListener(ISeatsCountChangedListener listener) {
        seatsCountChangedListeners.add(listener);
    }

    @Override
    public void registerOpenRoomListener(IRoomOpenedListener listener) {
        roomOpenedListeners.add(listener);
    }

    @Override
    public void registerCloseRoomListener(IRoomClosedListener listener) {
        roomClosedListeners.add(listener);
    }

    @Override
    public void removeObserverByAccountId(long accountId){
        observePlayers.remove(accountId);
    }

    public boolean isNotAllowPlayWithAnyPendingPlayers() {
        return false;
    }

    protected List<ITransportObserver> getTransportObservers() {

        List<ITransportObserver> result = new ArrayList<>();
        IRoomInfo roomInfo = getRoomInfo();

        if(roomInfo == null) {
            getLog().error("getTransportObservers: roomInfo is null, return empty List<ITransportObserver>");
            return result;
        }

        Collection<IGameSocketClient> observers = getObservers();

        //remove all disconnected observers from the collection
        observers = observers.stream()
                .filter(o -> !o.isDisconnected())
                .collect(Collectors.toCollection(ArrayList::new));

        if(!roomInfo.isPrivateRoom()) {
            //generate observers originally from GameSocketClients List
            for (IGameSocketClient observer : observers) {
                if (observer != null) {

                    Status status = getSeatByAccountId(observer.getAccountId()) == null ? Status.WAITING : Status.READY;

                    ITransportObserver transportObserver =
                            getTOFactoryService().createObserver(
                                    observer.getNickname(),
                                    observer.isKicked(),
                                    status,
                                    observer.isOwner() ? true : null);

                    getLog().debug("getTransportObservers: roomId={}, transportObserver={}", getId(), transportObserver);

                    result.add(transportObserver);
                }
            }
        } else {

            PrivateRoom privateRoom = null;
            String privateRoomID = roomInfo.getPrivateRoomId();

            if(!StringUtils.isTrimmedEmpty(privateRoomID) && this.roomInfoService instanceof IPrivateRoomInfoService) {

                privateRoom = ((IPrivateRoomInfoService)this.roomInfoService).getPlayersStatusInPrivateRoom(privateRoomID);

                //Transfer players in WAITING status from Observers list
                // if these are not present in Private Room Player's list
                if(privateRoom != null && privateRoom.getPlayers() != null && !privateRoom.getPlayers().isEmpty()
                        && observers != null && !observers.isEmpty()) {

                    List<String> playersNicknames = privateRoom.getPlayers().stream()
                            .map(Player::getNickname)
                            .collect(Collectors.toList());

                    List<IGameSocketClient> observersNotInPlayersList = observers.stream()
                            .filter(o -> o != null && !playersNicknames.contains(o.getNickname()))
                            .collect(Collectors.toList());

                    if(!observersNotInPlayersList.isEmpty()) {

                        for(IGameSocketClient observer : observersNotInPlayersList) {
                            Player playerFromObservers =
                                    new Player(observer.getNickname(), observer.getAccountId(), null, Status.WAITING);

                            getLog().debug("getTransportObservers: new Player found from observers which is not in the " +
                                    "players list, add it {}", playerFromObservers);

                            privateRoom.getPlayers().add(playerFromObservers);
                        }

                        UpdatePrivateRoomResponse updatePrivateRoomResponse =
                                ((IPrivateRoomInfoService)this.roomInfoService)
                                        .updatePlayersStatusInPrivateRoom(privateRoom, false, false);

                        if(updatePrivateRoomResponse != null && updatePrivateRoomResponse.getCode() == 200
                                && updatePrivateRoomResponse.getPrivateRoom() != null) {
                            privateRoom = updatePrivateRoomResponse.getPrivateRoom();
                        }
                    }
                }
            }

            //if Private Room Player's list exists, generate observers from this list
            if(privateRoom != null && privateRoom.getPlayers() != null && !privateRoom.getPlayers().isEmpty()) {

                for(Player player : privateRoom.getPlayers()) {
                    String ownerNickname = privateRoom.getOwnerNickname();
                    Boolean isOwner =
                            !StringUtils.isTrimmedEmpty(ownerNickname) && ownerNickname.equals(player.getNickname()) ?
                                    true : null;

                    ITransportObserver observer =
                            getTOFactoryService().createObserver(
                                    player.getNickname(),
                                    Status.KICKED.equals(player.getStatus()),
                                    player.getStatus(),
                                    isOwner
                            );

                    result.add(observer);
                }

            }
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameRoom [");
        sb.append("roomInfo.id=").append(roomInfo != null ? roomInfo.getId() : "'roomInfo is null'");
        sb.append(", tableStart=").append(tableStart);
        sb.append(", gameState=").append(gameState);
        sb.append(", getObserverCount()=").append(getObserverCount());
        sb.append(']');
        return sb.toString();
    }
}
