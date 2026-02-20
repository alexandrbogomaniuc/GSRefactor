package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.maxcrashgame.model.AbstractCrashGameRoom;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.ICrashGameSetting;
import com.betsoft.casino.mp.model.IRMSRoom;
import com.betsoft.casino.mp.model.IRoomPlayersMonitorService;
import com.betsoft.casino.mp.model.RMSPlayer;
import com.betsoft.casino.mp.model.RMSRoom;
import com.betsoft.casino.mp.model.onlineplayer.SocketClientInfo;
import com.betsoft.casino.mp.model.onlineplayer.SocketClientsStats;
import com.betsoft.casino.mp.model.onlineplayer.SocketClientsStats.SocketClientsStat;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class RoomPlayersMonitorService implements IRoomPlayersMonitorService {
    private static final Logger LOG = LogManager.getLogger(RoomPlayersMonitorService.class);

    public static final String SOCKET_CLIENT_INFO_STORE = "SocketClientInfoStore";
    public static final String SOCKET_CLIENT_STATS_STORE = "SocketClientStatsStore";

    @Value("${room.state.monitor.enabled}")
    private boolean enabled;
    @Value("${room.state.monitor.period}")
    private int timerPeriod;

    private long SOCKET_CLIENT_INFO_THRESHOLD_MS = TimeUnit.HOURS.toMillis(1);

    private ScheduledExecutorService executorService;
    private ReentrantLock lock = new ReentrantLock();
    private final ISocketService socketService;
    private IServerConfigService<ServerConfigDto> serverConfigService;
    protected final AsyncExecutorService asyncExecutorService;
    protected final IAnalyticsDBClientService analyticsDBClientService;

    int serverId;

    protected HazelcastInstance hazelcast;
    protected IMap<String, SocketClientInfo> socketClientInfos;

    protected IMap<Long, SocketClientsStats> socketClientsStats;

    public RoomPlayersMonitorService(HazelcastInstance hazelcast, IRoomServiceFactory roomServiceFactory,
            ISocketService socketService, AsyncExecutorService asyncExecutorService, IAnalyticsDBClientService analyticsDBClientService) {

        this.hazelcast = hazelcast;
        this.socketService = socketService;
        this.asyncExecutorService = asyncExecutorService;
        this.analyticsDBClientService = analyticsDBClientService;

        if(roomServiceFactory != null) {
            this.serverConfigService = roomServiceFactory.getServerConfigService();
            if(this.serverConfigService != null) {
                this.serverId = this.serverConfigService.getServerId();
            }
        }
        executorService = Executors.newScheduledThreadPool(2);
    }

    @PostConstruct
    public void init() {

        socketClientInfos = hazelcast.getMap(SOCKET_CLIENT_INFO_STORE);
        socketClientInfos.addIndex("serverId", false);
        socketClientInfos.addIndex("roomId", false);
        socketClientInfos.addIndex("gameId", false);
        socketClientInfos.addIndex("nickname", false);
        socketClientInfos.addIndex("currency", false);
        socketClientInfos.addIndex("buyInStake", false);
        socketClientInfos.addIndex("setAt", true);

        socketClientsStats = hazelcast.getMap(SOCKET_CLIENT_STATS_STORE);

        initSchedullers();
    }

    private void initSchedullers() {
        if (this.enabled) {
            // Start the timer when enabled
            LOG.debug("initScheduller: Timer is enabled. Starting timer with period {} ms", this.timerPeriod);
            this.executorService.scheduleAtFixedRate(this::timerOccurWrapper, 0, this.timerPeriod, TimeUnit.MILLISECONDS);
            this.executorService.scheduleAtFixedRate(this::timerCountWrapper, 0, 1, TimeUnit.MINUTES);

        } else {
            // Stop the timer when disabled
            LOG.debug("initScheduller: Timer is disable. Stopping timer");
            this.executorService.shutdown();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        initSchedullers();
    }

    public void socketClientInfosLock(String webSocketSessionId) {
        socketClientInfos.lock(webSocketSessionId);
    }

    public void socketClientInfosUnlock(String webSocketSessionId) {
        socketClientInfos.unlock(webSocketSessionId);
    }

    public void socketClientInfosForceUnlock(String webSocketSessionId) {
        socketClientInfos.forceUnlock(webSocketSessionId);
    }

    public boolean socketClientInfosIsLocked(String webSocketSessionId) {
        return socketClientInfos.isLocked(webSocketSessionId);
    }

    public boolean socketClientInfosTryLock(String webSocketSessionId) {
        return socketClientInfos.tryLock(webSocketSessionId);
    }

    public boolean socketClientInfosTryLock(String webSocketSessionId, long time, TimeUnit timeunit) throws InterruptedException {
        return socketClientInfos.tryLock(webSocketSessionId, time, timeunit);
    }

    public Collection<SocketClientInfo> socketClientInfosGetAll() {
        return socketClientInfos.values();
    }

    public Map<String, SocketClientInfo> getMapSocketClientInfos() {
        // Create a copy using a HashMap
        return new HashMap<>(socketClientInfos);
    }

    public NavigableMap<Long, SocketClientsStats> getSocketClientsStats() {
        return new TreeMap<>(socketClientsStats);
    }

    public void socketClientInfosRemoveOld() {

        EntryObject object = new PredicateBuilder().getEntryObject();

        long setAtThreshold = System.currentTimeMillis() - SOCKET_CLIENT_INFO_THRESHOLD_MS;
        final Predicate predicate = object.get("setAt").lessEqual(setAtThreshold);

        LOG.debug("socketClientInfosRemoveOld: setAtThreshold={}", setAtThreshold);

        Collection<SocketClientInfo> filteredSocketClientInfos = this.socketClientInfos.values(predicate);

        if(!filteredSocketClientInfos.isEmpty()) {
            LOG.debug("socketClientInfosRemoveOld: There are {} old filteredSocketClientInfos records, older {}ms",
                    filteredSocketClientInfos.size(), SOCKET_CLIENT_INFO_THRESHOLD_MS);

            for (SocketClientInfo socketClientInfo : filteredSocketClientInfos) {
                LOG.debug("socketClientInfosRemoveOld: remove socketClientInfo:{} from Hazelcast", socketClientInfo);
                this.removeSocketClientInfo(socketClientInfo.getWebSocketSessionId());
            }
        }
    }

    public void removeSocketClientInfoForAccountId(long accountId) {

        LOG.debug("removeSocketClientInfoForAccountId: accountId={}", accountId);

        EntryObject object = new PredicateBuilder().getEntryObject();

        final Predicate predicate = object.get("accountId").equal(accountId);

        Collection<SocketClientInfo> filteredSocketClientInfos = this.socketClientInfos.values(predicate);

        LOG.debug("removeSocketClientInfoForAccountId: There are {} duplicates on accountId={}",
                filteredSocketClientInfos.size(), accountId);

        if(!filteredSocketClientInfos.isEmpty()) {
            for(SocketClientInfo socketClientInfo : filteredSocketClientInfos) {
                LOG.debug("removeSocketClientInfoForAccountId: remove socketClientInfo:{} from Hazelcast",
                        socketClientInfo);
                this.removeSocketClientInfo(socketClientInfo.getWebSocketSessionId());
            }
        }
    }

    public SocketClientInfo convert(ILobbySocketClient client, long buyInStack, long roomId, String playerExternalId,
                                    boolean isOwner, int seatNr, boolean isPrivateRoom, String currency) {

        if(client == null ) {
            LOG.error("convert: client or roomInfo is null, client={}", client);
            return null;
        }

        GameType gameType = client.getGameType();

        if(gameType == null) {
            LOG.error("convert: gameType is null");
            return null;
        }

        try {

            boolean isBattleground = gameType.isBattleGroundGame();

            SocketClientInfo socketClientInfo = new SocketClientInfo();
            socketClientInfo.setWebSocketSessionId(client.getWebSocketSessionId());
            socketClientInfo.setServerId(client.getServerId());
            socketClientInfo.setRoomId(roomId);
            socketClientInfo.setGameId(gameType.getGameId());
            socketClientInfo.setGameName(gameType.name());
            socketClientInfo.setAccountId(client.getAccountId());
            socketClientInfo.setNickname(client.getNickname());
            socketClientInfo.setExternalId(playerExternalId);
            socketClientInfo.setOwner(isOwner);
            socketClientInfo.setSessionId(client.getSessionId());
            socketClientInfo.setSeatNr(seatNr);
            socketClientInfo.setPrivate(isPrivateRoom);
            socketClientInfo.setBattleGround(isBattleground);
            socketClientInfo.setBuyInStake(buyInStack);
            socketClientInfo.setCurrency(currency);
            socketClientInfo.setSetAt(System.currentTimeMillis());

            return socketClientInfo;

        } catch (Exception exception) {
            LOG.error("convert: exception: {}", exception.getMessage(), exception);
            return null;
        }
    }

    public SocketClientInfo convert(IGameSocketClient client, IRoom room, String playerExternalId) {

        if(client == null || room == null) {
            LOG.error("convert: client or roomInfo is null, client={}, room={}", client, room);
            return null;
        }

        GameType gameType = client.getGameType();

        if(gameType == null) {
            LOG.error("convert: gameType is null");
            return null;
        }

        IRoomInfo roomInfo = room.getRoomInfo();

        if(roomInfo == null) {
            LOG.error("convert: roomInfo is null");
            return null;
        }
        try {

            boolean isBattleground = roomInfo.isBattlegroundMode();
            long buyInStack = roomInfo.getStake().toCents();

            if (isBattleground && !GameType.BG_MAXCRASHGAME.equals(gameType)) {
                buyInStack = roomInfo.getBattlegroundBuyIn();
            } else if (GameType.TRIPLE_MAX_BLAST.equals(gameType) && room instanceof AbstractCrashGameRoom) {
                ICrashGameSetting crashGameSetting = ((AbstractCrashGameRoom) room).getCrashGameSetting();
                if (crashGameSetting != null) {
                    buyInStack = crashGameSetting.getMinStake();
                }
            }

            SocketClientInfo socketClientInfo = new SocketClientInfo();
            socketClientInfo.setWebSocketSessionId(client.getWebSocketSessionId());
            socketClientInfo.setServerId(client.getServerId());
            socketClientInfo.setRoomId(roomInfo.getId());
            socketClientInfo.setGameId(gameType.getGameId());
            socketClientInfo.setGameName(gameType.name());
            socketClientInfo.setAccountId(client.getAccountId());
            socketClientInfo.setNickname(client.getNickname());
            socketClientInfo.setExternalId(playerExternalId);
            socketClientInfo.setOwner(client.isOwner());
            socketClientInfo.setSessionId(client.getSessionId());
            socketClientInfo.setSeatNr(client.getSeatNumber());
            socketClientInfo.setPrivate(roomInfo.isPrivateRoom());
            socketClientInfo.setBattleGround(isBattleground);
            socketClientInfo.setBuyInStake(buyInStack);
            socketClientInfo.setCurrency(roomInfo.getCurrency());
            socketClientInfo.setSetAt(System.currentTimeMillis());

            return socketClientInfo;

        } catch (Exception exception) {
            LOG.error("convert: exception: {}", exception.getMessage(), exception);
            return null;
        }
    }

    public void upsertSocketClientInfo(SocketClientInfo socketClientInfo) {

        LOG.debug("upsertSocketClientInfo: socketClientInfo: {}", socketClientInfo);

        if(socketClientInfo == null ) {
            LOG.error("upsertSocketClientInfo: socketClientInfo is null.");
            return;
        }

        String webSocketSessionId = socketClientInfo.getWebSocketSessionId();

        if(StringUtils.isTrimmedEmpty(webSocketSessionId)) {
            LOG.error("upsertSocketClientInfo: webSocketSessionId is empty, socketClientInfo:{}.", socketClientInfo);
            return;
        }

        try {

            this.removeSocketClientInfoForAccountId(socketClientInfo.getAccountId());

            socketClientInfosLock(webSocketSessionId);

            socketClientInfos.set(webSocketSessionId, socketClientInfo);

        } catch (Exception e) {
            LOG.error("upsertSocketClientInfo: Exception:{}.", e.getMessage(), e);
        } finally {
            socketClientInfosUnlock(webSocketSessionId);
        }
    }

    public void removeSocketClientInfo(String webSocketSessionId) {

        LOG.debug("removeSocketClientInfo: remove from hazelcast element with webSocketSessionId:{}", webSocketSessionId);

        if(StringUtils.isTrimmedEmpty(webSocketSessionId)) {
            LOG.error("removeSocketClientInfo: webSocketSessionId is empty.");
            return;
        }

        try {
            socketClientInfos.remove(webSocketSessionId);
        } catch (Exception e) {
            LOG.error("removeSocketClientInfo: webSocketSessionId={}, Exception:{}.",
                    webSocketSessionId, e.getMessage(), e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getTimerPeriod() {
        return timerPeriod;
    }

    public void setTimerPeriod(int timerPeriod) {
        this.timerPeriod = timerPeriod;
    }

    private void timerOccurWrapper() {
        if (lock.tryLock()) {
            try {
                LOG.debug("timerOccurWrapper: locked.");
                timerOccur();
            } finally {
                lock.unlock();
                LOG.debug("timerOccurWrapper: unlock.");
            }
        } else {
            LOG.warn("timerOccurWrapper: Previous timerOccur is still running. Skipping this execution.");
        }
    }

    private void timerCountWrapper() {
        if (!serverConfigService.isThisIsAMaster()) {
            LOG.debug("timerCountWrapper: This is not a master node. Skipping.");
            return;
        }

        Map<Integer, Long> serverClientsCount = socketClientInfos.values().stream().collect(
                Collectors.groupingBy(SocketClientInfo::getServerId, Collectors.counting()));
        Map<Integer, ServerConfigDto> serverConfigs = serverConfigService.getConfigsMap();

        long now = Instant.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilli();

        Map<Integer, SocketClientsStat> stats =
                new HashMap<Integer, SocketClientsStats.SocketClientsStat>();

        for (Entry<Integer, Long> counts : serverClientsCount.entrySet()) {
            SocketClientsStat stat = new SocketClientsStat();
            stat.setClientsCount(counts.getValue());
            String serverIP = Optional.ofNullable(serverConfigs.get(counts.getKey()))
                    .map(ServerConfigDto::getServerIP).orElse(null);
            stat.setServerIP(serverIP);

            stats.put(counts.getKey(), stat);
        }

        for (Entry<Integer, ServerConfigDto> server : serverConfigs.entrySet()) {
            if (!stats.containsKey(server.getKey())) {
                SocketClientsStat stat = new SocketClientsStat();
                stat.setClientsCount(0L);
                String serverIP = server.getValue().getServerIP();
                stat.setServerIP(serverIP);

                stats.put(server.getKey(), stat);
            }
        }

        socketClientsStats.put(now, new SocketClientsStats(stats));
    }

    @Override
    public void finishGameSessionAndMakeSitOutAsync(int serverId, String sid, String privateRoomId) {
        LOG.debug("finishGameSessionAndMakeSitOutAsync: serverId={}, sid={}, privateRoomId={}",
                serverId, sid, privateRoomId);
        asyncExecutorService.execute(
                () -> finishGameSessionAndMakeSitOut(serverId, sid, privateRoomId)
        );
    }

    @Override
    public boolean finishGameSessionAndMakeSitOut(int serverId, String sid, String privateRoomId) {
        LOG.debug("finishGameSessionAndMakeSitOut: serverId={}, sid={}, privateRoomId={}",
                serverId, sid, privateRoomId);

        if(StringUtils.isTrimmedEmpty(sid)) {
            LOG.error("finishGameSessionAndMakeSitOut: sid is null or empty");
            return false;
        }

        boolean result = socketService.finishGameSessionAndMakeSitOut(serverId, sid, privateRoomId);

        LOG.debug("finishGameSessionAndMakeSitOut: result={}", result);

        return result;
    }

    @Override
    public void pushOnlineRoomsPlayersAsync(int serverId, List<IRMSRoom> trmsRooms) {
        LOG.debug("pushOnlineRoomsPlayersAsync: serverId:{}, trmsRooms:{}", serverId, trmsRooms);
        asyncExecutorService.execute(
                () -> pushOnlineRoomsPlayers(serverId, trmsRooms)
        );
    }

    @Override
    public boolean pushOnlineRoomsPlayers(int serverId, List<IRMSRoom> trmsRooms) {
        LOG.debug("pushOnlineRoomsPlayers: serverId={}, trmsRooms={}", serverId, trmsRooms);

        if(trmsRooms == null) {
            LOG.error("pushOnlineRoomsPlayers: trmsRooms is null");
            return false;
        }

        if(trmsRooms.isEmpty()) {
            LOG.debug("pushOnlineRoomsPlayers: trmsRooms is empty, skip pushOnlineRoomsPlayers");
            return true;
        }

        boolean pushResult = socketService.pushOnlineRoomsPlayers(trmsRooms);

        LOG.debug("pushOnlineRoomsPlayers: trmsRooms pushed={}", pushResult);

        return pushResult;
    }

    @Override
    public void saveRoomsPlayersAsync(int serverId, List<IRMSRoom> trmsRooms) {
        LOG.debug("saveRoomsPlayersAsync: serverId:{}, trmsRooms:{}", serverId, trmsRooms);
        asyncExecutorService.execute(
                () -> saveRoomsPlayers(serverId, trmsRooms)
        );
    }

    @Override
    public boolean saveRoomsPlayers(int serverId, List<IRMSRoom> trmsRooms) {
        LOG.debug("saveRoomsPlayers: serverId={}, trmsRooms={}", serverId, trmsRooms);

        if(trmsRooms == null) {
            LOG.error("saveRoomsPlayers: trmsRooms is null");
            return false;
        }

        if(trmsRooms.isEmpty()) {
            LOG.debug("saveRoomsPlayers: trmsRooms is empty, skip saveRoomsPlayers");
            return true;
        }

        List<Map<String, Object>> roomsPlayersRows
                = analyticsDBClientService.prepareRoomsPlayers(trmsRooms, serverId);

        boolean saveResult = false;
        if (roomsPlayersRows != null && !roomsPlayersRows.isEmpty()) {
            saveResult = analyticsDBClientService.saveRoomsPlayers(roomsPlayersRows);
        }

        LOG.debug("saveRoomsPlayers: trmsRooms saved={}", saveResult);

        return saveResult;
    }

    public void timerOccur() {
        LOG.info("timerOccur: Timer occurred. Performing some action...");
        long timeBegin = System.currentTimeMillis();
        try {
            if (lock.tryLock()) {
                LOG.debug("timerOccur: locked.");

                this.socketClientInfosRemoveOld();

                Collection<SocketClientInfo> socketClientInfos = this.socketClientInfosGetAll();

                if(!socketClientInfos.isEmpty()) {

                    List<IRMSRoom> trmsRooms = this.convertSocketClientInfoToTRMSRooms(socketClientInfos);

                    if(trmsRooms != null && !trmsRooms.isEmpty()) {

                        /* MQLEG-392 Turn off players room update to Canex
                        try {

                            this.pushOnlineRoomsPlayersAsync(serverId, trmsRooms);

                        } catch (Exception e) {
                            LOG.error("timerOccur: Error exception during pushOnlineRoomsPlayersAsync", e);
                        }*/

                        try {

                            this.saveRoomsPlayersAsync(serverId, trmsRooms);

                        } catch (Exception e) {
                            LOG.error("timerOccur: Error exception during saveRoomsPlayersAsync", e);
                        }
                    }

                } else {
                    LOG.debug("timerOccur: rooms is empty, nothing to push");
                }
            } else {
                LOG.warn("timerOccur: can't lock, Previous timerOccur is still running. Skipping this execution.");
            }
        } catch (Exception e) {
            LOG.error("timerOccur: Error exception", e);
        } finally {
            lock.unlock();
            LOG.debug("timerOccur: unlock, took {} ms", System.currentTimeMillis() - timeBegin);
        }
    }

    public RMSPlayer convertSocketClientInfoToTRMSPlayer(SocketClientInfo socketClientInfo) {

        LOG.debug("convertSocketClientInfoToTRMSPlayer: socketClientInfo:{}", socketClientInfo);

        if(socketClientInfo == null) {
            LOG.error("convertSocketClientInfoToTRMSPlayer: socketClientInfo is null.");
            return null;
        }

        RMSPlayer trmsPlayer = new RMSPlayer();

        trmsPlayer.setServerId(socketClientInfo.getServerId());
        trmsPlayer.setNickname(socketClientInfo.getNickname());
        trmsPlayer.setIsOwner(socketClientInfo.isOwner());
        trmsPlayer.setSessionId(socketClientInfo.getSessionId());
        trmsPlayer.setSeatNr(socketClientInfo.getSeatNr());

        LOG.debug("convertSocketClientInfoToTRMSPlayer: trmsPlayer:{}", trmsPlayer);

        return trmsPlayer;
    }

    public List<IRMSRoom> convertSocketClientInfoToTRMSRooms(Collection<SocketClientInfo> socketClientInfos) {
        if(socketClientInfos == null) {
            LOG.error("convertSocketClientInfoToTRMSRooms: socketClientInfos is null.");
            return null;
        }
        LOG.debug("convertSocketClientInfoToTRMSRooms: socketClientInfos:{}", socketClientInfos.size());

        Map<Long,IRMSRoom> trmsRooms = convertSocketClientInfoToTRMSRoomsMap(socketClientInfos);

        return new ArrayList<>(trmsRooms.values());
    }

    public Map<Long, IRMSRoom> convertSocketClientInfoToTRMSRoomsMap(Collection<SocketClientInfo> socketClientInfos) {

        if(socketClientInfos == null) {
            LOG.error("convertSocketClientInfoToTRMSRoomsMap: socketClientInfos is null.");
            return null;
        }

        LOG.debug("convertSocketClientInfoToTRMSRoomsMap: socketClientInfos:{}", socketClientInfos.size());

        Map<Long,IRMSRoom> trmsRooms = new HashMap<>();

        if(!socketClientInfos.isEmpty()) {

            for (SocketClientInfo socketClientInfo : socketClientInfos) {

                LOG.debug("convertSocketClientInfoToTRMSRoomsMap: socketClientInfo:{}", socketClientInfo);

                long roomId = socketClientInfo.getRoomId();
                IRMSRoom trmsRoom = trmsRooms.get(roomId);

                if(trmsRoom == null) {
                    RMSRoom rmsRoom = new RMSRoom();
                    rmsRoom.setRoomId(roomId);
                    rmsRoom.setServerId(socketClientInfo.getServerId());
                    rmsRoom.setIsActive(true);
                    rmsRoom.setIsBattleground(socketClientInfo.isBattleGround());
                    rmsRoom.setIsPrivate(socketClientInfo.isPrivate());
                    rmsRoom.setBuyInStake(socketClientInfo.getBuyInStake());
                    rmsRoom.setCurrency(socketClientInfo.getCurrency());
                    rmsRoom.setGameId(socketClientInfo.getGameId());
                    rmsRoom.setGameName(socketClientInfo.getGameName());
                    rmsRoom.setPlayers(new ArrayList<>());

                    trmsRoom = rmsRoom;

                    trmsRooms.put(roomId, trmsRoom);
                }

                RMSPlayer trmsPlayer = convertSocketClientInfoToTRMSPlayer(socketClientInfo);

                if(trmsPlayer != null) {
                    trmsRoom.getPlayers().add(trmsPlayer);
                }
            }
        }

        return trmsRooms;
    }

}