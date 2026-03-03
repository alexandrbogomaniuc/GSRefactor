package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.amazon.service.AmazonRoomService;
import com.betsoft.casino.mp.bgdragonstone.service.DragonStoneBattleGroundRoomService;
import com.betsoft.casino.mp.bgmissionamazon.service.BGMissionAmazonRoomService;
import com.betsoft.casino.mp.bgsectorx.service.BGSectorXRoomService;
import com.betsoft.casino.mp.clashofthegods.service.ClashOfTheGodsRoomService;
import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.data.persister.*;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.dragonstone.service.DragonStoneRoomService;
import com.betsoft.casino.mp.exceptions.ServiceNotStartedException;
import com.betsoft.casino.mp.maxblastchampions.service.MaxBlastChampionsRoomService;
import com.betsoft.casino.mp.maxcrashgame.service.LunarCrashRoomService;
import com.betsoft.casino.mp.maxcrashgame.service.MaxCrashRoomService;
import com.betsoft.casino.mp.maxcrashgame.service.TripleMaxBlastRoomService;
import com.betsoft.casino.mp.missionamazon.service.MissionAmazonRoomService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IMultiNodeRoom;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoom;
import com.betsoft.casino.mp.pirates.service.PiratesRoomService;
import com.betsoft.casino.mp.piratesdmc.service.PiratesDMCRoomService;
import com.betsoft.casino.mp.piratespov.service.PiratesPOVRoomService;
import com.betsoft.casino.mp.revengeofra.service.RevengeOfRaRoomService;
import com.betsoft.casino.mp.sectorx.service.SectorXRoomService;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.kafka.dto.RunningRoomDto;
import com.abs.casino.kafka.dto.privateroom.response.DeactivateRoomResultDto;
import com.hazelcast.core.*;
import com.hazelcast.core.Member;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * User: flsh
 * Date: 03.11.17.
 */
@SuppressWarnings("rawtypes")
@Service
public class RoomServiceFactory implements IRoomServiceFactory, MembershipListener {
    private static final Logger LOG = LogManager.getLogger(RoomServiceFactory.class);
    private final EnumMap<GameType, IRoomService> roomServices = new EnumMap<>(GameType.class);
    private final ApplicationContext context;
    private final HazelcastInstance hazelcast;
    private final SingleNodeRoomInfoService singleNodeRoomInfoService;
    private final MultiNodeRoomInfoService multiNodeRoomInfoService;
    private final BGPrivateRoomInfoService bgPrivateRoomInfoService;
    private final MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService;
    private final SocketService socketService;
    private final PlayerStatsPersister playerStatsPersister;
    private final ServerConfigService serverConfigService;
    private final SocketServer socketServer;
    private final IWeaponService weaponService;
    private final String loggerDir;
    private final GameRoomSnapshotPersister gameRoomSnapshotPersister;
    private final PlayerQuestsPersister playerQuestsPersister;
    private final PlayerProfilePersister playerProfilePersister;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private final ScheduledExecutorService cleanPrivateRoomsExecutor
            = Executors.newSingleThreadScheduledExecutor();
    private static final long ONE_MIN_MS = Duration.ofMinutes(1).toMillis();
    private static final long CLEAR_ROOMS_PERIOD_MS = Duration.ofHours(1).toMillis();

    private static final long PRIVATE_ROOM_EXPIRATION_DURATION_MS = Duration.ofDays(7).toMillis(); //7 days
    private static final long PUBLIC_ROOM_EXPIRATION_DURATION_MS = Duration.ofDays(7).toMillis(); //7 days

    private static final long DEACTIVATED_PRIVATE_ROOM_THRESHOLD_MS = Duration.ofDays(1).toMillis(); // 1 day
    private static final long DEACTIVATED_PUBLIC_ROOM_THRESHOLD_MS = Duration.ofDays(1).toMillis(); // 1 day

    private ReentrantLock cleanPrivateRoomsExecutorLock = new ReentrantLock();

    private final GameConfigPersister gameConfigPersister;
    private final IActiveFrbSessionService activeFrbSessionService;
    private final IActiveCashBonusSessionService activeCashBonusSessionService;
    private final ITournamentService tournamentService;
    private final IGameConfigProvider gameConfigProvider;
    private final ISpawnConfigProvider spawnConfigProvider;
    private final ISharedGameStateService sharedGameStateService;
    private final IMultiNodeSeatService multiNodeSeatService;
    private final IRoomPlayerInfoService roomPlayerInfoService;

    public RoomServiceFactory(ApplicationContext context, HazelcastInstance hazelcast,
                              CassandraPersistenceManager cpm,
                              String loggerDir,
                              IGameConfigProvider gameConfigProvider,
                              ISpawnConfigProvider spawnConfigProvider) {
        this.context = context;
        this.hazelcast = hazelcast;
        this.singleNodeRoomInfoService = context.getBean("singleNodeRoomInfoService", SingleNodeRoomInfoService.class);
        this.multiNodeRoomInfoService = context.getBean("multiNodeRoomInfoService", MultiNodeRoomInfoService.class);
        this.bgPrivateRoomInfoService = context.getBean("bgPrivateRoomInfoService", BGPrivateRoomInfoService.class);
        this.multiNodePrivateRoomInfoService = context.getBean("multiNodePrivateRoomInfoService", MultiNodePrivateRoomInfoService.class);
        this.socketService = context.getBean("socketService", SocketService.class);
        this.playerStatsPersister = cpm.getPersister(PlayerStatsPersister.class);
        this.serverConfigService = context.getBean("serverConfigService", ServerConfigService.class);
        this.socketServer = context.getBean("socketServer", SocketServer.class);
        this.weaponService = cpm.getPersister(WeaponsPersister.class);
        this.loggerDir = loggerDir;
        this.playerQuestsPersister = cpm.getPersister(PlayerQuestsPersister.class);
        this.gameRoomSnapshotPersister = cpm.getPersister(GameRoomSnapshotPersister.class);
        this.playerProfilePersister = cpm.getPersister(PlayerProfilePersister.class);
        this.gameConfigPersister = cpm.getPersister(GameConfigPersister.class);
        this.activeFrbSessionService = cpm.getPersister(ActiveFrbSessionPersister.class);
        this.activeCashBonusSessionService = cpm.getPersister(ActiveCashBonusSessionPersister.class);
        this.tournamentService = cpm.getPersister(TournamentSessionPersister.class);
        this.gameConfigProvider = gameConfigProvider;
        this.spawnConfigProvider = spawnConfigProvider;
        this.sharedGameStateService = (ISharedGameStateService) context.getBean("sharedGameStateService");
        this.multiNodeSeatService = (IMultiNodeSeatService) context.getBean("multiNodeSeatService");
        this.roomPlayerInfoService = singleNodeRoomInfoService.getRoomPlayerInfoService();
    }

