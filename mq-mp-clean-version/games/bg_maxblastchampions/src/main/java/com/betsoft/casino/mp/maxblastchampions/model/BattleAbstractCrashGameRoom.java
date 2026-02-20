package com.betsoft.casino.mp.maxblastchampions.model;

import com.betsoft.casino.mp.AbstractMultiNodeSeat;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.maxblastchampions.model.math.EnemyRange;
import com.betsoft.casino.mp.maxblastchampions.model.math.EnemyType;
import com.betsoft.casino.mp.maxblastchampions.model.math.config.GameConfig;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.ITransportObserver;
import com.betsoft.casino.mp.model.friends.Friend;
import com.betsoft.casino.mp.model.friends.Friends;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.onlineplayer.OnlinePlayer;
import com.betsoft.casino.mp.model.privateroom.Player;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.ICancelKickResponse;
import com.betsoft.casino.mp.model.room.IKickResponse;
import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.utils.ErrorCodes.TOO_MANY_PLAYER;
import static com.betsoft.casino.utils.TObject.SERVER_RID;
import static com.dgphoenix.casino.common.util.string.DateTimeUtils.toHumanReadableFormat;

//cannot make this class abstract for prevent Kryo serialization errors
@SuppressWarnings({"rawtypes", "unchecked"})
public class BattleAbstractCrashGameRoom extends AbstractMultiNodeGameRoom<EnemyGame, GameMap, Seat, GameRoomSnapshot,
        Enemy, EnemyType, IMultiNodeRoomInfo, ICrashGameRoomPlayerInfo>
        implements ICAFRoom,
        IRoomInfoService.IRoomInfoChangeHandler<IMultiNodeRoomInfo>,
        IPrivateRoomPlayersStatusService.IPrivateRoomChangeHandler,
        IPrivateRoomPlayersStatusService.IFriendsChangeHandler,
        IPrivateRoomPlayersStatusService.IOnlinePlayerChangeHandler {

    public static final double DEFAULT_MIN_MULTIPLIER = 1.01;
    public static final double DEFAULT_MAX_MULTIPLIER = 10000000.00;
    private final List<ITransportEnemy> possibleEnemies;
    private static final long NO_ACTIVITY_TIME = TimeUnit.SECONDS.toMillis(600);
    private static final long NO_ACTIVITY_TIME_ROUND = TimeUnit.SECONDS.toMillis(30);
    private static final long NO_ACTIVITY_TIME_ROUND_FOR_PENDING = TimeUnit.SECONDS.toMillis(90);
    private final transient ScriptEngine scriptEngine;
    private final transient ICrashGameSettingsService settingsService;
    private final IPrivateRoomPlayersStatusService privateRoomPlayersStatusService;

    private long lastUpdateTime = 0;
    private Set<Long> savedKickedPlayers = new HashSet<>();

    private String roomListenerId = null;
    private String privateRoomListenerId = null;
    private String friendsListenerId = null;
    private String onlinePlayersListenerId = null;

    private short MAX_CRASH_PRIVATE_ROOM_MIN_SEATS = 2;

    //used to sync the calls of notifyOnGameStateChanged and sendGameInfoToSocketClient
    //no parallel calls for notifyOnGameStateChanged and sendGameInfoToSocketClient should happen
    //notifyOnGameStateChanged should wait until all existing sendGameInfoToSocketClient calls finish and
    //sendGameInfoToSocketClient should wait until existing notifyOnGameStateChanged call finishes
    private final ReentrantReadWriteLock lockBetweenGameStateAndGameInfo = new ReentrantReadWriteLock();

    @SuppressWarnings("rawtypes")
    public BattleAbstractCrashGameRoom(ApplicationContext context, Logger logger, IMultiNodeRoomInfo roomInfo, GameMap map,
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

        this.privateRoomPlayersStatusService =
                context.getBean("privateRoomPlayersStatusService", IPrivateRoomPlayersStatusService.class);

        possibleEnemies = convertEnemies(EnemyType.values());
        scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        settingsService = (ICrashGameSettingsService) context.getBean("crashGameSettingsService");

        init(context);
    }

    @SuppressWarnings("rawtypes")
    public BattleAbstractCrashGameRoom(ApplicationContext context, Logger logger, IMultiNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService,
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

        this.privateRoomPlayersStatusService =
                context.getBean("privateRoomPlayersStatusService", IPrivateRoomPlayersStatusService.class);

        this.nextMapId = snapshot.getNextMapId();
        this.gameState = restoreGameState(snapshot.getGameState());
        possibleEnemies = convertEnemies(EnemyType.values());
        scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        settingsService = (ICrashGameSettingsService) context.getBean("crashGameSettingsService");

        init(context);
    }

    public String lockSendGameStateChanged() {
        UUID uuid = UUID.randomUUID();
        getLog().debug("lockSendGameStateChanged: roomId={}, uuid={}", getId(), uuid.toString());
        lockBetweenGameStateAndGameInfo.writeLock().lock();
        return uuid.toString();
    }

    public void unlockSendGameStateChanged(String uuid) {
        getLog().debug("unlockSendGameStateChanged: roomId={}, uuid={}", getId(), uuid);
        lockBetweenGameStateAndGameInfo.writeLock().unlock();
    }

    public String lockSendGameInfo() {
        UUID uuid = UUID.randomUUID();
        getLog().debug("lockSendGameInfo: roomId={}, uuid={}", getId(), uuid.toString());
        lockBetweenGameStateAndGameInfo.readLock().lock();
        return uuid.toString();
    }

    public void unlockSendGameInfo(String uuid) {
        getLog().debug("unlockSendGameInfo: roomId={}, uuid={}", getId(), uuid);
        lockBetweenGameStateAndGameInfo.readLock().unlock();
    }

    protected void init(ApplicationContext context) {

        if (roomInfo.isPrivateRoom()) {

            try {

                Environment environment = context.getEnvironment();
                String maxCrashPrivateRoomMinSeats = environment
                        .getProperty("maxblastchampions.bg.private.startround.players.min");

                this.MAX_CRASH_PRIVATE_ROOM_MIN_SEATS = Short.valueOf(maxCrashPrivateRoomMinSeats);

            } catch (Exception e) {
                getLog().error("init: error to get MAX_CRASH_PRIVATE_ROOM_MIN_SEATS from properties", e);
            }

            IRoomInfoService.RoomInfoListener roomInfoListener =
                    new IRoomInfoService.RoomInfoListener(this);
            roomListenerId = roomInfoService.registerListener(roomInfoListener);

            PrivateRoom privateRoom = getPrivateRoomPlayersStatus();
            if (privateRoom != null) {
                this.lastUpdateTime = privateRoom.getUpdateTime();
            }

            IPrivateRoomPlayersStatusService.PrivateRoomListener privateRoomListener =
                    new IPrivateRoomPlayersStatusService.PrivateRoomListener(this);
            privateRoomListenerId = privateRoomPlayersStatusService.registerPrivateRoomListener(privateRoomListener);

            IPrivateRoomPlayersStatusService.FriendsListener friendsListener =
                    new IPrivateRoomPlayersStatusService.FriendsListener(this);
            friendsListenerId = privateRoomPlayersStatusService.registerFriendsListener(friendsListener);

            IPrivateRoomPlayersStatusService.OnlinePlayersListener onlinePlayersListener =
                    new IPrivateRoomPlayersStatusService.OnlinePlayersListener(this);
            onlinePlayersListenerId = privateRoomPlayersStatusService.registerOnlinePlayersListener(onlinePlayersListener);
        }
    }

    @Override
    protected void finalize() {

        if (roomInfo.isPrivateRoom()) {

            savedKickedPlayers = new HashSet<>(roomInfo.getKickedPlayers());

            if (!StringUtils.isTrimmedEmpty(roomListenerId)) {
                roomInfoService.unregisterListener(roomListenerId);
            }

            if (!StringUtils.isTrimmedEmpty(privateRoomListenerId)) {
                privateRoomPlayersStatusService.unregisterPrivateRoomListener(privateRoomListenerId);
            }

            if (!StringUtils.isTrimmedEmpty(friendsListenerId)) {
                privateRoomPlayersStatusService.unregisterFriendsListener(friendsListenerId);
            }

            if (!StringUtils.isTrimmedEmpty(onlinePlayersListenerId)) {
                privateRoomPlayersStatusService.unregisterOnlinePlayersListener(onlinePlayersListenerId);
            }
        }
    }

    @Override
    public long getRoomId() {
        if (getRoomInfo() != null) {
            return getId();
        }
        return 0;
    }

    @Override
    public void processKickAndCancelKick(IMultiNodeRoomInfo roomInfo) {
        if (roomInfo == null) {
            getLog().error("processKickAndCancelKick: roomInfo is null");
            return;
        }

        if (!roomInfo.isPrivateRoom()) {
            getLog().error("processKickAndCancelKick: roomInfo is not isPrivateRoom: {}", roomInfo.isPrivateRoom());
            return;
        }

        if (roomInfo.getKickedPlayers() == null) {
            getLog().error("processKickAndCancelKick: roomInfo.getKickedPlayers() is null: {}", roomInfo);
            return;
        }

        if (savedKickedPlayers.equals(roomInfo.getKickedPlayers())) {
            //no incoming changes, just exit
            return;
        }

        // All account ids present in roomInfo.getKickedPlayers() but not present in kickedPlayers
        Set<Long> accountsToKick = new HashSet<>(roomInfo.getKickedPlayers());
        accountsToKick.removeAll(savedKickedPlayers);
        // All account ids present in kickedPlayers but not present in roomInfo.getKickedPlayers()
        Set<Long> accountsToCancelKick = new HashSet<>(savedKickedPlayers);
        accountsToCancelKick.removeAll(roomInfo.getKickedPlayers());

        savedKickedPlayers = new HashSet<>(roomInfo.getKickedPlayers());

        if (!accountsToKick.isEmpty()) {
            getLog().debug("processKickAndCancelKick: accountsToKick: {}", accountsToKick);
            this.processKick(accountsToKick);
        }

        if (!accountsToCancelKick.isEmpty()) {
            getLog().debug("processKickAndCancelKick: accountsToCancelKick: {}", accountsToCancelKick);
            this.processCancelKick(accountsToCancelKick);
        }
    }

    protected void processKick(Set<Long> accountIds) {

        getLog().debug("processKick: roomId:{}, accountIds {}", getId(), accountIds);

        if (accountIds == null || accountIds.isEmpty()) {
            getLog().error("processKick: roomId:{}, accountIds is empty", getId());
            return;
        }

        for (Long accountId : accountIds) {
            this.processKick(accountId);
        }
    }

    protected void processKick(Long accountId) {
        if (accountId == null || accountId == 0) {
            getLog().error("processKick: roomId:{}, accountId is empty", getId());
            return;
        }

        getLog().debug("processKick: roomId:{}, accountId {}", getId(), accountId);
        IGameSocketClient kickGameSocket = getObserver(accountId);

        String kickNickname = null;

        if (kickGameSocket != null) {
            kickNickname = kickGameSocket.getNickname();
        } else {
            getLog().debug("processKick: roomId:{}, kickGameSocket is null for accountId:{}", getId(), accountId);
        }

        if (StringUtils.isTrimmedEmpty(kickNickname)) {
            getLog().debug("processKick: kickNickname is null search nickname in PrivateRoomPlayerStatus");
            PrivateRoom privateRoom = getPrivateRoomPlayersStatus();
            if (privateRoom != null && privateRoom.getPlayers() != null && !privateRoom.getPlayers().isEmpty()) {
                Player player = privateRoom.getPlayers().stream()
                        .filter(p -> p.getAccountId() != 0 && p.getAccountId() == accountId)
                        .findFirst()
                        .orElse(null);
                if (player != null) {
                    kickNickname = player.getNickname();
                }
            }
        }

        if (StringUtils.isTrimmedEmpty(kickNickname)) {
            getLog().error("processKick: kickNickname is null exit");
            return;
        }

        getLog().debug("processKick: kickPlayer for {} ", kickNickname);
        this.kickPlayer(kickGameSocket);
    }

    public void kickPlayer(IGameSocketClient client) {
        getLog().debug("kickPLayer: client is {}", client);

        if (client != null) {
            client.setKicked(true);

            AbstractMultiNodeSeat seat = getSeatByAccountId(client.getAccountId());

            if (seat == null) {
                getLog().error("kickPlayer: seat is null, for client={}", client);
                return;
            } else {
                getLog().debug("kickPlayer: seat is {}", seat);
            }

            try {
                if (seat instanceof Seat) {
                    clearCrashBets((Seat)seat, WaitingPlayersGameState.class);
                } else {
                    getLog().debug("kickPlayer: seat is wrong type", seat);
                }
            } catch (Exception e) {
                getLog().error("kickPlayer: Cannot clearCrashBets, seat={}, room={}", seat, getId(), e);
            }
        }

        this.updateRoomInfo(newRoomInfo -> {
        });

        if (client != null) {
            sendKickResponse(client);
        }
    }

    private void sendKickResponse(IGameSocketClient socketClient) {
        IKickResponse response = getTOFactoryService()
                .createKickResponse(System.currentTimeMillis(), SERVER_RID);

        socketClient.sendMessage(response);
    }

    protected void processCancelKick(Set<Long> accountIds) {

        getLog().debug("processCancelKick: roomId:{}, accountIds {}", getId(), accountIds);

        if (accountIds == null || accountIds.isEmpty()) {
            getLog().error("processCancelKick: roomId:{}, accountIds is empty", getId());
            return;
        }

        for (Long accountId : accountIds) {
            this.processCancelKick(accountId);
        }
    }

    protected void processCancelKick(Long accountId) {
        if (accountId == null || accountId == 0) {
            getLog().error("processCancelKick: roomId:{}, accountId is empty", getId());
            return;
        }

        IGameSocketClient cancelKickGameSocket = getObserver(accountId);

        getLog().debug("processCancelKick: roomId:{}, accountId={}, cancelKickGameSocket={}",
                getId(), accountId, cancelKickGameSocket);

        String cancelKickNickname = null;
        Status status = null;

        if (cancelKickGameSocket != null && !cancelKickGameSocket.isDisconnected()) {
            cancelKickNickname = cancelKickGameSocket.getNickname();
            status = Status.WAITING;
        } else {
            getLog().debug("processCancelKick: roomId:{}, cancelKickGameSocket is null or disconnected " +
                    "for accountId:{}", getId(), accountId);
        }

        if (StringUtils.isTrimmedEmpty(cancelKickNickname)) {
            getLog().debug("processCancelKick: cancelKickNickname is null search nickname in PrivateRoomPlayerStatus");
            PrivateRoom privateRoom = getPrivateRoomPlayersStatus();
            if (privateRoom != null && privateRoom.getPlayers() != null && !privateRoom.getPlayers().isEmpty()) {
                Player player = privateRoom.getPlayers().stream()
                        .filter(p -> p.getAccountId() != 0 && p.getAccountId() == accountId)
                        .findFirst()
                        .orElse(null);
                if (player != null) {
                    cancelKickNickname = player.getNickname();
                }
            }
        }

        if (StringUtils.isTrimmedEmpty(cancelKickNickname)) {
            getLog().error("processCancelKick: cancelKickNickname is null exit");
            return;
        }

        this.cancelKickPlayer(cancelKickGameSocket);

        if (status != null) {

            try {
                getLog().debug("processCancelKick: updatePlayersStatusAndSendToOwner to {} for {} ",
                        status, cancelKickNickname);

                UpdatePrivateRoomResponse updatePrivateRoomResponse =
                        updatePlayersStatusNicknamesOnly(Arrays.asList(cancelKickNickname), status, false, true);

                getLog().debug("processCancelKick: updatePrivateRoomResponse {}", updatePrivateRoomResponse);

                if (updatePrivateRoomResponse == null) {
                    getLog().error("processCancelKick: updatePrivateRoomResponse is null for {}", cancelKickNickname);
                    return;
                }

                if (updatePrivateRoomResponse.getPrivateRoom() == null) {
                    getLog().error("processCancelKick: updatePrivateRoomResponse.getPrivateRoom() is null " +
                            "for {}", cancelKickNickname);
                    return;
                }

                if (StringUtils.isTrimmedEmpty(updatePrivateRoomResponse.getPrivateRoom().getPrivateRoomId())) {
                    getLog().error("processCancelKick: updatePrivateRoomResponse.getPrivateRoom().getPrivateRoomId() " +
                            "is empty for {}", cancelKickNickname);
                    return;
                }

                if (updatePrivateRoomResponse.getPrivateRoom().getPlayers() == null
                        || updatePrivateRoomResponse.getPrivateRoom().getPlayers().isEmpty()) {
                    getLog().error("processCancelKick: updatePrivateRoomResponse.getPrivateRoom().getPlayers() is empty " +
                            "for {}", cancelKickNickname);
                    return;
                }

            } catch (Exception e) {
                getLog().error("processCancelKick: Exception to updatePlayersStatusAndSentToOwner by " +
                        "List<IGameSocketClient>, {}", e.getMessage(), e);
            }

            try {
                String privateRoomId = getPrivateRoomId();

                getLog().debug("processCancelKick: privateRoomId:{}, status:{}, cancelKickNickname:{}",
                        privateRoomId, status, cancelKickNickname);

                this.sendPlayerStatusInPrivateRoomToCanex(
                        privateRoomId, serverConfigService.getServerId(), (int) roomInfo.getBankId(),
                        cancelKickNickname, null, accountId, status);

            } catch (Exception e) {
                getLog().error("processCancelKick: Exception to sendPlayerStatusInPrivateRoomToCanex, {}",
                        e.getMessage(), e);
            }
        }
    }

    public void cancelKickPlayer(IGameSocketClient client) {
        getLog().debug("cancelKickPlayer: client is {}", client);

        if (client != null) {
            client.setKicked(false);
        }

        this.updateRoomInfo(newRoomInfo -> {
        });

        if (client != null) {
            sendCancelKickResponse(client);
        }
    }

    private void sendCancelKickResponse(IGameSocketClient socketClient) {
        ICancelKickResponse response = getTOFactoryService()
                .createCancelKickResponse(System.currentTimeMillis(), SERVER_RID);

        socketClient.sendMessage(response);
    }

    @Override
    public short getRealConfirmedSeatsCount() {
        short count = 0;

        for (Seat seat : getAllSeats()) {
            if (seat == null || seat.isSitOutStarted()) {
                continue;
            }
            Map<String, ICrashBetInfo> crashBets = seat.getCrashBets();
            boolean isPendingOperation = seat.getPlayerInfo() == null ? false : seat.getPlayerInfo().isPendingOperation();

            if (crashBets != null && !crashBets.isEmpty() && !isPendingOperation) {
                count++;
            }
        }

        return count;
    }

    @Override
    public void clearKickedPlayers() {

        boolean isPrivateRoom = roomInfo.isPrivateRoom();
        long roomId = getId();

        if (!isPrivateRoom) {
            getLog().debug("clearKickedPlayers: room is not private room id:{}", roomId);
            return;
        }

        getLog().debug("clearKickedPlayers: room id:{}", roomId);

        roomInfoService.lock(roomId);
        try {
            roomInfo.clearKickedPlayers();
            roomInfoService.update(roomInfo);
        } finally {
            roomInfoService.unlock(roomId);
        }
    }

    @Override
    public short getMinSeats() {

        IRoomInfo roomInfo = getRoomInfo();
        boolean isPrivateRoom = roomInfo != null ? roomInfo.isPrivateRoom() : false;

        return isPrivateRoom ?
                MAX_CRASH_PRIVATE_ROOM_MIN_SEATS :
                super.getMinSeats();
    }

    @Override
    public boolean isBattlegroundMode() {

        IRoomInfo roomInfo = getRoomInfo();
        boolean isPrivateRoom = roomInfo != null ? roomInfo.isPrivateRoom() : false;

        return isPrivateRoom ?
                true :
                super.isBattlegroundMode();
    }

    @Override
    public String getOwnerExternalId() {

        if (privateRoomPlayersStatusService != null) {
            return privateRoomPlayersStatusService.getOwnerExternalId(roomInfo);
        }

        return null;
    }

    @Override
    public void processPrivateRoomChange(PrivateRoom privateRoom) {

        if (privateRoom != null) {
            //if there are any recorded updates to privateRoom object
            //update room owner with changes
            long updateTime = privateRoom.getUpdateTime();
            if (updateTime > lastUpdateTime) {
                lastUpdateTime = updateTime;
                sendGameInfoToAllLocalNodeObservers();
            }
        }
    }

    @Override
    public void processFriendsChange(Friends friends) {

        sendGameInfoToAllLocalNodeObservers();

        //retrieve onlineStatus for friends
        if (friends == null) {
            getLog().error("processFriendsChange: friends is null");
            return;
        }

        if (friends.getFriends() == null || friends.getFriends().isEmpty()) {
            getLog().error("processFriendsChange: friends Map is empty, friends:{}", friends);
            return;
        }

        if (serverConfigService != null) {
            int serverId = serverConfigService.getServerId();
            privateRoomPlayersStatusService
                    .getOnlineStatusFromCanexAsync(serverId, friends.getFriends().values());
        }
    }

    @Override
    public Set<String> getOnlinePlayerExternalIds() {

        if (roomInfo == null) {
            getLog().debug("getOnlinePlayerExternalIds: roomInfo is null");
            return null;
        }

        Map<String, Friend> friendsNotInPrivateRoom =
                privateRoomPlayersStatusService.getFriendsNotInPrivateRoom(roomInfo);

        long roomId = roomInfo.getId();

        if (friendsNotInPrivateRoom == null) {
            getLog().error("getOnlinePlayerExternalIds: roomId={}, friendsNotInPrivateRoom is null", roomId);
            return new HashSet<>();
        }

        Set<String> externalIds = friendsNotInPrivateRoom.keySet();

        getLog().debug("getOnlinePlayerExternalIds: roomId={}, externalIds to return: {}", roomId, externalIds);

        return externalIds;
    }

    @Override
    public void processOnlinePlayerChange(OnlinePlayer onlinePlayer) {
        sendGameInfoToAllLocalNodeObservers();
    }

    @Override
    public PrivateRoom getPrivateRoomPlayersStatus() {

        if (privateRoomPlayersStatusService != null) {
            return privateRoomPlayersStatusService.getPrivateRoomPlayersStatus(roomInfo);
        }

        return null;
    }

    @Override
    public String getPrivateRoomId() {
        if (privateRoomPlayersStatusService != null) {
            return privateRoomPlayersStatusService.getPrivateRoomId(this, roomInfo);
        }

        return null;
    }

    @Override
    public UpdatePrivateRoomResponse updatePlayersStatusAndSendToOwnerNicknamesOnly(List<String> nicknames, Status status) {
        if (isBattlegroundMode()) {

            IRoomInfo roomInfo = getRoomInfo();

            if (roomInfo == null) {
                getLog().error("updatePlayersStatusAndSendToOwner: roomInfo is null, for roomId: {}", getId());
                return null;
            }

            if (roomInfo.isPrivateRoom()) {

                UpdatePrivateRoomResponse response =
                        updatePlayersStatusNicknamesOnly(nicknames, status, false, false);

                sendGameInfoToOwner();

                return response;
            }
        }

        return null;
    }

    @Override
    public UpdatePrivateRoomResponse updatePlayersStatusNicknamesOnly(List<String> nicknames, Status status,
                                                                      boolean isTransitionLimited, boolean updateTime) {

        if (nicknames == null || nicknames.isEmpty()) {
            getLog().error("updatePlayersStatusNicknamesOnly: nicknames is null or empty, for roomId: {}", getId());
            return null;
        }

        if (privateRoomPlayersStatusService == null) {
            getLog().error("updatePlayersStatusNicknamesOnly: privateRoomPlayersStatusService is null, " +
                    "for roomId: {}", getId());
            return null;
        }

        IRoomInfo roomInfo = getRoomInfo();

        if (roomInfo == null) {
            getLog().error("updatePlayersStatusNicknamesOnly: roomInfo is null, for roomId: {}", getId());
            return null;
        }

        if (!roomInfo.isPrivateRoom()) {
            getLog().error("updatePlayersStatusNicknamesOnly: roomInfo is is not private, for roomId: {}", getId());
            return null;
        }

        String privateRoomId = getPrivateRoomId();

        if (StringUtils.isTrimmedEmpty(privateRoomId)) {
            getLog().error("updatePlayersStatusNicknamesOnly: privateRoomId is empty for private room:{}", getId());
            return null;
        }

        List<Player> players = new ArrayList<>();

        for (String nickname : nicknames) {
            Player player = new Player(nickname, 0, null, status);
            players.add(player);
        }

        long oldUpdateTime = 0;
        PrivateRoom oldPrivateRoom = privateRoomPlayersStatusService.getPrivateRoom(privateRoomId);
        if (oldPrivateRoom != null) {
            oldUpdateTime = oldPrivateRoom.getUpdateTime();
        }

        PrivateRoom privateRoom =
                new PrivateRoom(getId(), privateRoomId, 0, null, null, players, oldUpdateTime);

        UpdatePrivateRoomResponse response = privateRoomPlayersStatusService
                .updatePlayersStatusInPrivateRoom(privateRoom, isTransitionLimited, updateTime);

        getLog().debug("updatePlayersStatusNicknamesOnly: roomId: {}, UpdatePrivateRoomResponse is {}, ", getId(), response);

        return response;
    }

    @Override
    public UpdatePrivateRoomResponse updatePlayersStatusAndSendToOwner(List<IGameSocketClient> clients, Status status) {
        if (isBattlegroundMode()) {

            IRoomInfo roomInfo = getRoomInfo();

            if (roomInfo == null) {
                getLog().error("updatePlayersStatusAndSendToOwner: roomInfo is null, for roomId: {}", getId());
                return null;
            }

            if (roomInfo.isPrivateRoom()) {

                UpdatePrivateRoomResponse response =
                        updatePlayersStatus(clients, status, false, false);

                sendGameInfoToOwner();

                return response;
            }
        }

        return null;
    }

    @Override
    public void updatePlayerStatusForObserver(String nickname, Status status) {

        getLog().debug("updatePlayerStatusForObserver: nickname={}, status={}", nickname, status);

        if (StringUtils.isTrimmedEmpty(nickname)) {
            getLog().error("updatePlayerStatusForObserver: nickname is null or empty");
            return;
        }

        IRoomInfo roomInfo = getRoomInfo();

        if (roomInfo == null) {
            getLog().error("updatePlayerStatusForObserver: roomInfo is null, for roomId: {}", getId());
            return;
        }

        if (!roomInfo.isPrivateRoom()) {
            getLog().error("updatePlayerStatusForObserver: room is not Private: {}", roomInfo);
            return;
        }

        for (IGameSocketClient observer : getObservers()) {
            if (observer != null && observer.getNickname() != null && observer.getNickname().equals(nickname)) {
                updatePlayersStatus(Arrays.asList(observer), status, false, true);
                break;
            }
        }
    }

    @Override
    public UpdatePrivateRoomResponse updatePlayersStatus(List<IGameSocketClient> clients, Status status, boolean isTransitionLimited,
                                                         boolean updateTime) {

        if (clients == null || clients.isEmpty()) {
            getLog().error("updatePlayersStatus: clients is null or empty, for roomId: {}", getId());
            return null;
        }

        if (privateRoomPlayersStatusService == null) {
            getLog().error("updatePlayersStatus: privateRoomPlayersStatusService is null, " +
                    "for roomId: {}", getId());
            return null;
        }

        String privateRoomId = getPrivateRoomId();

        if (StringUtils.isTrimmedEmpty(privateRoomId)) {
            getLog().error("updatePlayersStatus: privateRoomId is empty for private room:{}",
                    getId());
            return null;
        }

        List<Player> players = new ArrayList<>();

        for (IGameSocketClient client : clients) {
            Player player = new Player(client.getNickname(), client.getAccountId(), null, client.isKicked() ? Status.KICKED : status);
            players.add(player);
        }

        long oldUpdateTime = 0;
        PrivateRoom oldPrivateRoom = privateRoomPlayersStatusService.getPrivateRoom(privateRoomId);
        if (oldPrivateRoom != null) {
            oldUpdateTime = oldPrivateRoom.getUpdateTime();
        }

        PrivateRoom privateRoom =
                new PrivateRoom(getId(), privateRoomId, 0, null, null, players, oldUpdateTime);

        UpdatePrivateRoomResponse response = privateRoomPlayersStatusService
                .updatePlayersStatusInPrivateRoom(privateRoom, isTransitionLimited, updateTime);

        getLog().debug("updatePlayersStatus: roomId: {}, UpdatePrivateRoomResponse is {}, ", getId(), response);

        return response;
    }

    @Override
    public void sendPlayerStatusInPrivateRoomToCanex(String privateRoomId,
                                                     int serverId,
                                                     int bankId,
                                                     String nickname,
                                                     String externalId,
                                                     Long accountId,
                                                     Status tbgStatus) {

        getLog().debug("sendPlayerStatusInPrivateRoomToCanex: roomId:{}, privateRoomId:{}, serverId:{}, bankId:{}, " +
                        "nickname:{}, externalId:{}, accountId:{}, tbgStatus:{}",
                getId(), privateRoomId, serverId, bankId, nickname, externalId, accountId, tbgStatus);

        if (serverConfigService != null) {
            if (serverId == 0) {
                serverId = serverConfigService.getServerId();
            }
        }

        if (this.getRoomInfo() != null) {
            if (bankId == 0) {
                bankId = (int) this.getRoomInfo().getBankId();
            }
        } else {
            getLog().error("sendPlayerStatusInPrivateRoomToCanex: this.getRoomInfo() is null");
            return;
        }

        privateRoomPlayersStatusService.sendPlayerStatusInPrivateRoomToCanex(serverId,
                bankId, privateRoomId, nickname, externalId, accountId == null ? 0 : accountId.longValue(), tbgStatus);
    }

    @Override
    public void sendPlayerStatusInPrivateRoomToCanex(String privateRoomId,
                                                     IGameSocketClient gameSocketClient,
                                                     String externalId,
                                                     Status tbgStatus) {

        getLog().debug("sendPlayerStatusInPrivateRoomToCanex: roomId:{}, privateRoomId:{}, gameSocketClient:{}, " +
                "externalId:{}, tbgStatus:{}", getId(), privateRoomId, gameSocketClient, externalId, tbgStatus);

        if (StringUtils.isTrimmedEmpty(privateRoomId)) {
            getLog().warn("sendPlayerStatusInPrivateRoomToCanex: privateRoomId is empty, for roomId: {}," +
                    " client:{}", getId(), gameSocketClient);
            privateRoomId = getPrivateRoomId();
        }

        if (StringUtils.isTrimmedEmpty(privateRoomId)) {
            getLog().error("sendPlayerStatusInPrivateRoomToCanex: privateRoomId is empty, for roomId: {}," +
                    " client:{}", getId(), gameSocketClient);
            return;
        }

        if (gameSocketClient == null) {
            getLog().error("sendPlayerStatusInPrivateRoomToCanex: gameSocketClient is null, for roomId: {}," +
                    " privateRoomId:{}", getId(), privateRoomId);
            return;
        }

        int serverId = gameSocketClient.getServerId();

        int bankId = gameSocketClient.getBankId() == null ?
                0 :
                gameSocketClient.getBankId().intValue();

        String nickname = gameSocketClient.getNickname();
        long accountId = gameSocketClient.getAccountId() == null ? 0 : gameSocketClient.getAccountId().longValue();

        privateRoomPlayersStatusService.sendPlayerStatusInPrivateRoomToCanex(serverId,
                bankId, privateRoomId, nickname, externalId, accountId, tbgStatus);
    }

    @Override
    public boolean invitePlayersToPrivateRoomAtCanex(List<String> nicknames) {
        getLog().debug("invitePlayersToPrivateRoomAtCanex: nicknames:{}", nicknames);

        if (nicknames == null || nicknames.isEmpty()) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: list nicknames is empty:{}", nicknames);
            return false;
        }

        if (!getRoomInfo().isPrivateRoom()) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: room is not private:{}, skip", getId());
            return false;
        }

        if (StringUtils.isTrimmedEmpty(getRoomInfo().getPrivateRoomId())) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: getRoomInfo().getPrivateRoomId() is empty:{}, skip", getId());
            return false;
        }

        int serverId = serverConfigService.getServerId();

        PrivateRoom privateRoom = getPrivateRoomPlayersStatus();

        if (privateRoom == null) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: privateRoom is null for:{} skip", getId());
            return false;
        }

        if (StringUtils.isTrimmedEmpty(privateRoom.getOwnerExternalId())) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: privateRoom.getOwnerExternalId() is empty for:{}, " +
                    "{} skip", getId(), privateRoom);
            return false;
        }

        Friends friends = privateRoomPlayersStatusService.getFriendsForExternalId(privateRoom.getOwnerExternalId());

        if (friends == null) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: friends is null:{}, " +
                    "OwnerExternalId:{} skip", getId(), privateRoom.getOwnerExternalId());
            return false;
        }

        if (friends.getFriends() == null || friends.getFriends().isEmpty()) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: friends.getFriends() is empty:{}, " +
                    "OwnerExternalId:{} skip", friends, privateRoom.getOwnerExternalId());
            return false;
        }

        Set<Friend> friendsSet = friends.getFriends().values().stream()
                .filter(f -> !StringUtils.isTrimmedEmpty(f.getExternalId())
                        && !StringUtils.isTrimmedEmpty(f.getNickname())
                        && nicknames.contains(f.getNickname()))
                .collect(Collectors.toSet());

        if (friendsSet == null || friendsSet.isEmpty()) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: friendsSet is empty:{}, " +
                    "OwnerExternalId:{} skip", friends, privateRoom.getOwnerExternalId());
            return false;
        }

        return privateRoomPlayersStatusService.invitePlayersToPrivateRoomAtCanex(serverId, friendsSet,
                getRoomInfo().getPrivateRoomId());
    }

    @Override
    protected void notifyOnGameStateChanged(IGameState newState,
                                            RoomState oldRoomState,
                                            long timeToNextState,
                                            long roundId) {

        long startTime = System.currentTimeMillis();
        long roomId = this.getId();
        RoomState newRoomState = newState.getRoomState();

        IMultiNodeRoomInfo roomInfo = this.getRoomInfo();
        boolean isPrivateRoom = roomInfo != null && roomInfo.isPrivateRoom();

        String uuid = lockSendGameStateChanged();  // <- Waits until all SendGameInfo locks are released
        try {

            if (isPrivateRoom) {
                getLog().debug("notifyOnGameStateChanged: roomId={}, roundId={}, stacktrace={}",
                        roomId, roundId, Thread.currentThread().getStackTrace());
            }

            IGameStateChanged gameStateChanged = getTOFactoryService().createGameStateChanged(
                    getCurrentTime(),
                    newState.getRoomState(),
                    timeToNextState,
                    roundId,
                    getRoundStartTime(newState)
            );

            sendChanges(gameStateChanged);

        } finally {
            unlockSendGameStateChanged(uuid);  // Always release
        }

        for (IRoomStateChangedListener listener : stateChangedListeners) {
            listener.notifyStateChanged(this, oldRoomState, newState.getRoomState());
        }


        getLog().debug("notifyOnGameStateChanged: roomId={}, roundId={}, timeToNextState={}, oldRoomState={}, newRoomState={}, duration={}ms",
                roomId, roundId, timeToNextState, oldRoomState, newRoomState, System.currentTimeMillis() - startTime);
    }

    @Override
    public void sendGameInfoToSocketClient(IGameSocketClient socketClient) {
        if (socketClient != null) {
            String uuid = lockSendGameInfo();  // <- Waits until all GameStateChanged locks are released
            try {
                ITransportObject fullGameInfo = getFullGameInfo(null, socketClient);
                socketClient.sendMessage(fullGameInfo);
            } catch (Exception e) {
                getLog().error("sendGameInfoToSocketClient: exception, {}", e.getMessage(), e);
            } finally {
                unlockSendGameInfo(uuid);
            }
        } else {
            getLog().error("sendGameInfoToSocketClient: socketClient is null");
        }
    }

    @Override
    public void sendGameInfoToAllObservers() {

        sendGameInfoToAllLocalNodeObservers();

    }

    protected void sendGameInfoToAllLocalNodeObservers() {
        Collection<IGameSocketClient> roomNodeObservers = getObservers();

        if (roomNodeObservers != null && !roomNodeObservers.isEmpty()) {

            for (IGameSocketClient observer : roomNodeObservers) {
                sendGameInfoToSocketClient(observer);
            }
        } else {
            getLog().warn("sendGameInfoToAllLocalNodeObservers: no observers are present in room:{}", getId());
        }
    }

    @Override
    public void sendGameInfoToOwner() {
        IGameSocketClient ownerSocketClient = getOwnerClient();
        if (ownerSocketClient != null) {
            sendGameInfoToSocketClient(ownerSocketClient);
        }
    }

    @Override
    public IGameSocketClient getOwnerClient() {
        for (IGameSocketClient observer : getObservers()) {
            if (observer != null && observer.isOwner()) {
                return observer;
            }
        }
        return null;
    }

    private IGameState restoreGameState(IGameState snapshotGameState) {

        SharedCrashGameState sharedCrashGameState = this.getSharedGameStateService()
                .get(getId(), SharedCrashGameState.class);

        if (sharedCrashGameState == null) {
            getLog().debug("restoreGameState: SharedCrashGameState not found, restore from snapshot");
            return snapshotGameState;
        }

        if (snapshotGameState.getRoomState() == sharedCrashGameState.getState()) {
            getLog().debug("restoreGameState: SharedCrashGameState found and state equals, restore from snapshot");
            return snapshotGameState;
        }

        IGameState gameState = this.getGameState(sharedCrashGameState);

        return gameState;
    }

    private IGameState getGameState(SharedCrashGameState sharedCrashGameState) {

        IGameState gameState = null;

        if (sharedCrashGameState.getState() == RoomState.PLAY) {

            gameState = new PlayGameState(this);

        } else if (sharedCrashGameState.getState() == RoomState.QUALIFY) {

            int lastUsedMapId = getMapId();
            int pauseTime = 3000;
            long roundTimeStart = sharedCrashGameState.getRoundStartTime();
            long roundTimeEnd = sharedCrashGameState.getRoundEndTime();

            gameState = new QualifyGameState(this, lastUsedMapId, pauseTime, roundTimeStart, roundTimeEnd);

        } else {

            gameState = getWaitingPlayersGameState();

        }
        return gameState;
    }

    @Override
    public boolean shutdownRoomIfEmpty() {
        return false;
    }

    public boolean isValidMultiplier(double multiplier) {
        boolean isValidMultiplier = multiplier >= DEFAULT_MIN_MULTIPLIER && multiplier <= DEFAULT_MAX_MULTIPLIER;
        return isValidMultiplier;
    }

    @Override
    protected WaitingPlayersGameState getWaitingPlayersGameState() {

        IRoomInfo roomInfo = getRoomInfo();

        if (roomInfo == null) {
            getLog().error("getWaitingPlayersGameState: roomInfo is null, for roomId: {}", getId());
            new WaitingPlayersGameState(this);
        }

        boolean isPrivateRoom = roomInfo.isPrivateRoom();

        WaitingPlayersGameState waitingPlayersGameState = isPrivateRoom ?
                new PrivateBTGWaitingGameState(this) :
                new WaitingPlayersGameState(this);

        return waitingPlayersGameState;
    }

    void setPossibleEnemies(EnemyRange possibleEnemies) {
        GameMap gameMap = this.getMap();
        gameMap.setPossibleEnemies(possibleEnemies);
    }

    @Override
    public GameRoomSnapshot getSnapshot() {
        long id = this.getId();
        IMultiNodeRoomInfo roomInfo = this.getRoomInfo();
        long roundId = roomInfo.getRoundId();
        Seat[] seats = getSeats().toArray(new Seat[0]);
        GameMap map = getMap();

        GameRoomSnapshot gameRoomSnapshot = new GameRoomSnapshot(id, roundId, seats, map, nextMapId, gameState);
        return gameRoomSnapshot;
    }

    @Override
    public Seat createSeat(ICrashGameRoomPlayerInfo playerInfo, IGameSocketClient socketClient, double currentRate) {
        GameType gameType = this.getGameType();
        long gameId = gameType.getGameId();

        Seat seat = new Seat(playerInfo, socketClient, currentRate, gameId);

        PlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
        IMultiNodeRoomInfo roomInfo = this.getRoomInfo();
        long roundId = roomInfo.getRoundId();
        currentPlayerRoundInfo.setRoomRoundId(roundId);

        return seat;
    }

    public void clearCrashBets(Seat seat, Class<?> gameStateClass) {
        if (seat == null) {
            getLog().debug("clearCrashBets: roomId={}, gameStateClass={}, seat is null, skip", getRoomId(), gameStateClass);
            return;
        }

        if (seat.getSocketClient() == null) {
            getLog().debug("clearCrashBets: roomId={}, gameStateClass={}, seat.getSocketClient() == null, skip", getRoomId(), gameStateClass);
            return;
        }
        Map<String, ICrashBetInfo> crashBets = seat.getCrashBets();

        getLog().debug("clearCrashBets: roomId={}, gameStateClass={}, accountId={}, crashBets={}",
                getRoomId(), gameStateClass, seat.getAccountId(), crashBets);
        
        if (!crashBets.isEmpty()) {

            for (String crashBetId : crashBets.keySet()) {

                if (gameStateClass == null || gameStateClass.isInstance(gameState)) {

                    gameState.processCancelCrashMultiplier(
                            seat.getAccountId(),
                            crashBetId,
                            -1,
                            false,
                            null
                    );
                }
            }
        }
    }

    @Override
    public Seat processSitOut(IGameSocketClient client, ISitOut request, int seatNumber, long accountId,
                              boolean updateStats) {
        return processSitOut(client, request, seatNumber, accountId, updateStats, false);
    }

    @Override
    public Seat processSitOut(IGameSocketClient client, ISitOut request, int seatNumber, long accountId,
                              boolean updateStats, boolean bulletsConvertedToMoney) {

        getLog().debug("processSitOut: roomId={}, accountId={}, bulletsConvertedToMoney={}, client={}, request={}, " +
                        "seatNumber={}, updateStats={}",
                getRoomId(), accountId, bulletsConvertedToMoney, client, request, seatNumber, updateStats);

        Seat seat = this.getSeatByAccountId(accountId);

        getLog().debug("processSitOut: roomId={}, accountId={}, seat={}", getRoomId(), accountId, seat);

        clearCrashBets(seat, null);

        seat = super.processSitOut(client, request, seatNumber, accountId, updateStats, bulletsConvertedToMoney);

        this.sendGameInfoToAllObservers();

        return seat;
    }

    ICrashGameSetting getCrashGameSetting() {
        GameType gameType = this.getGameType();
        long gameId = gameType.getGameId();

        IMultiNodeRoomInfo roomInfo = this.getRoomInfo();
        long bankId = roomInfo.getBankId();
        String currency = roomInfo.getCurrency();

        ICrashGameSetting crashGameSetting = settingsService.getSettings(bankId, gameId, currency);
        return crashGameSetting;
    }

    ICurrencyRateService getCurrencyRateService() {
        return currencyRateService;
    }

    @Override
    public int getAllowedPlayers() {
        ICrashGameSetting crashGameSetting = this.getCrashGameSetting();
        int maxRoomPlayers = crashGameSetting.getMaxRoomPlayers();
        return maxRoomPlayers;
    }

    @Override
    public int processSitIn(Seat seat, ISitIn request) throws CommonException {
        if (this.isRoomFull()) {
            return TOO_MANY_PLAYER;
        }
        int result = super.processSitIn(seat, request);

        this.sendGameInfoToAllObservers();

        return result;
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

        Trajectory enemyTrajectory = enemy.getTrajectory();

        Trajectory trajectory = null;
        if(fillTrajectory) {
            trajectory = EnemyType.ROCKET.equals(enemyType)
                    ? this.convertFullTrajectory(enemyTrajectory)
                    : this.convertTrajectory(enemyTrajectory, System.currentTimeMillis());
        }

        ITransportObjectsFactoryService toFactoryService = getTOFactoryService();

        double enemyHP = this.getHP(enemy);

        IRoomEnemy roomEnemy = toFactoryService.createRoomEnemy(
                enemy.getId(),
                enemyType.getId(),
                enemyType.isBoss(),
                enemy.getSpeed(),
                enemy.getAwardedPrizesAsString(),
                enemy.getAwardedSum().toDoubleCents(),
                enemyHP,
                enemy.getSkin(),
                trajectory,
                enemy.getParentEnemyId(),
                0,
                enemy.getMembers(),
                enemy.getSwarmId(),
                enemy.getSwarmType()
        );

        return roomEnemy;
    }

    private double getHP(Enemy enemy) {
        double hp = enemy.getLives() + 1.;
        return hp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BattleAbstractCrashGameRoom gameRoom = (BattleAbstractCrashGameRoom) o;
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

    @Override
    public IGetRoomInfoResponse getRoomInfoResponse(int requestId, IGameSocketClient client, String playerCurrency) throws CommonException {

        IGetRoomInfoResponse roomInfoResponse = super.getRoomInfoResponse(requestId, client, playerCurrency);
        roomInfoResponse.setEndTime(gameState.getEndRoundTime());

        IGameState gameState = this.getGameState();
        if (gameState instanceof AbstractPlayGameState) {

            PlayGameState playGameState = (PlayGameState) gameState;
            boolean needWaitingWhenEnemiesLeave = playGameState.isNeedWaitingWhenEnemiesLeave();

            roomInfoResponse.setNeedWaitingWhenEnemiesLeave(needWaitingWhenEnemiesLeave);
        }

        return roomInfoResponse;
    }

    @Override
    protected List<ITransportSeat> getTransportSeats() {

        List<ITransportSeat> transportSeats = new ArrayList<>();
        List<Seat> seats = this.getAllSeats();
        ITransportObjectsFactoryService toFactoryService = this.getTOFactoryService();

        for (Seat seat : seats) {

            if (seat != null) {

                IRoomPlayerInfo roomPlayerInfo = seat.getPlayerInfo();
                IActiveFrbSession frbSession = roomPlayerInfo.getActiveFrbSession();

                long roundWin = frbSession != null
                        ? frbSession.getWinSum() + seat.getRoundWin().toCents()
                        : seat.getRoundWin().add(seat.getRebuyFromWin()).toCents();

                int seatNumber = this.getSeatNumber(seat);

                ITransportSeat transportSeat = toFactoryService.createSeat(
                        seatNumber,
                        seat.getNickname(),
                        seat.getJoinDate(),
                        seat.getTotalScore() == null ? 0 : seat.getTotalScore().getAmount(),
                        seat.getCurrentScore() == null ? 0 : seat.getCurrentScore().getAmount(),
                        seat.getAvatar(),
                        0,
                        seat.getLevel(),
                        0,
                        seat.getCurrentPlayerRoundInfo().getTotalDamage(),
                        seat.getBetLevel(),
                        roundWin);

                transportSeats.add(transportSeat);
            }
        }
        return transportSeats;
    }

    @Override
    public ITransportObject getFullGameInfo(IGetFullGameInfo fullGameInfoRequest, IGameSocketClient gameSocketClient) {

        int rid = fullGameInfoRequest == null ? -1 : fullGameInfoRequest.getRid();

        ICrashGameInfo crashGameInfo = this.createCrashGameInfo(rid, gameSocketClient);

        return crashGameInfo;
    }

    @Override
    protected int getMaxObservers() {
        int allowedPlayers = this.getAllowedPlayers();
        return allowedPlayers;
    }

    @Override
    public boolean isRoomFullOrManyObservers() {
        short seatsCount = this.getSeatsCount();
        int allowedPlayers = this.getAllowedPlayers();

        if (seatsCount >= allowedPlayers) {
            return true;
        }

        int observersCount = this.getObserverCount();
        int totalObserversCountFromState = getTotalObserversCountFromState();

        boolean roomIsFullOrManyObservers =
                observersCount >= allowedPlayers || totalObserversCountFromState >= allowedPlayers;

        return roomIsFullOrManyObservers;
    }

    @Override
    public ITransportObject processOpenRoom(IGameSocketClient client, IOpenRoom openRoomRequest, String currency)
            throws CommonException {

        getLog().debug("processOpenRoom: client={}, openRoomRequest={}", client, openRoomRequest);

        int rid = openRoomRequest.getRid();

        this.checkAndStartRoom();

        this.removeDisconnectedObservers();

        int observersCount = this.getObserverCount();
        int maxObservers = this.getMaxObservers();

        if (observersCount >= maxObservers) {
            getLog().debug("processOpenRoom: too many observers={}, maxObservers={}", observersCount, maxObservers);
            ITransportObjectsFactoryService toFactoryService = this.getTOFactoryService();
            IError error = toFactoryService
                    .createError(ErrorCodes.TOO_MANY_OBSERVERS, "Too many observers", this.getCurrentTime(), rid);
            return error;
        }

        if (roomInfo.isPrivateRoom()) {
            client.setKicked(roomInfo.isPlayerKicked(client.getAccountId()));
        }

        observePlayers.put(client.getAccountId(), client);

        List<Seat> seats = this.getAllSeats();

        for (Seat seat : seats) {

            if (seat != null && seat.getAccountId() == client.getAccountId()) {
                seat.setSocketClient(client);
            }
        }

        ICrashGameInfo crashGameInfo = this.createCrashGameInfo(rid, client);

        if (roomInfo.isPrivateRoom()) {
            if(!client.isKicked()) {
                updatePlayerStatusForObserver(client.getNickname(), Status.WAITING);
            }
        }

        return crashGameInfo;
    }

    @Override
    public void addSeatFromOtherServer(Seat seat) {

        double rake = 0.0;

        if (seat.getPlayerInfo() instanceof IBattlegroundRoomPlayerInfo) {
            IBattlegroundRoomPlayerInfo battlegroundRoomPlayerInfo = (IBattlegroundRoomPlayerInfo) seat.getPlayerInfo();
            rake = battlegroundRoomPlayerInfo.getBattlegroundRake();
        }

        IMultiNodeRoomInfo roomInfo = this.getRoomInfo();

        ITransportObjectsFactoryService toFactoryService = this.getTOFactoryService();

        ISitInResponse sitInResponse = toFactoryService.createSitInResponse(
                this.getCurrentTime(),
                this.getSeatNumber(seat),
                seat.getNickname(),
                seat.getJoinDate(),
                0,
                0,
                seat.getAvatar(),
                null,
                null,
                false,
                0,
                false,
                0,
                roomInfo.getMoneyType().name(),
                rake);

        long bankId = roomInfo.getBankId();
        String currency = roomInfo.getCurrency();
        long gameId = this.getGameType().getGameId();

        ICrashGameSetting crashGameSetting = settingsService.getSettings(bankId, gameId, currency);

        if (crashGameSetting != null) {

            double maxMultiplier = crashGameSetting.getMaxMultiplier();
            Long maxPlayerProfitInRound = crashGameSetting.getMaxPlayerProfitInRound();
            Long totalPlayersProfitInRound = crashGameSetting.getTotalPlayersProfitInRound();

            sitInResponse.setMaxMultiplier(maxMultiplier);
            sitInResponse.setMaxPlayerProfitInRound(maxPlayerProfitInRound);
            sitInResponse.setTotalPlayersProfitInRound(totalPlayersProfitInRound);

        }

        this.sendChanges(sitInResponse);
    }

    private ICrashGameInfo createCrashGameInfo(int rid, IGameSocketClient client) {

        long startTime = System.currentTimeMillis();

        long roomId = this.getId();
        long accountId = client.getAccountId();
        String nickname = client.getNickname();

        EnemyGame enemyGame = this.getGame();
        GameConfig gameConfig = enemyGame.getConfig(roomId);
        String function = gameConfig.getFunction();

        ISharedGameStateService sharedGameStateService = this.getSharedGameStateService();
        SharedCrashGameState sharedCrashGameState = sharedGameStateService.get(roomId, SharedCrashGameState.class);
        double kilometerMult = this.getKilometerMult(sharedCrashGameState);

        ILobbySession lobbySession = lobbySessionService.get(accountId);
        Double rakePercent = lobbySession != null ? lobbySession.getBattlegroundRakePercent() : null;

        IMultiNodeRoomInfo roomInfo = this.getRoomInfo();
        long roundId = roomInfo.getRoundId();
        boolean isPrivateRoom = roomInfo.isPrivateRoom();

        int mapId = this.getMapId();

        long startRoundTime = gameState.getStartRoundTime();
        long timeToNextState = gameState.getTimeToNextState();
        RoomState roomState = gameState.getRoomState();

        if(isPrivateRoom) {
            getLog().debug("createCrashGameInfo: roomId={}, nickname={}, accountId={}, roundId={}, stacktrace={}",
                    roomId, nickname, accountId, roundId, Thread.currentThread().getStackTrace());

            if (startRoundTime != sharedCrashGameState.getRoundStartTime()) {
                getLog().warn("createCrashGameInfo: roomId={}, nickname={}, accountId={}, roundId={}, startRoundTime={}, " +
                                "sharedCrashGameState.getRoundStartTime()={}", roomId, nickname, accountId, roundId,
                        toHumanReadableFormat(startRoundTime, "yyyy-MM-dd HH:mm:ss.SSS"),
                        toHumanReadableFormat(sharedCrashGameState.getRoundStartTime(), "yyyy-MM-dd HH:mm:ss.SSS"));
            }

            if (roomState != sharedCrashGameState.getState()) {
                getLog().warn("createCrashGameInfo: roomId={}, nickname={}, accountId={}, roundId={}, roomState={}," +
                                " sharedCrashGameState.getState()={}",
                        roomId, nickname, accountId, roundId, roomState, sharedCrashGameState.getState());
            }
        }

        List<ITransportSeat> transportSeats = this.getTransportSeats();

        List<ICrashRoundInfo> multHistory = this.getMap() != null
                ? this.getMap().getMultHistory()
                : new ArrayList<>();

        ITransportObjectsFactoryService toFactoryService = this.getTOFactoryService();

        ICrashGameInfo crashGameInfo = toFactoryService.createCrashGameInfo(System.currentTimeMillis(), rid, roomId, mapId,
                startRoundTime, roomState, transportSeats, roundId, multHistory, nextMapId, timeToNextState,
                function, true, kilometerMult, rakePercent);

        crashGameInfo.setBuyIn(roomInfo.getStake().toCents());

        long bankId = roomInfo.getBankId();
        String currency = roomInfo.getCurrency();
        long gameId = this.getGameType().getGameId();

        ICrashGameSetting crashGameSetting = settingsService.getSettings(bankId, gameId, currency);
        if (crashGameSetting != null) {
            int  maxRoomPlayers = crashGameSetting.getMaxRoomPlayers();
            crashGameInfo.setMaxRoomPlayers(maxRoomPlayers);
        }

        List<Seat> seats = this.getSeats();
        for (Seat seat : seats) {

            if(seat != null) {

                String seatNickname = seat.getNickname();
                long seatAccountId = seat.getAccountId();
                long canceledBetAmount = seat.getCanceledBetAmount();

                crashGameInfo.setCanceledBetAmount(seatNickname, canceledBetAmount);

                if (seatAccountId == accountId) {

                    crashGameInfo.setCanceledBetAmount(canceledBetAmount);
                    IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);

                    if (roomPlayerInfo != null) {
                        boolean isPending = roomPlayerInfo.isPendingOperation();
                        crashGameInfo.setPending(isPending);
                    }
                }

                Map<String, ICrashBetInfo> crashBetInfoMap = seat.getCrashBets();

                List<String> betNicknames = new ArrayList<>();

                for (Map.Entry<String, ICrashBetInfo> crashBetInfoEntry : crashBetInfoMap.entrySet()) {

                    String betId = crashBetInfoEntry.getKey();
                    ICrashBetInfo crashBetInfo = crashBetInfoEntry.getValue();

                    crashGameInfo.addBet(
                            betId,
                            seatNickname,
                            crashBetInfo.getCrashBetAmount(),
                            crashBetInfo.isAutoPlay(),
                            crashBetInfo.getMultiplier(),
                            crashBetInfo.getEjectTime(),
                            crashBetInfo.getAutoPlayMultiplier());

                    betNicknames.add(seatNickname);
                }

                if (betNicknames.size() > 0 && isPrivateRoom) {
                    updatePlayersStatusNicknamesOnly(betNicknames, Status.READY, false, false);
                }
            }
        }

        roomState = gameState.getRoomState();
        if (RoomState.PLAY.equals(roomState)) {
            if (sharedCrashGameState != null && sharedCrashGameState.getMaxCrashData() != null) {

                MaxCrashData maxCrashData = sharedCrashGameState.getMaxCrashData();

                crashGameInfo.setCurrentMult(maxCrashData.getCurrentMult());
                crashGameInfo.setCrash(maxCrashData.getCurrentMult() >= maxCrashData.getCrashMult());
                crashGameInfo.setTimeSpeedMult(maxCrashData.getTimeSpeedMult());
                crashGameInfo.setAllEjectedTime(maxCrashData.getLastEjectTime());
            }
        }

        if (isPrivateRoom) {

            crashGameInfo.setOwner(client.isOwner());
            crashGameInfo.setMinSeats(getMinSeats());

            List<ITransportObserver> transportObservers = getTransportObservers();
            crashGameInfo.setObservers(transportObservers);

            if (client.isOwner()) {
                List<OnlinePlayer> onlinePlayersNotInPrivateRoom =
                        privateRoomPlayersStatusService.getOnlinePlayersNotInPrivateRoom(roomInfo);

                List<com.betsoft.casino.mp.model.onlineplayer.Friend> friendsNotInPrivateRoom =
                        privateRoomPlayersStatusService.convertOnlinePlayersToGameInfoFriends(onlinePlayersNotInPrivateRoom);

                List<com.betsoft.casino.mp.model.onlineplayer.Friend> onlineFriendsNotInPrivateRoom =
                        friendsNotInPrivateRoom.stream()
                                .filter(f -> f.isOnline())
                                .collect(Collectors.toList());

                crashGameInfo.setFriends(onlineFriendsNotInPrivateRoom);
            }
        }

        getLog().debug("createCrashGameInfo: roomId={}, nickname={}, accountId={}, roundId={}, duration={}ms",
                roomId, nickname, accountId, roundId, System.currentTimeMillis() - startTime);

        return crashGameInfo;
    }

    private double getKilometerMult(SharedCrashGameState crashGameState) {
        double kilometerMult =  crashGameState != null ? crashGameState.getKilometerMult() : 0.8;
        return kilometerMult;
    }

    @Override
    protected Money getReturnedBet(Seat seat) {
        long canceledBetAmount = seat.retrieveCanceledBetAmount();
        return Money.fromCents(canceledBetAmount);
    }

    @Override
    protected void rollbackSeatWin(Seat seat, Money roundWin, int ammoAmount, Money returnedBet) {
        super.rollbackSeatWin(seat, roundWin, ammoAmount, Money.ZERO);
        seat.setCanceledBetAmount(returnedBet.toCents());
    }

    @Override
    public void toggleMap() {
        map.getMapShape().setId(nextMapId);
        this.generateNextMapId();
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
        int crashBetsCount = seat.getCrashBetsCount();
        return crashBetsCount > 0;
    }

    @Override
    public List<Seat> getRealSeats() {

        List<Seat> resultList = new ArrayList<>();

        List<Seat> seats = this.getAllSeats();

        for (Seat seat : seats) {
            if (seat == null) {
                continue;
            }
            if (seat.getCrashBetsCount() > 0) {
                resultList.add(seat);
            }
        }

        return resultList;
    }

    @Override
    public short getRealSeatsCount() {
        List<Seat> seats = this.getRealSeats();

        short count = seats == null ? 0 : (short)seats.size();

        return count;
    }

    @Override
    public List<Long> getRealSeatsAccountId() {

        List<Long> resultList = new ArrayList<>();

        List<Seat> seats = this.getRealSeats();

        for (Seat seat : seats) {
            resultList.add(seat.getAccountId());
        }

        return resultList;
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
        return seat.getCrashBetsCount() == 0 && seat.isDisconnected() && isNoActivityForTime(seat, NO_ACTIVITY_TIME);
    }

    private boolean isNoActivityForTime(Seat seat, long time) {
        return System.currentTimeMillis() - seat.getLastActivityDate() > time;
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

        RoomState roomState = gameState.getRoomState();
        if (RoomState.PLAY.equals(roomState)) {

            ISharedGameStateService sharedGameStateService = this.getSharedGameStateService();
            long id = this.getId();

            SharedCrashGameState sharedCrashGameState = sharedGameStateService.get(id, SharedCrashGameState.class);

            lastRoundStartTime = sharedCrashGameState.getRoundStartTime();
            return lastRoundStartTime;
        }

        lastRoundStartTime = null;
        return null;
    }

    @Override
    public void startUpdateTimer() {
        if (updateTimer != null) {
            getLog().debug("startUpdateTimer: need stop stopUpdateTimer");
            this.stopUpdateTimer();
        }
        this.checkAndStartTimer();
    }

    @Override
    public void sendRoundResultsForExtSeats(Map<Long, IRoundResult> roundResults) {
        if (roomInfoService != null && roundResults != null) {

            for(Map.Entry<Long, IRoundResult> entry : roundResults.entrySet()) {
                long accountId = entry.getKey();
                IRoundResult roundResult = entry.getValue();
                Runnable sendSeatMessageTask = this.createSendSeatMessageTask(accountId, roundResult);
                this.executeOnAllMembers(sendSeatMessageTask);
            }
         }
    }

    @Override
    public void sendRoundResultForExtObserversWithNoSeats(IRoundResult roundResult) {
        if (roomInfoService != null && roundResult != null) {
            Runnable sendObserverNoSeatMessageTask = this.createSendAllObserversNoSeatMessageTask(roundResult);
            this.executeOnAllMembers(sendObserverNoSeatMessageTask);
        }
    }

    @Override
    public Map<Long, IRoundResult> getExtResultsAndSendForLocal() {

        Map<Long, IRoundResult> disconnectedSeatsRoundResults = new HashMap<>();
        List<IBattlegroundRoundResult> battlegroundRoundResults = new ArrayList<>();
        List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs = new ArrayList<>();

        List<String> kickedNicknames = new ArrayList<String>();

        List<ITransportSeat> transportSeats = this.getTransportSeats();

        ITransportObjectsFactoryService toFactoryService = this.getTOFactoryService();

        //find all IBattlegroundRoundResult objects for all seats in the room
        //and add these to the battlegroundRoundResults list
        List<Seat> seats = this.getAllSeats();
        for (Seat seat : seats) {

            if (seat != null) {

                PlayerRoundInfo playerRoundInfo = seat.getCurrentPlayerRoundInfo();

                long seatWin = playerRoundInfo.getTotalPayouts().toCents();
                long seatRefund = playerRoundInfo.getRefundAmount();

                if (playerRoundInfo.getTotalBets().greaterThan(Money.ZERO)) {

                    IBattlegroundRoundResult battlegroundRoundResult = toFactoryService.createBattlegroundRoundResult(
                            seat.getId(),
                            seatWin,
                            seatRefund,
                            playerRoundInfo.getTotalPot(),
                            seat.getNickname());

                    battlegroundRoundResults.add(battlegroundRoundResult);

                    if (seat.getSocketClient() != null && seat.getSocketClient().isKicked()) {
                        kickedNicknames.add(battlegroundRoundResult.getNickName());
                    }
                }
            }
        }

        long id = this.getId();
        ISharedGameStateService sharedGameStateService = this.getSharedGameStateService();
        SharedCrashGameState sharedCrashGameState = sharedGameStateService.get(id, SharedCrashGameState.class);

        //create roundResult objects for every seat and add this pair to seatsRoundResultsPairs list
        //submit RoundResult message to the seat if seat is connected to the node
        //if seat is not connected to the node add account and roundResult to the list disconnectedSeatsRoundResults
        seats = this.getAllSeats();
        for (Seat seat : seats) {

            if (seat != null) {

                IRoundResult roundResult = this.getRoundResult(seat, toFactoryService, transportSeats, battlegroundRoundResults, sharedCrashGameState);
                getLog().debug("getExtResultsAndSendForLocal: seat={}, roundResult={}", seat, roundResult);

                seatsRoundResultsPairs.add(new Pair<>(seat, roundResult));

                if (!seat.isDisconnected()) {
                    seat.sendMessage(roundResult);
                } else {
                    getLog().warn("getExtResultsAndSendForLocal: Battle, cannot send round result from this server," +
                                    " seat is disconnected or on other server, try send remote, seat: {}, seat.getCrashBets(): {}",
                            seat.getAccountId(), seat.getCrashBets());

                    getLog().debug("getExtResultsAndSendForLocal: {}, iRoundResult.getRoundId(): {}, iRoundResult.getWinAmount(): {}",
                            seat.getAccountId(), roundResult.getRoundId(), roundResult.getWinAmount());

                    disconnectedSeatsRoundResults.put(seat.getAccountId(), roundResult);
                }
            }
        }

        getLog().debug("getExtResultsAndSendForLocal: disconnectedSeatsRoundResults: {},", disconnectedSeatsRoundResults);
        getLog().debug("getExtResultsAndSendForLocal: battlegroundRoundResults: {},", battlegroundRoundResults);
        getLog().debug("getExtResultsAndSendForLocal: seatsRoundResultsPairs: {},", seatsRoundResultsPairs);


        if(!seatsRoundResultsPairs.isEmpty()) {

            Collection<IGameSocketClient> observers = this.getObservers();

            if (observers != null && !observers.isEmpty() && seats != null && !seats.isEmpty()) {

                List<Long> seatsAccountIds = seats.stream()
                        .map(Seat::getAccountId)
                        .collect(Collectors.toList());

                List<IGameSocketClient> observersNoSeat = observers.stream()
                        .filter(o -> !seatsAccountIds.contains(o.getAccountId()))
                        .collect(Collectors.toList());

                if(observersNoSeat != null && !observersNoSeat.isEmpty()) {

                    getLog().debug("getExtResultsAndSendForLocal: there are observersNoSeat={},", observersNoSeat);

                    IRoundResult roundResultObserversNoSeat = seatsRoundResultsPairs.get(0).getValue().copy();

                    roundResultObserversNoSeat.setWinAmount(0);
                    roundResultObserversNoSeat.setRealWinAmount(0);
                    roundResultObserversNoSeat.setWinRebuyAmount(0);

                    getLog().debug("getExtResultsAndSendForLocal: roundResultObserversNoSeat={},", roundResultObserversNoSeat);

                    for (IGameSocketClient observerNoSeat : observersNoSeat) {
                        if (observerNoSeat != null) {
                            roundResultObserversNoSeat.setDate(System.currentTimeMillis());
                            observerNoSeat.sendMessage(roundResultObserversNoSeat);
                        }
                    }
                }
            }
        }

        //save round result to Google BigQuery BD
        List<Map<String, Object>> roundResults = analyticsDBClientService.prepareBattlegroundRoundResults(seatsRoundResultsPairs, this);
        asyncExecutorService.execute(() ->
            analyticsDBClientService.saveRoundResults(roundResults)
        );

        IRoomInfo roomInfo = getRoomInfo();

        //if room is private, set status for all seats to WAITING
        if (roomInfo == null) {
            getLog().error("getExtResultsAndSendForLocal: roomInfo is null, for roomId: {}", getId());
        } else {
            if (roomInfo.isPrivateRoom()) {

                if(battlegroundRoundResults == null || battlegroundRoundResults.isEmpty()) {
                    getLog().error("getExtResultsAndSendForLocal: battlegroundRoundResults is null or empty, for roomId: {}", getId());
                } else {
                    List<String> nicknames = battlegroundRoundResults.stream()
                            .map(IBattlegroundRoundResult::getNickName)
                            .filter(nn -> !kickedNicknames.contains(nn))
                            .collect(Collectors.toList());

                    getLog().debug("getExtResultsAndSendForLocal: call updatePlayersStatusNicknamesOnly to set " +
                            "Status.WAITING for nicknames={}", nicknames);

                    updatePlayersStatusNicknamesOnly(nicknames, Status.WAITING, false, true);
                }
            } else {
                getLog().debug("getExtResultsAndSendForLocal: Room is not private skip updatePlayersStatus");
            }
        }

        return disconnectedSeatsRoundResults;
    }

    private IRoundResult getRoundResult(Seat seat, ITransportObjectsFactoryService toFactoryService,
                                        List<ITransportSeat> transportSeats, List<IBattlegroundRoundResult> battlegroundRoundResults,
                                        SharedCrashGameState sharedCrashGameState) {

        getLog().debug("getRoundResult: seat={}, transportSeats: {}, battlegroundRoundResults: {}",
                seat, transportSeats, battlegroundRoundResults);

        PlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();

        roundInfo.updateStatOnEndRound(0, seat.getCurrentScore(), 0);

        IExperience prevScore = seat.getPlayerInfo().getPrevXP();
        int prevLevel = AchievementHelper.getPlayerLevel(prevScore);
        int level = AchievementHelper.getPlayerLevel(seat.getTotalScore());
        double winAmount = seat.getRoundWin().add(seat.getRebuyFromWin()).toDoubleCents();

        long roundId = this.getRoomInfo().getRoundId();

        ILevelInfo beforeRoundLevelInfo = toFactoryService.createLevelInfo(
                prevLevel,
                prevScore.getLongAmount(),
                AchievementHelper.getXP(prevLevel),
                AchievementHelper.getXP(prevLevel + 1));

        ILevelInfo afterRoundLevelInfo = toFactoryService.createLevelInfo(
                level,
                seat.getTotalScore().getLongAmount(),
                AchievementHelper.getXP(level),
                AchievementHelper.getXP(level + 1));

        IRoundResult roundResult = toFactoryService.createRoundResult(
                this.getCurrentTime(),
                TObject.SERVER_RID,
                winAmount,
                seat.getRebuyFromWin().toDoubleCents(),
                0,
                seat.getCurrentScore().getLongAmount(),
                seat.getTotalScore().getLongAmount(),
                0,
                0,
                nextMapId,
            transportSeats,
                0,
                seat.getRoundWinInCredits(),
                0,
                0,
                0,
                AchievementHelper.getXP(level),
                Collections.emptyList(),
                0,
                0,
                0,
                beforeRoundLevelInfo,
                afterRoundLevelInfo,
                0,
                seat.getQuestsCompletedCount(),
                seat.getQuestsPayouts(),
                roundId,
                Collections.emptyList(),
                0,
                winAmount,
                roundInfo.getFreeShotsWon(),
                roundInfo.getMoneyWheelCompleted(),
                roundInfo.getMoneyWheelPayouts(),
                roundInfo.getTotalDamage(),
            battlegroundRoundResults);

        if (sharedCrashGameState != null && sharedCrashGameState.getMaxCrashData() != null) {
            MaxCrashData maxCrashData = sharedCrashGameState.getMaxCrashData();
            roundResult.setCrashMultiplier(maxCrashData.getCrashMult());
        }

        getLog().debug("getRoundResult: seat={}, roundResult={}", seat, roundResult);

        return roundResult;
    }

    public boolean needToSitOutByNoActivity(Seat seat) {

        boolean noBets = seat.getCrashBetsCount() == 0;

        IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(seat.getAccountId());
        boolean isPending = roomPlayerInfo != null && roomPlayerInfo.isPendingOperation();
        boolean isNoActive = isPending
                ? this.isNoActivityForTime(seat, NO_ACTIVITY_TIME_ROUND_FOR_PENDING)
                : this.isNoActivityForTime(seat, NO_ACTIVITY_TIME_ROUND);

        boolean isWantSitOut = seat.isWantSitOut();

        boolean isClientDisconnected = this.isSeatClientDisconnected(seat);

        getLog().debug("noBets={} isNoActive={} seat.isWantSitOut()={} isSeatClientDisconnected(seat)={}",
                noBets, isNoActive, isWantSitOut, isClientDisconnected);

        return noBets && isNoActive && (isWantSitOut || isClientDisconnected);
    }

    @Override
    public void sendStartNewRoundToAllPlayers(List<ISeat> seats) {
        try {

            long roundStartDate = roomInfo.getLastRoundStartDate();
            long roundId = roomInfo.getRoundId();

            SharedCrashGameState sharedCrashGameState = this.getSharedGameStateService()
                    .get(getId(), SharedCrashGameState.class);

            if (sharedCrashGameState != null) {
                getLog().debug("sendStartNewRoundToAllPlayers: sharedCrashGameState={}", sharedCrashGameState);
                roundStartDate = sharedCrashGameState.getRoundStartTime();
                roundId = sharedCrashGameState.getRoundId();
            }

            List<ISeat> localNotPendingSeats = seats.stream()
                    .filter((seat) ->
                    {
                        IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(seat.getAccountId());
                        boolean isLocal = seat.getSocketClient() != null;
                        boolean isNotPending = !roomPlayerInfo.isPendingOperation();
                        return isLocal && isNotPending;
                    })
                    .collect(Collectors.toList());


            getLog().debug("sendStartNewRoundToAllPlayers: roomId={}, localNotPendingSeats={}",
                    roomInfo.getId(), localNotPendingSeats);

            List<IStartNewRoundResult> startNewRoundResults = socketService.startNewRoundForManyPlayers(
                    localNotPendingSeats,
                    roomInfo.getId(),
                    roundId,
                    roundStartDate,
                    true,
                    roomInfo.getStake().toCents());

            for (IStartNewRoundResult startNewRoundResult : startNewRoundResults) {

                getLog().debug("sendStartNewRoundToAllPlayers: startNewRoundResult={}", startNewRoundResult);

                long newRoundId = startNewRoundResult.getPlayerRoundId();

                Seat seat = this.getSeatByAccountId(startNewRoundResult.getAccountId());

                if (newRoundId > 0 && startNewRoundResult.isSuccess()) {

                    if (seat == null) {
                        getLog().error("sendStartNewRoundToAllPlayers: seat is null for accountId={}",
                                startNewRoundResult.getAccountId());
                        continue;
                    }

                    IRoomPlayerInfo roomPlayerInfo = seat.getPlayerInfo();
                    if (roomPlayerInfo == null) { //impossible, remove after check
                        getLog().error("sendStartNewRoundToAllPlayers: roomPlayerInfo is null for seat={}", seat);
                        continue;
                    }

                    roomPlayerInfo.setExternalRoundId(newRoundId);
                    seat.updatePlayerRoundInfo(newRoundId);

                    playerInfoService.put(roomPlayerInfo);
                }

                getLog().debug("sendStartNewRoundToAllPlayers success: {}", seat.getAccountId());
            }
        } catch (Exception e) {
            getLog().error("sendStartNewRoundToAllPlayers: error, seat={}", seats, e);
        }
    }
}
