package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.exceptions.ServiceNotStartedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.abs.casino.common.mp.TransactionErrorCodes;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.context.ApplicationContext;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * User: flsh
 * Date: 06.02.19.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractRoomService<ROOM extends IRoom, SNAPSHOT extends IGameRoomSnapshot, SEAT extends ISeat,
        ROOM_INFO extends IRoomInfo> implements IRoomService<ROOM, SNAPSHOT, SEAT, ROOM_INFO> {
    protected ApplicationContext context;
    protected final HazelcastInstance hazelcast;
    protected final GameMapStore gameMapStore;
    protected ConcurrentMap<Long, ROOM> rooms = new ConcurrentHashMap<>();
    protected IExecutorService remoteExecutorService;
    protected final IGameRoomSnapshotPersister snapshotPersister;
    protected final IRoomPlayerInfoService playerInfoService;
    protected final ICurrencyRateService currencyRateService;
    protected final IGameConfigProvider gameConfigProvider;
    protected final ISpawnConfigProvider spawnConfigProvider;
    protected volatile boolean started = false;

    private final String loggerDir;

    public AbstractRoomService(ApplicationContext context, HazelcastInstance hazelcast, String loggerDir,
                               IGameRoomSnapshotPersister snapshotPersister,
                               IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        this.context = context;
        this.hazelcast = hazelcast;
        this.gameMapStore = context.getBean("gameMapStore", GameMapStore.class);
        this.loggerDir = loggerDir;
        this.remoteExecutorService = hazelcast.getExecutorService("default");
        this.snapshotPersister = snapshotPersister;
        this.playerInfoService = (IRoomPlayerInfoService) context.getBean("playerInfoService");
        this.currencyRateService = (ICurrencyRateService) context.getBean("currencyRateService");
        this.gameConfigProvider = gameConfigProvider;
        this.spawnConfigProvider = spawnConfigProvider;
    }

    protected abstract Logger getLogger();

    @Override
    public synchronized void init() {
        getLogger().info("init: completed");
        started = true;
    }

    @Override
    public synchronized void shutdown() {
        getLogger().info("Shutdown started: {}", getType());
        if (started) {
            started = false;
            for (ROOM room : rooms.values()) {
                IGameRoomSnapshot snapshot = null;
                try {
                    getLogger().debug("Try shutdown room={}", room.getId());
                    snapshot = room.shutdown();
                } catch (Exception e) {
                    getLogger().error("Cannot shutdown room: {}", room.getId(), e);
                }
                //snapshot may be null if not required
                if (snapshot != null) {
                    try {
                        snapshotPersister.persist(snapshot);
                        ISeat[] seats = snapshot.getSeats();
                        if (seats != null && seats.length > 0) {
                            for (ISeat seat : seats) {
                                if (seat != null) {
                                    playerInfoService.put(seat.getPlayerInfo());
                                }
                            }
                        }
                    } catch (Exception e) {
                        getLogger().error("Cannot persist snapshot", e);
                    }
                }
            }
        }
        getLogger().info("Shutdown completed: {}", getType());
    }

    protected void assertServiceStarted() throws ServiceNotStartedException {
        if (!started) {
            throw new ServiceNotStartedException();
        }
    }

    @Override
    public ROOM put(ROOM room) throws ServiceNotStartedException {
        assertServiceStarted();
        ROOM createdRoom = rooms.putIfAbsent(room.getId(), room);
        return createdRoom == null ? room : createdRoom;
    }

    @Override
    public void remove(long id) {
        rooms.remove(id);
    }

    @Override
    public ROOM getRoom(long id) throws ServiceNotStartedException {
        assertServiceStarted();
        return rooms.get(id);
    }

    @Override
    public Collection<ROOM> getRooms() throws ServiceNotStartedException {
        assertServiceStarted();
        return rooms.values();
    }

    @Override
    public Map<Long, ROOM> getRoomsUnmodifiableMap() throws ServiceNotStartedException {
        assertServiceStarted();
        return Collections.unmodifiableMap(new ConcurrentHashMap<>(rooms));
    }

    protected abstract ROOM create(ROOM_INFO roomInfo, IPlayerStatsService playerStatsService,
                                   IWeaponService weaponService, IPlayerQuestsService playerQuestsService,
                                   IPlayerProfileService playerProfileService,
                                   IGameConfigService gameConfigService,
                                   IActiveFrbSessionService activeFrbSessionService,
                                   IActiveCashBonusSessionService activeCashBonusSessionService,
                                   ITournamentService tournamentService);

    protected abstract ROOM repair(ROOM_INFO roomInfo, IPlayerStatsService playerStatsService,
                                   IWeaponService weaponService, IPlayerQuestsService playerQuestsService,
                                   IPlayerProfileService playerProfileService,
                                   SNAPSHOT snapshot, IGameConfigService gameConfigService,
                                   IActiveFrbSessionService activeFrbSessionService,
                                   IActiveCashBonusSessionService activeCashBonusSessionService,
                                   ITournamentService tournamentService);

    private ROOM repairFromSnapshot(IGameRoomSnapshot snapshot, ROOM_INFO roomInfo,
                                    IPlayerStatsService playerStatsService, IPlayerQuestsService playerQuestsService,
                                    IWeaponService weaponService, IPlayerProfileService playerProfileService,
                                    IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                                    IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService) {

        ISeat[] snapshotSeats = snapshot.getSeats();
        long roomId = roomInfo.getId();

        if (snapshotSeats != null && snapshotSeats.length > 0) {
            for (ISeat snapshotSeat : snapshotSeats) {
                if (snapshotSeat != null) {
                    //need set actual roomPlayerInfo
                    IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(snapshotSeat.getAccountId());
                    getLogger().debug("fromSnapshot: Actual player info={}", roomPlayerInfo);

                    if (roomPlayerInfo != null && roomPlayerInfo.getRoomId() == roomId) {
                        snapshotSeat.setPlayerInfo(roomPlayerInfo);
                        snapshotSeat.setSitOutStarted(false);
                    } else {
                        getLogger().error("fromSnapshot: found not actual roomPlayerInfo!!!! Please fix ASAP!!!");
                        snapshotSeat.setPlayerInfo(null);
                    }
                }
            }
        }

        IMap map = snapshot.getMap();
        GameMapShape mapShape = gameMapStore.getMap(map.getMapId());
        if (mapShape == null) {
            getLogger().warn("fromSnapshot: mapShape not found for map={}, set to startMap", map);
            mapShape = gameMapStore.getStartMap(roomInfo.getGameType());
        }
        map.setMapShape(mapShape);

        getLogger().debug("fromSnapshot: repair");
        ROOM room =  this.repair(roomInfo, playerStatsService, weaponService, playerQuestsService,
                playerProfileService, (SNAPSHOT) snapshot, gameConfigService, activeFrbSessionService,
                activeCashBonusSessionService, tournamentService);

        return room;
    }

    private void refundRoomPlayersIfRequired(ROOM_INFO roomInfo, ISocketService socketService, IRoomInfoService roomInfoService) throws InterruptedException {

        long roomId = roomInfo.getId();
        Set<IRoomPlayerInfo> roomPlayerInfos = new HashSet<>(playerInfoService.getForRoom(roomId));
        getLogger().debug("refundRoomPlayersIfRequired: roomPlayerInfos size={}", roomPlayerInfos.size());

        while (!roomPlayerInfos.isEmpty()) {

            getLogger().debug("refundRoomPlayersIfRequired: Found RoomPlayerInfo without snapshot. roomId={}, players={}", roomId, roomPlayerInfos.size());

            CountDownLatch refundLatch = new CountDownLatch(roomPlayerInfos.size());
            Set<IRoomPlayerInfo> failedRoomPlayers = new HashSet<>();

            for (IRoomPlayerInfo currRoomPlayerInfo : roomPlayerInfos) {

                IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(currRoomPlayerInfo.getId());
                getLogger().debug("refundRoomPlayersIfRequired: check crashed room, found roomPlayerInfo={}", roomPlayerInfo);

                if (roomPlayerInfo.getActiveFrbSession() != null) {

                    getLogger().debug("refundRoomPlayersIfRequired: skip repair FRB room: {}", roomId);
                    //not required repair FRB
                    // frbRoom = true;
                    refundLatch.countDown();
                    continue;

                } else if (roomInfo.getMoneyType() == MoneyType.FREE) {

                    playerInfoService.remove(roomInfoService, roomId, roomPlayerInfo.getId());
                    refundLatch.countDown();
                    continue;

                }

                int serverId = IRoom.extractServerId(roomPlayerInfo.getSessionId());

                IPlayerBet playerBet = roomPlayerInfo.createNewPlayerBet();
                playerBet.setBet(roomPlayerInfo.getRoundBuyInAmount());
                playerBet.setWin(roomPlayerInfo.getRoundBuyInAmount());
                playerBet.setDateTime(System.currentTimeMillis());
                playerBet.setData("Refund");

                socketService.addWin(serverId,
                                roomPlayerInfo.getSessionId(),
                                roomPlayerInfo.getGameSessionId(),
                                Money.ZERO,
                                Money.fromCents(roomPlayerInfo.getRoundBuyInAmount()),
                                roomPlayerInfo.getExternalRoundId(),
                                roomId,
                                roomPlayerInfo.getId(),
                                playerBet,
                                null)
                        .doOnSuccess(addWinResult -> {
                            try {

                                getLogger().debug("refundRoomPlayersIfRequired: Refund success for accountId={}, addWinResult={}",
                                        roomPlayerInfo.getId(), addWinResult);

                                if (addWinResult.getErrorCode() == TransactionErrorCodes.OK ||
                                        addWinResult.getErrorCode() == TransactionErrorCodes.FOUND_PENDING_TRANSACTION) {

                                    playerInfoService.remove(roomInfoService, roomId, roomPlayerInfo.getId());

                                } else { //same as doOnError

                                    failedRoomPlayers.add(roomPlayerInfo);
                                }

                            } finally {
                                refundLatch.countDown();
                            }
                        })
                        .doOnError(error -> {
                            try {
                                getLogger().error("refundRoomPlayersIfRequired: Refund failed for account", error);

                                if (roomPlayerInfo.getRoundBuyInAmount() > 0) {

                                    failedRoomPlayers.add(roomPlayerInfo);

                                } else {

                                    getLogger().error("refundRoomPlayersIfRequired: Refund failed, but returnedBet=0, process as success");
                                    playerInfoService.remove(roomInfoService, roomId, roomPlayerInfo.getId());

                                }
                            } finally {
                                refundLatch.countDown();
                            }
                        })
                        .subscribeOn(Schedulers.elastic())
                        .subscribe();
            }

            refundLatch.await();

            if (failedRoomPlayers.isEmpty()) {

                getLogger().debug("refundRoomPlayersIfRequired: All players refunded, return empty room");
                break;

            } else {

                getLogger().error("refundRoomPlayersIfRequired: found failed refunds, failedRoomPlayers.size={}", failedRoomPlayers.size());
                roomPlayerInfos = failedRoomPlayers;
                //noinspection BusyWait
                Thread.sleep(500);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ROOM newInstance(ROOM_INFO roomInfo, ISocketService socketService,
                            IPlayerStatsService playerStatsService,
                            IPlayerQuestsService playerQuestsService,
                            IWeaponService weaponService,
                            IPlayerProfileService playerProfileService,
                            boolean onlyFromSnapshot, IGameConfigService gameConfigService,
                            IActiveFrbSessionService activeFrbSessionService,
                            IActiveCashBonusSessionService activeCashBonusSessionService,
                            ITournamentService tournamentService,
                            IRoomInfoService roomInfoService)
            throws ServiceNotStartedException {

        assertServiceStarted();
        long roomId = roomInfo.getId();
        ROOM room = null;
        try {

            getLogger().debug("newInstance: Try find snapshot for roomId={}, roundId={}", roomId, roomInfo.getRoundId());
            IGameRoomSnapshot snapshot = this.getSnapshot(roomInfo);
            getLogger().debug("newInstance: snapshot for roomId={}, roundId={}, snapshot={}", roomId, roomInfo.getRoundId(), snapshot);

            if (snapshot != null) {
                room = this.repairFromSnapshot(snapshot, roomInfo, playerStatsService, playerQuestsService,
                        weaponService, playerProfileService, gameConfigService, activeFrbSessionService,
                        activeCashBonusSessionService, tournamentService);
            } else {

                if (roomInfo.getGameType().isSingleNodeRoomGame()) {
                    //check for room crash for refund
                    this.refundRoomPlayersIfRequired(roomInfo, socketService, roomInfoService);
                }

                room = this.create(roomInfo, playerStatsService, weaponService, playerQuestsService,
                        playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService,
                        tournamentService);
            }

        } catch (Exception e) {
            getLogger().error("Cannot load (or process) snapshot for roomId={}, roundId={}", roomId, roomInfo.getRoundId(), e);
        }

        getLogger().debug("newInstance: completed");
        return room;
    }

    public IExecutorService getRemoteExecutorService() {
        return remoteExecutorService;
    }

    private IGameRoomSnapshot getSnapshot(ROOM_INFO roomInfo) {
        IGameRoomSnapshot snapshot = null;
        long roomId = roomInfo.getId();
        try {
            if (roomInfo.getGameType().isSingleNodeRoomGame()) {
                snapshot = snapshotPersister.get(roomId, roomInfo.getRoundId());
            }
        } catch (Exception e) {
            getLogger().error("Cannot load snapshot for roomId={}, need refund", roomId, e);
        }
        return snapshot;
    }

    /**
     * For details see docs at
     *
     * @link http://logging.apache.org/log4j/2.x/manual/customconfig.html#AddingToCurrent
     */
    protected synchronized Logger createRoomLogger(long roomId, GameType gameType, MoneyType mode) {
        final String name = "Room-" + roomId;
        final String path = loggerDir + "/rooms/" + mode.name().toLowerCase() +
                "/" + gameType.name().toLowerCase().replaceAll("_", "") + "-" + roomId;
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("%d{yyyy.MM.dd HH:mm:ss,SSS} %-5p [%t] %m%n")
                .build();
        CompositeTriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(
                SizeBasedTriggeringPolicy.createPolicy("10 MB"),
                OnStartupTriggeringPolicy.createPolicy(1)
        );
        DefaultRolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
                .withConfig(config)
                .withMax("100")
                .build();
        Appender appender = RollingFileAppender.newBuilder()
                .withName(name)
                .withFileName(path + ".log")
                .withFilePattern(path + "_%d{yyyy-MM-dd_HH_mm_ss}_%i.logg")
                .withLayout(layout)
                .withPolicy(policy)
                .withStrategy(strategy)
                .build();
        appender.start();
        config.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef(name, null, null);
        AppenderRef[] refs = new AppenderRef[]{ref};
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.DEBUG, name, "true", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(name, loggerConfig);
        ctx.updateLoggers();
        return LogManager.getLogger(name);
    }

}