    @PostConstruct
    private void init() {
        addRoomService(new PiratesRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister));
        addRoomService(new AmazonRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister));

        addRoomService(new PiratesPOVRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister));

        addRoomService(new RevengeOfRaRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister));
        addRoomService(new DragonStoneRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));
        addRoomService(new ClashOfTheGodsRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister));

        addRoomService(new PiratesDMCRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister));

        addRoomService(new DragonStoneBattleGroundRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));

        addRoomService(new MissionAmazonRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));

        addRoomService(new MaxCrashRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));

        addRoomService(new TripleMaxBlastRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));

        addRoomService(new LunarCrashRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));

        addRoomService(new BGMissionAmazonRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));

        addRoomService(new SectorXRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));

        addRoomService(new MaxBlastChampionsRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));

        addRoomService(new BGSectorXRoomService(context, hazelcast, loggerDir, gameRoomSnapshotPersister,
                gameConfigProvider, spawnConfigProvider));


        //startSingleNodeRooms(singleNodeRoomInfoService.getThisServerRooms(), singleNodeRoomInfoService);
        //startMultiNodeRooms(multiNodeRoomInfoService.getThisServerRooms(), multiNodeRoomInfoService);

        //todo should we start Private Rooms
        //startSingleNodeRooms(bgPrivateRoomInfoService.getThisServerRooms(), bgPrivateRoomInfoService);
        //todo should we start Private Rooms
        //startMultiNodeRooms(multiNodePrivateRoomInfoService.getThisServerRooms(), multiNodePrivateRoomInfoService);

        hazelcast.getCluster().addMembershipListener(this);
        initCleanPrivateRoomsExecutor();
    }

    @Override
    public IRoomInfo getRoomInfo(long roomId) {
        IRoomInfo room = singleNodeRoomInfoService.getRoom(roomId);
        if (room == null) {
            room = multiNodeRoomInfoService.getRoom(roomId);
        }
        if (room == null) {
            room = bgPrivateRoomInfoService.getRoom(roomId);
        }
        if (room == null) {
            room = multiNodePrivateRoomInfoService.getRoom(roomId);
        }
        return room;
    }

    //todo probably dump code, Haven't thought of a better one yet
    private IRoomInfoService getRoomInfoServiceByRoomId(long roomId) {
        if (singleNodeRoomInfoService.getRoom(roomId) != null) {
            return singleNodeRoomInfoService;
        }
        if (multiNodeRoomInfoService.getRoom(roomId) != null) {
            return multiNodeRoomInfoService;
        }
        if (bgPrivateRoomInfoService.getRoom(roomId) != null) {
            return bgPrivateRoomInfoService;
        }
        if (multiNodePrivateRoomInfoService.getRoom(roomId) != null) {
            return multiNodePrivateRoomInfoService;
        }
        return null;
    }

    private synchronized void startSingleNodeRooms(Collection<? extends SingleNodeRoomInfo> roomInfos, AbstractRoomInfoService roomInfoService) {
        for (SingleNodeRoomInfo roomInfo : roomInfos) {
            if(!roomInfo.isDeactivated()) {
                scheduler.execute(() -> {
                    this.startSingleNodeRoom(roomInfo, roomInfoService);
                });
            }
        }
    }

    private void startSingleNodeRoom(SingleNodeRoomInfo roomInfo, AbstractRoomInfoService roomInfoService) {
        if(roomInfo == null) {
            LOG.error("startSingleNodeRooms: roomInfo is null");
            return;
        }

        if(roomInfoService == null) {
            LOG.error("startSingleNodeRooms: roomInfoService is null");
            return;
        }

        IRoomService roomService;
        long roomId = roomInfo.getId();

        try {
            roomService = this.getRoomServiceWithCheck(roomInfo.getGameType());
        } catch (CommonException e) {
            LOG.error("startSingleNodeRooms: Cannot load RoomService for start room={}", roomId, e);
            return;
        }

        //try to force unlock roomId first if locked
        if (roomInfo.getGameServerId() == serverConfigService.getServerId() && this.isLocked(roomInfoService, true, roomId)) {
            try {
                roomInfoService.forceUnlock(roomId);
                LOG.info("startSingleNodeRooms: startServerRooms: Success unlock room={}", roomId);
            } catch (Exception e) {
                LOG.error("startSingleNodeRooms: startServerRooms: Cannot unlock, roomId={}, exception={}", roomId, e.getMessage());
            }
        }

        boolean locked = false;
        try {
            locked = this.tryLock(roomInfoService, true, roomId, 10);
        } catch (Exception e) {
            LOG.error("startSingleNodeRooms: Interrupted for room.id={}", roomId, e);
        }
        if (locked) {
            try {
                IRoom room = roomService.getRoom(roomId);
                if (room == null) {
                    this.createRoom(roomService, roomId, true);
                }
            } catch (Exception e) {
                LOG.error("startSingleNodeRooms: Cannot start room={}", roomId, e);
            } finally {
                this.unlock(roomInfoService, true, roomId);
            }
        } else {
            LOG.warn("startSingleNodeRooms: Cannot obtain lock for room: {}", roomId);
        }
    }

    private synchronized void startMultiNodeRooms(Collection<? extends MultiNodeRoomInfo> roomInfos, AbstractRoomInfoService roomInfoService) {
        for (MultiNodeRoomInfo roomInfo : roomInfos) {
            if(!roomInfo.isDeactivated()) {
                scheduler.execute(() -> {
                    this.startMultiNodeRoom(roomInfo, roomInfoService);
                });
            }
        }
    }

    private void startMultiNodeRoom(MultiNodeRoomInfo roomInfo, AbstractRoomInfoService roomInfoService) {
        if(roomInfo == null) {
            LOG.error("startMultiNodeRoom: roomInfo is null");
            return;
        }

        if(roomInfoService == null) {
            LOG.error("startMultiNodeRoom: roomInfoService is null");
            return;
        }

        IRoomService roomService;
        long roomId = roomInfo.getId();

        try {
            roomService = this.getRoomServiceWithCheck(roomInfo.getGameType());
        } catch (CommonException e) {
            LOG.error("startMultiNodeRooms: Cannot load RoomService for start room={}", roomId, e);
            return;
        }

        boolean locked = false;
        try {
            locked = this.tryLock(roomInfoService, false, roomId, 10);
        } catch (Exception e) {
            LOG.error("startMultiNodeRooms: Interrupted for room.id={}", roomId, e);
        }
        if (locked) {
            try {
                IRoom room = roomService.getRoom(roomId);
                if (room == null) {
                    this.createRoom(roomService, roomId, false);
                }
            } catch (Exception e) {
                LOG.error("startMultiNodeRooms: Cannot start room={}", roomId, e);
            } finally {
                this.unlock(roomInfoService, false, roomId);
            }
        } else {
            LOG.warn("startMultiNodeRooms: Cannot obtain lock for room: {}", roomId);
        }
    }

    private void initCleanPrivateRoomsExecutor() {
        int serverId = serverConfigService.getServerId();
        LOG.debug("initCleanPrivateRoomsExecutor: serverId={}, Timer is enabled. Starting timer with period {} ms",
                serverId, CLEAR_ROOMS_PERIOD_MS);
        this.cleanPrivateRoomsExecutor
                .scheduleAtFixedRate(
                        this::timerCleanPrivateRoomsWrapper,
                        //(5 + 4 * serverId) mins shift the initial delay for every node
                        (5 + 2L * serverId ) * ONE_MIN_MS,
                        CLEAR_ROOMS_PERIOD_MS,
                        TimeUnit.MILLISECONDS);
    }

    private void timerCleanPrivateRoomsWrapper() {
        if (cleanPrivateRoomsExecutorLock.tryLock()) {
            try {
                LOG.debug("timerCleanPrivateRoomsWrapper: locked.");
                timerCleanPrivateRooms();
            } finally {
                cleanPrivateRoomsExecutorLock.unlock();
                LOG.debug("timerCleanPrivateRoomsWrapper: unlock.");
            }
        } else {
            LOG.warn("timerCleanPrivateRoomsWrapper: Previous timerCleanPrivateRooms is still running. Skipping this execution.");
        }
    }

    public void timerCleanPrivateRooms() {

        long timeBegin = System.currentTimeMillis();

        LOG.debug("timerCleanPrivateRooms: call stopMultinodeAnyRoomsIfNoPlayers");
        this.stopMultinodeAnyRoomsIfNoPlayers();

        LOG.debug("timerCleanPrivateRooms: call deactivateExpiredMultinodeAnyRooms for multiNodePrivateRoomInfoService with roomExpirationDurationMs={}", PRIVATE_ROOM_EXPIRATION_DURATION_MS);
        this.deactivateExpiredMultinodeAnyRooms(multiNodePrivateRoomInfoService, PRIVATE_ROOM_EXPIRATION_DURATION_MS);
        LOG.debug("timerCleanPrivateRooms: call removeDeactivatedMultinodeAnyRoomInfos for multiNodePrivateRoomInfoService with deactivatedRoomThresholdMs={}", DEACTIVATED_PRIVATE_ROOM_THRESHOLD_MS);
        this.removeDeactivatedMultinodeAnyRoomInfos(multiNodePrivateRoomInfoService, DEACTIVATED_PRIVATE_ROOM_THRESHOLD_MS);

        LOG.debug("timerCleanPrivateRooms: call deactivateExpiredMultinodeAnyRooms for multiNodeRoomInfoService with roomExpirationDurationMs={}", PUBLIC_ROOM_EXPIRATION_DURATION_MS);
        this.deactivateExpiredMultinodeAnyRooms(multiNodeRoomInfoService, PUBLIC_ROOM_EXPIRATION_DURATION_MS);
        LOG.debug("timerCleanPrivateRooms: call removeDeactivatedMultinodeAnyRoomInfos for multiNodeRoomInfoService with deactivatedRoomThresholdMs={}", DEACTIVATED_PUBLIC_ROOM_THRESHOLD_MS);
        this.removeDeactivatedMultinodeAnyRoomInfos(multiNodeRoomInfoService, DEACTIVATED_PUBLIC_ROOM_THRESHOLD_MS);

        LOG.debug("timerCleanPrivateRooms: call stopSingleNodeAnyRoomsIfNoPlayers");
        this.stopSingleNodeAnyRoomsIfNoPlayers();

        LOG.debug("timerCleanPrivateRooms: call deactivateExpiredSingleNodeAnyRooms for bgPrivateRoomInfoService with roomExpirationDurationMs={}", PRIVATE_ROOM_EXPIRATION_DURATION_MS);
        this.deactivateExpiredSingleNodeAnyRooms(bgPrivateRoomInfoService, PRIVATE_ROOM_EXPIRATION_DURATION_MS);
        LOG.debug("timerCleanPrivateRooms: call removeDeactivatedSingleNodeAnyRoomInfos for bgPrivateRoomInfoService with deactivatedRoomThresholdMs={}", DEACTIVATED_PRIVATE_ROOM_THRESHOLD_MS);
        this.removeDeactivatedSingleNodeAnyRoomInfos(bgPrivateRoomInfoService, DEACTIVATED_PRIVATE_ROOM_THRESHOLD_MS);

        LOG.debug("timerCleanPrivateRooms: call deactivateExpiredSingleNodeAnyRooms for singleNodeRoomInfoService with roomExpirationDurationMs={}", PUBLIC_ROOM_EXPIRATION_DURATION_MS);
        this.deactivateExpiredSingleNodeAnyRooms(singleNodeRoomInfoService, PUBLIC_ROOM_EXPIRATION_DURATION_MS);
        LOG.debug("timerCleanPrivateRooms: call removeDeactivatedSingleNodeAnyRoomInfos for singleNodeRoomInfoService with deactivatedRoomThresholdMs={}", DEACTIVATED_PUBLIC_ROOM_THRESHOLD_MS);
        this.removeDeactivatedSingleNodeAnyRoomInfos(singleNodeRoomInfoService, DEACTIVATED_PUBLIC_ROOM_THRESHOLD_MS);

        LOG.debug("timerCleanPrivateRooms: took {} ms", System.currentTimeMillis() - timeBegin);
    }

    private void deactivateExpiredMultinodeAnyRooms(AbstractRoomInfoService multiNodeAnyRoomInfoService, long roomExpirationDurationMs) {
        Collection<AbstractRoomInfo> multiNodeAnyRoomInfos = multiNodeAnyRoomInfoService.getAllRooms();

        if(multiNodeAnyRoomInfos == null || multiNodeAnyRoomInfos.isEmpty()) {
            LOG.debug("deactivateExpiredMultinodeAnyRooms: multiNodeAnyRoomInfos is null or empty, skip");
            return;
        }

        for(IRoomInfo multinodeAnyNodeRoomInfo : multiNodeAnyRoomInfos) {
            if(multinodeAnyNodeRoomInfo.getLastTimeActivity() == 0) {
                long roomId = multinodeAnyNodeRoomInfo.getId();
                LOG.debug("deactivateExpiredMultinodeAnyRooms: update multiNodeAnyRoomInfo with LastTimeActivity for roomId={} found", roomId);
                multiNodeAnyRoomInfoService.lock(roomId);
                try {
                    multinodeAnyNodeRoomInfo = multiNodeAnyRoomInfoService.getRoom(roomId);
                    if(multinodeAnyNodeRoomInfo != null) {
                        multinodeAnyNodeRoomInfo.updateLastTimeActivity();
                        multiNodeAnyRoomInfoService.update(multinodeAnyNodeRoomInfo);
                    }
                } finally {
                    multiNodeAnyRoomInfoService.unlock(roomId);
                }
            }
        }

        long now = System.currentTimeMillis();

        Collection<AbstractRoomInfo> expiredMultiNodeAnyRoomInfos = new ArrayList<>();

        for(AbstractRoomInfo ri : multiNodeAnyRoomInfos) {
            long roomAccessedAgoMs = now - ri.getLastTimeActivity();
            LOG.debug("deactivateExpiredMultinodeAnyRooms: room with Id={}, isDeactivated={}, roomAccessedAgoMs={}, to deactivate={}",
                    ri.getId(), ri.isDeactivated(), roomAccessedAgoMs, !ri.isDeactivated() && roomAccessedAgoMs > roomExpirationDurationMs);
            if(!ri.isDeactivated() && roomAccessedAgoMs > roomExpirationDurationMs) {
                expiredMultiNodeAnyRoomInfos.add(ri);
            }
        }

        LOG.debug("deactivateExpiredMultinodeAnyRooms: expiredMultiNodeAnyRoomInfos count={}",
                expiredMultiNodeAnyRoomInfos.size());

        for(AbstractRoomInfo expiredMultiNodeAnyRoomInfo : expiredMultiNodeAnyRoomInfos) {

            try {
                GameType gameType = expiredMultiNodeAnyRoomInfo.getGameType();
                long roomId = expiredMultiNodeAnyRoomInfo.getId();
                IRoom room = getRoomWithoutCreation(gameType, roomId);

                if (room != null) {
                    LOG.debug("deactivateExpiredMultinodeAnyRooms: localRoom withId={} is not null, skip", roomId);
                    continue;
                }

                if (expiredMultiNodeAnyRoomInfo instanceof MultiNodePrivateRoomInfo) {
                    String privateRoomId = expiredMultiNodeAnyRoomInfo.getPrivateRoomId();
                    long ownerAccountId = expiredMultiNodeAnyRoomInfo.getOwnerAccountId();
                    LOG.debug("deactivateExpiredMultinodeAnyRooms: privateRoomId={} and ownerAccountId={}", privateRoomId, ownerAccountId);
                }

                tryDeactivateMultiNodeAnyRoom(multiNodeAnyRoomInfoService, roomId);
            } catch (Exception e) {
                LOG.debug("deactivateExpiredMultinodeAnyRooms: Exception ", e);
            }
        }
    }

    private void deactivateExpiredSingleNodeAnyRooms(AbstractRoomInfoService singleNodeAnyRoomInfoService, long roomExpirationDurationMs) {
        Collection<AbstractRoomInfo> singleNodeAnyRoomInfos = singleNodeAnyRoomInfoService.getAllRooms();

        if(singleNodeAnyRoomInfos == null || singleNodeAnyRoomInfos.isEmpty()) {
            LOG.debug("deactivateExpiredSingleNodeAnyRooms: singleNodeAnyRoomInfos is null or empty, skip");
            return;
        }

        for(IRoomInfo singleNodeAnyRoomInfo : singleNodeAnyRoomInfos) {
            if(singleNodeAnyRoomInfo.getLastTimeActivity() == 0) {
                long roomId = singleNodeAnyRoomInfo.getId();
                LOG.debug("deactivateExpiredSingleNodeAnyRooms: update singleNodeAnyRoomInfo with LastTimeActivity for roomId={} found", roomId);
                singleNodeAnyRoomInfoService.lock(roomId);
                try {
                    singleNodeAnyRoomInfo = singleNodeAnyRoomInfoService.getRoom(roomId);
                    if(singleNodeAnyRoomInfo != null) {
                        singleNodeAnyRoomInfo.updateLastTimeActivity();
                        singleNodeAnyRoomInfoService.update(singleNodeAnyRoomInfo);
                    }
                } finally {
                    singleNodeAnyRoomInfoService.unlock(roomId);
                }
            }
        }

        long now = System.currentTimeMillis();

        Collection<AbstractRoomInfo> expiredSingleNodeAnyRoomInfos = new ArrayList<>();

        for(AbstractRoomInfo ri : singleNodeAnyRoomInfos) {
            long roomAccessedAgoMs = now - ri.getLastTimeActivity();
            LOG.debug("deactivateExpiredSingleNodeAnyRooms: room with Id={}, isDeactivated={}, roomAccessedAgoMs={}, to deactivate={}",
                    ri.getId(), ri.isDeactivated(), roomAccessedAgoMs, !ri.isDeactivated() && roomAccessedAgoMs > roomExpirationDurationMs);
            if(!ri.isDeactivated() && roomAccessedAgoMs > roomExpirationDurationMs) {
                expiredSingleNodeAnyRoomInfos.add(ri);
            }
        }

        int localServerId = serverConfigService.getServerId();
        LOG.debug("deactivateExpiredSingleNodeAnyRooms: localServerId={}, expiredSingleNodeAnyRoomInfos count={}",
                localServerId, expiredSingleNodeAnyRoomInfos.size());

        for(AbstractRoomInfo expiredSingleNodeAnyRoomInfo : expiredSingleNodeAnyRoomInfos) {

            int roomInfoServerId = ((SingleNodeRoomInfo)expiredSingleNodeAnyRoomInfo).getGameServerId();

            if(roomInfoServerId == -1 || localServerId == roomInfoServerId) {
                try {
                    GameType gameType = expiredSingleNodeAnyRoomInfo.getGameType();
                    long roomId = expiredSingleNodeAnyRoomInfo.getId();
                    IRoom room = getRoomWithoutCreation(gameType, roomId);

                    if (room != null) {
                        LOG.debug("deactivateExpiredSingleNodeAnyRooms: localRoom withId={} is not null, skip", roomId);
                        continue;
                    }

                    if(expiredSingleNodeAnyRoomInfo instanceof BGPrivateRoomInfo) {
                        String privateRoomId = expiredSingleNodeAnyRoomInfo.getPrivateRoomId();
                        long ownerAccountId = expiredSingleNodeAnyRoomInfo.getOwnerAccountId();
                        LOG.debug("deactivateExpiredSingleNodeAnyRooms: privateRoomId={} and ownerAccountId={}", privateRoomId, ownerAccountId);
                    }

                    tryDeactivateSingleNodeAnyRoom(singleNodeAnyRoomInfoService, roomId);
                } catch (Exception e) {
                    LOG.debug("deactivateExpiredSingleNodeAnyRooms: Exception ", e);
                }
            }
        }
    }

    private boolean multinodeRoomToStop(long roomId, IRoom room) {

        if(room == null) {
            return false;
        }

        if (!(room instanceof IMultiNodeRoom)) {
            return false;
        }

        SharedCrashGameState crashGameState = sharedGameStateService.get(roomId, SharedCrashGameState.class);
        if (crashGameState != null) {
            RoomState roomState = crashGameState.getState();
            LOG.debug("multinodeRoomToStop: id={}, roomState={}", roomId, roomState);
            if(roomState != RoomState.WAIT) {
                LOG.debug("multinodeRoomToStop: id={}, roomState is not WAIT, return false", roomId);
                return false;
            }
        }

        int observersCount = room.getObserverCount();
        //int multiNodeSeatsCount = multiNodeSeatService.seatsCount(roomId);
        //int roomPlayersCount = roomPlayerInfoService.getForRoom(roomId).size();

        boolean multinodeRoomToStop = observersCount == 0;// && multiNodeSeatsCount == 0 && roomPlayersCount == 0;
        LOG.debug("multinodeRoomToStop: id={}, multinodeRoomToStop={}, observersCount={}",//, multiNodeSeatsCount={}, roomPlayersCount={}",
                roomId, multinodeRoomToStop, observersCount);//, multiNodeSeatsCount, roomPlayersCount);

        return multinodeRoomToStop;
    }

    private void stopMultinodeAnyRoomsIfNoPlayers() {

        GameType[] multyNodeGameTypes = new GameType[] {
                GameType.BG_MAXCRASHGAME
        };

        for(GameType gameType : multyNodeGameTypes) {
            stopMultinodeAnyRoomsIfNoPlayersForGameType(gameType);
        }
    }

    private void stopMultinodeAnyRoomsIfNoPlayersForGameType(GameType gameType) {

        IRoomService roomService = null;
        try {
            roomService = getRoomServiceWithCheck(gameType);
        } catch (Exception e) {
            LOG.error("stopMultinodeAnyRoomsIfNoPlayersForGameType: exception to get roomService for GameType.{}", gameType.name(), e);
            return;
        }

        if(roomService == null) {
            LOG.debug("stopMultinodeAnyRoomsIfNoPlayersForGameType: roomService is null for GameType.{}, skip", gameType.name());
            return;
        }

        Map<Long, IRoom> localRoomsMap = null;

        try {
            localRoomsMap = roomService.getRoomsUnmodifiableMap();
        } catch (Exception e) {
            LOG.error("stopMultinodeAnyRoomsIfNoPlayersForGameType: exception to get localRoomsMap for GameType.{}", gameType.name(), e);
            return;
        }

        if(localRoomsMap == null || localRoomsMap.isEmpty()) {
            LOG.debug("stopMultinodeAnyRoomsIfNoPlayersForGameType: localRoomsMap is null or empty for GameType.{}, skip", gameType.name());
            return;
        }

        LOG.debug("stopMultinodeAnyRoomsIfNoPlayersForGameType: localRoomsMap size={} for GameType.{}", localRoomsMap.size(), gameType.name());

        for(Map.Entry<Long, IRoom> roomEntry : localRoomsMap.entrySet()) {

            long roomId = roomEntry.getKey();
            IRoom room = roomEntry.getValue();
            try {

                if(!(room instanceof IMultiNodeRoom)) {
                    LOG.debug("stopMultinodeAnyRoomsIfNoPlayersForGameType: local room with id={} is not a " +
                            "IMultiNodeRoom, skip", roomId);
                    continue;
                }

                //room.updateRoomInfo(roomInfo -> {});
                //IRoomInfo roomInfo = room.getRoomInfo();

                //if(roomInfo == null) {
                //    LOG.debug("stopMultinodeAnyRoomsIfNoPlayersForGameType: localRoom with id={} is null, skip", roomId);
                //    continue;
                //}

                //if(!roomInfo.isPrivateRoom()) {
                    //LOG.debug("stopMultinodeAnyRoomsIfNoPlayersForGameType: localRoom with id={} is not private, skip", roomId);
                //    continue;
                //}

                if(multinodeRoomToStop(roomId, room)) {

                    LOG.debug("stopMultinodePrivateRoomsIfNoPlayers: Make a pause for 5 sec before to " +
                            "proceed with 2 check for multinodeRoomToStop value for roomId={} ", roomId);

                    Thread.sleep(Duration.ofSeconds(5).toMillis());

                    if(multinodeRoomToStop(roomId, room)) {
                        //shutdown room
                        LOG.debug("stopMultinodePrivateRoomsIfNoPlayers: stop local room with id={}", roomId);
                        room.shutdown();

                        //remove room from the list in IRoomService
                        LOG.debug("stopMultinodePrivateRoomsIfNoPlayers: remove room with id={} from the roomService", roomId);
                        roomService.remove(roomId);
                    }
                }

            } catch (Exception e) {
                LOG.error("stopMultinodePrivateRoomsIfNoPlayers: exception to process localRoom {}", roomId, e);
                return;
            }
        }
    }

    private boolean singleNodeRoomToStop(long roomId, IRoom room) {

        if(room == null) {
            return false;
        }

        if (!(room instanceof ISingleNodeRoom)) {
            return false;
        }

        int observersCount = room.getObserverCount();
        //int singleNodeSeatsCount = room.getSeatsCount();
        //int roomPlayersCount = roomPlayerInfoService.getForRoom(roomId).size();

        boolean singleNodeRoomToStop = observersCount == 0;// && singleNodeSeatsCount == 0 && roomPlayersCount == 0;
        LOG.debug("singleNodeRoomToStop: id={}, singleNodeRoomToStop={}, observersCount={}",//, singleNodeSeatsCount={}, roomPlayersCount={}",
                roomId, singleNodeRoomToStop, observersCount);//, singleNodeSeatsCount, roomPlayersCount);

        return singleNodeRoomToStop;
    }

    private void stopSingleNodeAnyRoomsIfNoPlayers() {

        GameType[] singleNodeGameTypes = new GameType[] {
                GameType.BG_DRAGONSTONE,
                GameType.BG_MISSION_AMAZON,
                GameType.BG_SECTOR_X,
                GameType.DRAGONSTONE,
                GameType.MISSION_AMAZON,
                GameType.SECTOR_X
        };

        for(GameType gameType : singleNodeGameTypes) {
            stopSingleNodeAnyRoomsIfNoPlayersForGameType(gameType);
        }
    }

    private void stopSingleNodeAnyRoomsIfNoPlayersForGameType(GameType gameType) {

        if(gameType == null) {
            return;
        }

        IRoomService roomService = null;
        try {
            roomService = getRoomServiceWithCheck(gameType);
        } catch (Exception e) {
            LOG.error("stopSingleNodeAnyRoomsIfNoPlayersForGameType: exception to get roomService for GameType.{}", gameType.name(), e);
            return;
        }

        if(roomService == null) {
            LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: roomService is null for GameType.{}, skip", gameType.name());
            return;
        }

        Map<Long, IRoom> localRoomsMap = null;

        try {
            localRoomsMap = roomService.getRoomsUnmodifiableMap();
        } catch (Exception e) {
            LOG.error("stopSingleNodeAnyRoomsIfNoPlayersForGameType: exception to get localRoomsMap for GameType.{}", gameType.name(), e);
            return;
        }

        if(localRoomsMap == null || localRoomsMap.isEmpty()) {
            LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: localRoomsMap is null or empty for GameType.{}, skip", gameType.name());
            return;
        }

        LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: localRoomsMap size={} for GameType.{}", localRoomsMap.size(), gameType.name());

        for(Map.Entry<Long, IRoom> roomEntry : localRoomsMap.entrySet()) {

            long roomId = roomEntry.getKey();
            IRoom room = roomEntry.getValue();
            try {

                if(!(room instanceof ISingleNodeRoom)) {
                    LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: local room with id={} is not a " +
                            "ISingleNodeRoom, skip", roomId);
                    continue;
                }

                //room.updateRoomInfo(roomInfo -> {});
                //IRoomInfo roomInfo = room.getRoomInfo();

                //if(roomInfo == null) {
                //    LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: localRoom with id={} is null, skip", roomId);
                //    continue;
                //}

                //if(!roomInfo.isPrivateRoom()) {
                    //LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: localRoom with id={} is not private, skip", roomId);
                //    continue;
                //}

                if(singleNodeRoomToStop(roomId, room)) {

                    LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: Make a pause for 5 sec before to " +
                            "proceed with 2 check for singleNodeRoomToStop value for roomId={} ", roomId);

                    Thread.sleep(Duration.ofSeconds(5).toMillis());

                    if(singleNodeRoomToStop(roomId, room)) {
                        //shutdown room
                        LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: stop local room with id={}", roomId);
                        room.shutdown();

                        //remove room from the list in IRoomService
                        LOG.debug("stopSingleNodeAnyRoomsIfNoPlayersForGameType: remove room with id={} from the roomService", roomId);
                        roomService.remove(roomId);
                    }
                }

            } catch (Exception e) {
                LOG.error("stopSingleNodeAnyRoomsIfNoPlayersForGameType: exception to process localRoom {}", roomId, e);
                return;
            }
        }
    }

    private void removeDeactivatedMultinodeAnyRoomInfos(AbstractRoomInfoService multiNodeRoomInfoService, long deactivatedRoomThresholdMs) {
        Collection<IRoomInfo> multiNodeRoomInfos = multiNodeRoomInfoService.getAllRooms();

        if(multiNodeRoomInfos == null || multiNodeRoomInfos.isEmpty()) {
            LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: multiNodeRoomInfos is null or empty, skip");
            return;
        }

        long now = System.currentTimeMillis();


        Collection<IRoomInfo> deactivatedMultiNodeRoomInfos = new ArrayList<>();

        for(IRoomInfo ri : multiNodeRoomInfos) {
            long roomDeactivatedAgoMs = now - ri.getDeactivationTime();
            LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: room with Id={}, isDeactivated={}, roomDeactivatedAgoMs={}, to remove={}",
                    ri.getId(), ri.isDeactivated(), roomDeactivatedAgoMs,  ri.isDeactivated() && roomDeactivatedAgoMs > deactivatedRoomThresholdMs);
            if(ri.isDeactivated() && roomDeactivatedAgoMs > deactivatedRoomThresholdMs) {
                deactivatedMultiNodeRoomInfos.add(ri);
            }
        }

        LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: deactivatedMultiNodeRoomInfos count={}",
                deactivatedMultiNodeRoomInfos.size());

        for(IRoomInfo deactivatedMultiNodeRoomInfo : deactivatedMultiNodeRoomInfos) {

            try {
                long roomId = deactivatedMultiNodeRoomInfo.getId();
                GameType gameType = deactivatedMultiNodeRoomInfo.getGameType();

                LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: local room with id={}, privateRoomId={}, " +
                                "ownerUsername={}, type={}", roomId, deactivatedMultiNodeRoomInfo.getPrivateRoomId(),
                        deactivatedMultiNodeRoomInfo.getOwnerUsername(), gameType.name());

                IRoom room = getRoomWithoutCreation(gameType, roomId);

                if(room != null) {
                    LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: local room with id={} is not null, " +
                            "room should be stopped first", roomId);
                } else {
                    LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: local room with id={} is null, " +
                            "continue to try to remove RoomInfo={}", roomId, deactivatedMultiNodeRoomInfo);

                    Collection<IRoomPlayerInfo> roomPlayerInfos = roomPlayerInfoService.getForRoom(roomId);
                    int roomPlayersCount = roomPlayerInfos.size();

                    LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: id={}, roomPlayersCount={}", roomId, roomPlayersCount);

                    if(roomPlayersCount > 0) {
                        for(IRoomPlayerInfo roomPlayerInfo : roomPlayerInfos) {
                            try {
                                LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: id={}, remove roomPlayerInfo={}", roomId, roomPlayerInfo);
                                roomPlayerInfoService.remove(null, roomId, roomPlayerInfo.getId());
                            } catch (Exception e) {
                                LOG.error("removeDeactivatedMultinodeAnyRoomInfos: Exception 1", e);
                            }
                        }
                    }

                    roomPlayerInfos = roomPlayerInfoService.getForRoom(roomId);
                    roomPlayersCount = roomPlayerInfos.size();

                    LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: id={}, roomPlayersCount={}", roomId, roomPlayersCount);

                    if(roomPlayersCount > 0) {
                        LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: id={}, roomPlayersCount={} is not 0, skip", roomId, roomPlayersCount);
                        continue;
                    }

                    //remove RoomInfoService records from multiNodeRoomInfoService
                    LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: remove roomInfo with id={} from private " +
                            "multiNodeRoomInfoService of type={}", roomId, gameType.name());
                    multiNodeRoomInfoService.remove(roomId);

                    //remove all seats records from multiNodeSeatService
                    LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: id={} remove all seats records " +
                            "from multiNodeSeatService", roomId);
                    multiNodeSeatService.removeAll(roomId);

                    //remove sharedGameState
                    LOG.debug("removeDeactivatedMultinodeAnyRoomInfos: remove sharedGameState with id={} from " +
                            "sharedGameStateService", roomId);
                    sharedGameStateService.remove(roomId);
                }
            } catch (Exception e) {
                LOG.error("removeDeactivatedMultinodeAnyRoomInfos: Exception 2", e);
            }
        }
    }

    private void removeDeactivatedSingleNodeAnyRoomInfos(AbstractRoomInfoService singleNodeRoomInfoService, long deactivatedRoomThresholdMs) {
        Collection<IRoomInfo> singleNodeRoomInfos = singleNodeRoomInfoService.getAllRooms();

        if(singleNodeRoomInfos == null || singleNodeRoomInfos.isEmpty()) {
            LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: singleNodeRoomInfos is null or empty, skip");
            return;
        }

        long now = System.currentTimeMillis();

        Collection<IRoomInfo> deactivatedSingleNodeRoomInfos = new ArrayList<>();

        for(IRoomInfo ri : singleNodeRoomInfos) {
            long roomDeactivatedAgoMs = now - ri.getDeactivationTime();
            LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: room with Id={}, isDeactivated={}, roomDeactivatedAgoMs={}, to remove={}",
                    ri.getId(), ri.isDeactivated(), roomDeactivatedAgoMs, ri.isDeactivated() && roomDeactivatedAgoMs > deactivatedRoomThresholdMs);
            if(ri.isDeactivated() && roomDeactivatedAgoMs > deactivatedRoomThresholdMs) {
                deactivatedSingleNodeRoomInfos.add(ri);
            }
        }

        int localServerId = serverConfigService.getServerId();
        LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: localServerId={}, deactivatedSingleNodeRoomInfos count={}",
                localServerId, deactivatedSingleNodeRoomInfos.size());

        for(IRoomInfo deactivatedSingleNodeRoomInfo : deactivatedSingleNodeRoomInfos) {

            long roomId = deactivatedSingleNodeRoomInfo.getId();
            GameType gameType = deactivatedSingleNodeRoomInfo.getGameType();

            LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: local room with id={}, " +
                            "privateRoomId={}, ownerUsername={}, type={}",
                    roomId, deactivatedSingleNodeRoomInfo.getPrivateRoomId(),
                    deactivatedSingleNodeRoomInfo.getOwnerUsername(), gameType.name());

            boolean toRemoveRoomInfo = false;

            try {

                IRoom room = getRoomWithoutCreation(gameType, roomId);

                Collection<IRoomPlayerInfo> roomPlayerInfos = roomPlayerInfoService.getForRoom(roomId);
                int roomPlayersCount = roomPlayerInfos.size();

                LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: id={}, roomPlayersCount={}", roomId, roomPlayersCount);

                if (roomPlayersCount > 0) {
                    for (IRoomPlayerInfo roomPlayerInfo : roomPlayerInfos) {
                        try {
                            LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: id={}, remove roomPlayerInfo={}", roomId, roomPlayerInfo);
                            roomPlayerInfoService.remove(null, roomId, roomPlayerInfo.getId());
                        } catch (Exception e) {
                            LOG.error("removeDeactivatedSingleNodeAnyRoomInfos: Exception 1", e);
                        }
                    }
                }

                roomPlayerInfos = roomPlayerInfoService.getForRoom(roomId);
                roomPlayersCount = roomPlayerInfos.size();

                toRemoveRoomInfo = room == null && roomPlayersCount == 0;

                LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: roomId={}, toRemoveRoomInfo={} local room " +
                        "is null={} and roomPlayersCount={}", roomId, toRemoveRoomInfo, (room == null), roomPlayersCount);

            } catch (Exception e) {
                LOG.error("removeDeactivatedSingleNodeAnyRoomInfos: Exception 2", e);
            }

            LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: id={}, toRemoveRoomInfo={}", roomId, toRemoveRoomInfo);

            if (toRemoveRoomInfo) {
                try {

                    LOG.debug("removeDeactivatedSingleNodeAnyRoomInfos: remove roomInfo with id={} from private " +
                            "singleNodeRoomInfoService of type={}", roomId, gameType.name());
                    singleNodeRoomInfoService.remove(roomId);

                } catch (Exception e) {
                    LOG.error("removeDeactivatedSingleNodeAnyRoomInfos: Exception 3", e);
                }
            }
        }
    }

    private boolean isLocked(IRoomInfoService roomInfoService, boolean singleNodeRoom, long roomId) {
        if(singleNodeRoom) {
            return roomInfoService.isLocked(roomId);
        } else {
            return sharedGameStateService.isLocked(roomId);
        }
    }

    private boolean tryLock(IRoomInfoService roomInfoService, boolean singleNodeRoom, long roomId, long timeInSeconds) throws InterruptedException {
        if(singleNodeRoom) {
            return roomInfoService.tryLock(roomId, timeInSeconds, TimeUnit.SECONDS);
        } else {
            return sharedGameStateService.tryLock(roomId, timeInSeconds, TimeUnit.SECONDS);
        }
    }

    private void unlock(IRoomInfoService roomInfoService, boolean singleNodeRoom, long roomId) {
        if (singleNodeRoom) {
            roomInfoService.unlock(roomId);
        } else {
            sharedGameStateService.unlock(roomId);
        }
    }

    @PreDestroy
    private void shutdown() {
        LOG.debug("Shutdown started");
        for (IRoomService roomService : roomServices.values()) {
            roomService.shutdown();
        }
        LOG.debug("Shutdown finished");
    }

    private void addRoomService(IRoomService roomService) {
        roomService.init();
        roomServices.put(roomService.getType(), roomService);
        LOG.debug("Added roomService for: {}", roomService.getType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public IRoom put(IRoom room) throws CommonException {
        GameType gameType = room.getRoomInfo().getGameType();
        IRoomService roomService = this.getRoomServiceWithCheck(gameType);
        return roomService.put(room);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<IRoom> getRooms(GameType gameType) throws CommonException {
        IRoomService roomService = this.getRoomServiceWithCheck(gameType);
        return roomService.getRooms();
    }

    @Override
    public IRoomInfoService getRoomInfoService(GameType gameType, Boolean isPrivate) {
        if(gameType.isSingleNodeRoomGame()) {
            return isPrivate ? bgPrivateRoomInfoService : singleNodeRoomInfoService;
        } else {
            return isPrivate ? multiNodePrivateRoomInfoService : multiNodeRoomInfoService;
        }
    }

    @Override
    public IRoom getRoom(GameType gameType, long id) throws CommonException {
        IRoomInfoService roomInfoService = this.getRoomInfoServiceByRoomId(id);
        if (roomInfoService == null) {
            LOG.error("getRoom: roomInfoService not found for id={}", id);
            throw new CommonException("IRoomInfoService not found");
        }

        IRoomInfo roomInfo = roomInfoService.getRoom(id);
        if (roomInfo == null) {
            LOG.error("getRoom: roomInfo not found for id={}", id);
            throw new CommonException("RoomInfo not found");
        }

        GameType roomInfoGameType = roomInfo.getGameType();
        if (!gameType.equals(roomInfoGameType)) {
            LOG.error("getRoom: gameType mismatch, required={}, found={}, roomInfo={}", gameType.name(), roomInfoGameType.name(), roomInfo);
            throw new CommonException("GameType mismatch, required=" + gameType.name() + ", but found=" + roomInfoGameType.name());
        }

        IRoomService roomService;
        IRoom room;

        try {
            roomService = this.getRoomServiceWithCheck(gameType);
        } catch (CommonException e) {
            LOG.error("getRoom: Cannot load RoomService for GameType={}", gameType.name(), e);
            throw new CommonException("Cannot load RoomService for GameType=" + gameType.name());
        }

        boolean locked;
        try {
            locked = this.tryLock(roomInfoService, roomInfoGameType.isSingleNodeRoomGame(), id, 2);
        } catch (Exception e) {
            LOG.error("Interrupted for room.id={}", id, e);
            return null;
        }

        if (locked) {
            try {
                room = roomService.getRoom(id);

                if (room == null) {
                    room = this.createRoom(roomService, id, roomInfoGameType.isSingleNodeRoomGame());
                } else {
                    if (room instanceof ISingleNodeRoom) {
                        ISingleNodeRoom singleNodeRoom = (ISingleNodeRoom) room;
                        if (singleNodeRoom.getGameServerId() != serverConfigService.getServerId()) {
                            throw new CommonException("Strange error, serverId mismatch. room.getGameServerId=" + singleNodeRoom.getGameServerId());
                        }
                    }
                }
            } finally {
                this.unlock(roomInfoService, roomInfoGameType.isSingleNodeRoomGame(), id);
            }
        } else { //room may be created on this server but by other thread, try just reload
            LOG.warn("getRoom: cannot lock for roomId: {}", id);
            room = roomService.getRoom(id);
        }

        return room;
    }

    /**
     * Compatibility bridge for callers migrated to ABS CommonException.
     */
    public IRoom getRoomAbs(GameType gameType, long id) throws com.abs.casino.common.exception.CommonException {
        try {
            return getRoom(gameType, id);
        } catch (CommonException e) {
            throw new com.abs.casino.common.exception.CommonException(e.getMessage(), e);
        }
    }

    @Override
    public IRoom getRoomWithoutCreation(GameType gameType, long id) throws CommonException {

        IRoom room = this.getRoomWithCheck(gameType, id);
        if (room != null) {
            if (this.isNotThisServerRoom(room)) {
                LOG.debug("getRoomWithoutCreation, room in other server, roomId: {}", id);
                return null;
            }
            LOG.debug("getRoomWithoutCreation, room found, room: {}", room);
            return room;
        }
        return null;
    }

    @Override
    public IRoom getRoomWithoutCreationById(long id) throws CommonException {
        for (GameType gameType : GameType.values()) {
            IRoomService roomService;
            try {
                roomService = this.getRoomServiceWithCheck(gameType);
            } catch (Exception e) {
                LOG.warn("getRoomWithoutCreationById: IRoomService not found for gameType={}", gameType);
                continue;
            }

            IRoom room = roomService.getRoom(id);
            if (room != null) {
                if (this.isNotThisServerRoom(room)) {
                    LOG.debug("getRoomWithoutCreationById, room in other server, roomId: {}", id);
                    return null;
                }
                LOG.debug("getRoomWithoutCreationById, room found, room: {}", room);
                return room;
            }
        }
        return null;
    }

    private boolean isNotThisServerRoom(IRoom room) {
        if (room instanceof ISingleNodeRoom) {
            ISingleNodeRoom singleNodeRoom = (ISingleNodeRoom) room;
            return singleNodeRoom.getGameServerId() != serverConfigService.getServerId();
        }
        return false;
    }

    private IRoom createRoom(IRoomService roomService, long id, boolean onlyFromSnapshot) throws ServiceNotStartedException {
        LOG.debug("createRoom: roomId={}, onlyFromSnapshot={}", id, onlyFromSnapshot);

        IRoomInfo roomInfo = this.getRoomInfo(id); //need clean load after lock
        if (roomInfo == null) { // impossible at this moment, but may be need later
            LOG.error("createRoom: roomInfo not found, room.id={}", id);
            return null;
        }

        if (roomInfo.isClosed()) {
            LOG.error("createRoom: roomInfo is closed, room.id={}", id);
            return null;
        }

        boolean isSingleNodeRoomInfo = roomInfo instanceof SingleNodeRoomInfo;
        if (isSingleNodeRoomInfo) {
            SingleNodeRoomInfo singleNodeRoomInfo = (SingleNodeRoomInfo) roomInfo;
            if (singleNodeRoomInfo.getGameServerId() == IRoomInfo.NOT_ASSIGNED_ID) {
                singleNodeRoomInfo.setGameServerId(serverConfigService.getServerId());
            } else if (singleNodeRoomInfo.getGameServerId() == serverConfigService.getServerId()) { //very rare case
                LOG.warn("createRoom: very strange, found unreachable condition, same serverId. May be global crash ? Try repair");
            } else { //placed on other server
                boolean serverIsOnline = socketServer.isOnline(singleNodeRoomInfo.getGameServerId());
                if (!serverIsOnline) {
                    LOG.info("createRoom: found room placed on down server, move to this, roomInfo.serverId={}",
                            singleNodeRoomInfo.getGameServerId());
                    singleNodeRoomInfo.setGameServerId(serverConfigService.getServerId());
                } else {
                    return null;
                }
            }
        }

        IRoomInfoService roomInfoService = this.getRoomInfoServiceByRoomId(id);

        IRoom room = roomService.newInstance(roomInfo, socketService, playerStatsPersister, playerQuestsPersister,
                weaponService, playerProfilePersister, onlyFromSnapshot, gameConfigPersister,
                activeFrbSessionService, activeCashBonusSessionService, tournamentService, roomInfoService);

        LOG.debug("createRoom: created room with id={}", room == null ? null : room.getId());

        if (room == null) {
            if (onlyFromSnapshot) { //this is normal, just return. Discard roomInfo changes
                //nop
            } else {
                LOG.error("createRoom: Cannot start roomSaved: {}", roomInfo.getId());
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        IRoom roomSaved = roomService.put(room);
        roomInfoService = this.getRoomInfoServiceByRoomId(id);
        roomInfoService.update(roomInfo);

        try {
            LOG.debug("createRoom: start roomSaved");
            roomSaved.start();
            LOG.debug("createRoom: started roomSaved");
        } catch (com.abs.casino.common.exception.CommonException e) {
            LOG.error("createRoom: Cannot start roomSaved: {}, roomInfo={}", roomSaved, roomInfo, e);
            //rollback changes
            roomService.remove(id);
            if (roomInfo instanceof SingleNodeRoomInfo) {
                ((SingleNodeRoomInfo) roomInfo).setGameServerId(IRoomInfo.NOT_ASSIGNED_ID);
            }
            roomInfoService.update(roomInfo);
            return null;
        }
        LOG.debug("createRoom: success roomSaved={}", roomSaved);
        return roomSaved;
    }

    private IRoomService getRoomServiceWithCheck(GameType gameType) throws CommonException {
        IRoomService roomService = roomServices.get(gameType);
        assertServiceExist(roomService, gameType);
        return roomService;
    }

    private IRoom getRoomWithCheck(GameType gameType, long id) throws CommonException {
        IRoomService roomService = this.getRoomServiceWithCheck(gameType);
        if(roomService == null) {
            LOG.warn("getRoomWithCheck: roomService is null for gameType {}", gameType.name() );
            return null;
        }
        return roomService.getRoom(id);
    }

    private void assertServiceExist(IRoomService service, GameType type) throws CommonException {
        if (service == null) {
            throw new CommonException("Unknown room service: " + type);
        }
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        //nop, not interested at this moment. may be later implement re-balance rooms logic ?
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        Member member = membershipEvent.getMember();
        Integer downServerId = member.getIntAttribute(ServerConfigService.MP_SERVER_ID);
        repairRoomsOnDownServer(downServerId);
    }

    @Override
    public void repairRoomsOnDownServer(Integer downServerId) {
        Collection<SingleNodeRoomInfo> downRooms = singleNodeRoomInfoService.getServerRooms(downServerId);
        LOG.debug("memberRemoved: for down serverId={} found {} rooms, try repair", downServerId, downRooms.size());
        List<SingleNodeRoomInfo> rooms = new ArrayList<>(downRooms);
        Collections.shuffle(rooms);
        startSingleNodeRooms(rooms, singleNodeRoomInfoService);
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        //nop
    }

    @Override
    public Collection<IRoom> getAllRooms() throws CommonException {
        Collection<IRoom> allRooms = new ArrayList<>();
        for(IRoomService roomService : this.roomServices.values()) {
            allRooms.addAll(roomService.getRooms());
        }
        return allRooms;
    }
    @Override
    public Collection<IRoom> getAllActiveRooms() throws CommonException {
        Collection<IRoom> allRooms = getAllRooms();
        Collection<IRoom> allActiveRooms = allRooms.stream()
                .filter(r -> RoomState.PLAY.equals(r.getState()))
                .collect(Collectors.toList());
        return allActiveRooms;
    }
    @Override
    public IServerConfigService getServerConfigService() {
        return serverConfigService;
    }

    private DeactivateRoomResultDto tryDeactivateSingleNodeRoom(long roomId) {

        SingleNodeRoomInfo roomInfo = singleNodeRoomInfoService.getRoom(roomId);

        if(roomInfo != null) {

            if (roomInfo.isDeactivated()) {
                return new DeactivateRoomResultDto(true, 200, "room is deactivated");
            }

            singleNodeRoomInfoService.lock(roomInfo.getId());

            try {
                SingleNodeRoomInfo roomInfoToUpdate = singleNodeRoomInfoService.getRoom(roomInfo.getId());
                roomInfoToUpdate.setDeactivated(true);
                singleNodeRoomInfoService.update(roomInfoToUpdate);
            } catch (Exception e) {
                LOG.error("tryDeactivateSingleNodeRoom: Unable update room info. Reason: {}", e.getMessage());
                return new DeactivateRoomResultDto(false, 400, "internal error");
            } finally {
                singleNodeRoomInfoService.unlock(roomInfo.getId());
            }

            RoomState state = roomInfo.getState();
            if (state.equals(RoomState.QUALIFY) || state.equals(RoomState.PLAY)) {

                return new DeactivateRoomResultDto(true, 200, "room will be deactivated after the end of the round");

            } else {

                ITransportObject roomWasDeactivatedMessage = new com.betsoft.casino.mp.transport.Error(ErrorCodes.ROOM_WAS_DEACTIVATED,
                        "Room was deactivated", System.currentTimeMillis(), -1);

                notifyRoomDeactivatedToAllObserversOnLocalNode(roomInfo, roomWasDeactivatedMessage);

                LOG.debug("tryDeactivateSingleNodeRoom: RoomInfo: {} was deactivated successfully", roomInfo.getId());
                return new DeactivateRoomResultDto(true, 200, "room is deactivated");
            }
        }
        return null;
    }

    private DeactivateRoomResultDto tryDeactivateSingleNodeAnyRoom(AbstractRoomInfoService singleNodeRoomInfoService, long roomId) {

        IRoomInfo roomInfo = singleNodeRoomInfoService.getRoom(roomId);

        if(roomInfo != null) {

            if (roomInfo.isDeactivated()) {
                return new DeactivateRoomResultDto(true, 200, "room is deactivated");
            }

            singleNodeRoomInfoService.lock(roomInfo.getId());

            try {
                IRoomInfo roomInfoToUpdate = singleNodeRoomInfoService.getRoom(roomInfo.getId());
                roomInfoToUpdate.setDeactivated(true);
                singleNodeRoomInfoService.update(roomInfoToUpdate);
            } catch (Exception e) {
                LOG.error("tryDeactivateSingleNodeAnyRoom: Unable update room info. Reason: {}", e.getMessage());
                return new DeactivateRoomResultDto(false, 400, "internal error");
            } finally {
                singleNodeRoomInfoService.unlock(roomInfo.getId());
            }

            RoomState state = roomInfo.getState();
            if (state.equals(RoomState.QUALIFY) || state.equals(RoomState.PLAY)) {

                return new DeactivateRoomResultDto(true, 200, "room will be deactivated after the end of the round");

            } else {

                ITransportObject roomWasDeactivatedMessage = new com.betsoft.casino.mp.transport.Error(ErrorCodes.ROOM_WAS_DEACTIVATED,
                        "Room was deactivated", System.currentTimeMillis(), -1);

                notifyRoomDeactivatedToAllObserversOnLocalNode(roomInfo, roomWasDeactivatedMessage);

                LOG.debug("tryDeactivateSingleNodeAnyRoom: RoomInfo: {} was deactivated successfully", roomInfo.getId());
                return new DeactivateRoomResultDto(true, 200, "room is deactivated");
            }
        }
        return null;
    }

    private DeactivateRoomResultDto tryDeactivateMultiNodeAnyRoom(AbstractRoomInfoService multiNodeRoomInfoService, long roomId) {

        IRoomInfo roomInfo = multiNodeRoomInfoService.getRoom(roomId);

        if(roomInfo != null) {

            if (roomInfo.isDeactivated()) {
                return new DeactivateRoomResultDto(true, 200, "room is deactivated");
            }

            multiNodeRoomInfoService.lock(roomInfo.getId());

            try {
                IRoomInfo roomInfoToUpdate = multiNodeRoomInfoService.getRoom(roomInfo.getId());
                roomInfoToUpdate.setDeactivated(true);
                multiNodeRoomInfoService.update(roomInfoToUpdate);
            } catch (Exception e) {
                LOG.error("tryDeactivateMultiNodeAnyRoom: Unable update room info. Reason: {}", e.getMessage());
                return new DeactivateRoomResultDto(false, 400, "internal error");
            } finally {
                multiNodeRoomInfoService.unlock(roomInfo.getId());
            }

            RoomState state = roomInfo.getState();
            if (state.equals(RoomState.QUALIFY) || state.equals(RoomState.PLAY)) {

                return new DeactivateRoomResultDto(true, 200, "room will be deactivated after the end of the round");

            } else {

                ITransportObject roomWasDeactivatedMessage = new Error(ErrorCodes.ROOM_WAS_DEACTIVATED,
                        "Room was deactivated", System.currentTimeMillis(), -1);

                notifyMultinodeAnyRoomDeactivatedToAllObserversOnAllNodes(multiNodeRoomInfoService, roomInfo, roomWasDeactivatedMessage);

                sitOutAllFromMultiNodeAnyRoom(roomInfo);

                LOG.debug("tryDeactivateMultiNodeAnyRoom: RoomInfo: {} was deactivated successfully", roomInfo.getId());
                return new DeactivateRoomResultDto(true, 200, "room is deactivated");
            }
        }

        return null;
    }

    private void sitOutAllFromMultiNodeAnyRoom(IRoomInfo roomInfo) {
        try {
            IRoom room = this.getRoomWithoutCreation(roomInfo.getGameType(), roomInfo.getId());
            if (room != null) {
                LOG.info("sitOutFromMultiNodePrivateRoom: Room={}", room);
                List<ISeat> seats = room.getAllSeats();

                for(ISeat seat : seats) {
                    if(seat != null) {
                        long accountId = seat.getAccountId();
                        LOG.debug("sitOutFromMultiNodePrivateRoom: call room.processSitOut for accountId: {}, " +
                                "seat: {}", accountId, seat);

                        room.processSitOut(null, null, 0, accountId, true);
                    }
                }
            }
        }  catch (com.abs.casino.common.exception.CommonException e) {
            LOG.error("sitOutFromMultiNodePrivateRoom: Cannot send message={}", toString(), e);
        }  catch (Exception e) {
            LOG.error("sitOutFromMultiNodePrivateRoom: Cannot send message={}", toString(), e);
        }
    }

    private void notifyMultinodeAnyRoomDeactivatedToAllObserversOnAllNodes(AbstractRoomInfoService multiNodeAnyRoomInfoService, IRoomInfo roomInfo, ITransportObject roomWasDeactivatedMessage ) {

        if(roomInfo != null) {
            //send roomWasDeactivatedMessage to all observers over all nodes
            SendSeatsMessageTask sendSeatsMessageTask = multiNodeAnyRoomInfoService.createSendSeatsMessageTask(
                    roomInfo.getId(),
                    roomInfo.getGameType(),
                    -1,
                    null,
                    false,
                    -1,
                    roomWasDeactivatedMessage,
                    true
            );

            LOG.debug("notifyMultinodeAnyRoomDeactivatedToAllObserversOnAllNodes: remote execute by " +
                    "multiNodeAnyRoomInfoService the sendSeatsMessageTask:{}.", sendSeatsMessageTask);

            multiNodeAnyRoomInfoService.executeOnAllMembers(sendSeatsMessageTask);
        }
    }

    private void notifyRoomDeactivatedToAllObserversOnLocalNode(IRoomInfo roomInfo, ITransportObject roomWasDeactivatedMessage ) {
        if(roomInfo != null) {
            try {
                IRoom room = this.getRoomWithoutCreation(roomInfo.getGameType(), roomInfo.getId());
                if (room == null) {
                    LOG.info("notifyRoomDeactivatedToAllObserversOnLocalNode: Room not found for={}", roomInfo);
                } else {
                    LOG.debug("notifyRoomDeactivatedToAllObserversOnLocalNode: Room found {}", room);

                    Collection<IGameSocketClient> observers = room.getObservers();
                    for (IGameSocketClient observer : observers) {
                        if (!observer.isDisconnected()) {
                            LOG.debug("notifyRoomDeactivatedToAllObserversOnLocalNode: send message {} to observer: {}", roomWasDeactivatedMessage, observer);
                            observer.sendMessage(roomWasDeactivatedMessage);
                        } else {
                            LOG.debug("notifyRoomDeactivatedToAllObserversOnLocalNode: skip observer (is disconnected): {}", observer);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("notifyRoomDeactivatedToAllObserversOnLocalNode: Cannot send message={}", toString(), e);
            }
        }
    }

    public Map<Long, RunningRoomDto> getLocalServerRunningRooms(Long gameId) throws CommonException {

        LOG.debug("getLocalServerRunningRooms: gameId={}", gameId);

        GameType[] gameTypes = new GameType[] {
                GameType.BG_MAXCRASHGAME,
                GameType.BG_DRAGONSTONE,
                GameType.BG_MISSION_AMAZON,
                GameType.BG_SECTOR_X,
                GameType.DRAGONSTONE,
                GameType.MISSION_AMAZON,
                GameType.SECTOR_X
        };

        Map<Long, RunningRoomDto> runningRoomsDtoMap = new HashMap<>();

        for(GameType gameType : gameTypes) {
            if(gameId == null || gameId == gameType.getGameId()) {
                IRoomService roomService = getRoomServiceWithCheck(gameType);
                if(roomService != null) {
                    Map<Long, IRoom> rooms = roomService.getRoomsUnmodifiableMap();
                    if(rooms != null && !rooms.isEmpty()) {
                        for(Map.Entry<Long, IRoom> roomEntry : rooms.entrySet()) {

                            Long roomId = roomEntry.getKey();
                            IRoom room = roomEntry.getValue();

                            RunningRoomDto runningRoomDto = new RunningRoomDto();
                            runningRoomDto.setRoomId(roomId);
                            runningRoomDto.setGameId((int)gameType.getGameId());

                            boolean isPrivate = room.getRoomInfo() != null && room.getRoomInfo().isPrivateRoom();
                            runningRoomDto.setPrivate(isPrivate);

                            Set<String> observers = new HashSet<>();
                            Collection<IGameSocketClient> gameSocketClients = room.getObservers();
                            if(gameSocketClients != null && !gameSocketClients.isEmpty()) {
                                observers = gameSocketClients.stream()
                                        .map(IGameSocketClient::getNickname)
                                        .collect(Collectors.toSet());
                            }
                            runningRoomDto.setObservers(observers);

                            runningRoomsDtoMap.put(roomId, runningRoomDto);
                        }
                    }
                }
            }
        }

        LOG.debug("getLocalServerRunningRooms: runningRoomsDtoMap={}", runningRoomsDtoMap);

        return runningRoomsDtoMap;
    }
}
