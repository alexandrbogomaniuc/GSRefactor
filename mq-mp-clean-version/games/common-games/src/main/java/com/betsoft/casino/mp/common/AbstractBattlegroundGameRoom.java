package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.*;
import com.betsoft.casino.mp.model.friends.Friend;
import com.betsoft.casino.mp.model.friends.Friends;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.model.onlineplayer.OnlinePlayer;
import com.betsoft.casino.mp.model.privateroom.Player;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.room.*;
import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.payment.PendingOperationType;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.mp.TransactionErrorCodes;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.hazelcast.core.IExecutorService;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.utils.ErrorCodes.WRONG_WEAPON;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("rawtypes")
public abstract class AbstractBattlegroundGameRoom<
        GAME extends IGame, MAP extends IMap,
        SEAT extends IBattlegroundSeat,
        SNAPSHOT extends IGameRoomSnapshot,
        ENEMY extends IEnemy,
        ENEMY_TYPE extends IEnemyType,
        ROOM_INFO extends ISingleNodeRoomInfo,
        RPI extends IBattlegroundRoomPlayerInfo>
        extends AbstractActionGameRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI>
        implements IBattlegroundRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI>,
        ICAFRoom,
        IRoomInfoService.IRoomInfoChangeHandler<ROOM_INFO>,
        IPrivateRoomPlayersStatusService.IPrivateRoomChangeHandler,
        IPrivateRoomPlayersStatusService.IFriendsChangeHandler,
        IPrivateRoomPlayersStatusService.IOnlinePlayerChangeHandler {

    private final IPrivateRoomPlayersStatusService privateRoomPlayersStatusService;
    private ScheduledExecutorService scheduler = null;
    private static final int PLAYER_STATUSES_PERIOD_IN_SEC = 3;

    private long lastUpdateTime = 0;
    private Set<Long> savedKickedPlayers = new HashSet<>();

    private String roomListenerId = null;
    private String privateRoomListenerId = null;
    private String friendsListenerId = null;
    private String onlinePlayersListenerId = null;

    public AbstractBattlegroundGameRoom(ApplicationContext context, Logger logger, SEAT[] seats, ROOM_INFO roomInfo, GAME game, MAP map,
                                        IPlayerStatsService playerStatsService, IPlayerQuestsService playerQuestsService,
                                        IWeaponService weaponService, IExecutorService remoteExecutorService,
                                        IPlayerProfileService playerProfileService, IGameConfigService gameConfigService,
                                        IActiveFrbSessionService activeFrbSessionService,
                                        IActiveCashBonusSessionService activeCashBonusSessionService,
                                        ITournamentService tournamentService) {

        super(context, logger, seats, roomInfo, game, map, playerStatsService, playerQuestsService, weaponService, remoteExecutorService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService);

        this.privateRoomPlayersStatusService =
                context.getBean("privateRoomPlayersStatusService", IPrivateRoomPlayersStatusService.class);

        init();
    }

    protected void init() {

        if (roomInfo.isPrivateRoom()) {

            savedKickedPlayers = new HashSet<>(roomInfo.getKickedPlayers());

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

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleWithFixedDelay(new PrivateRoomPlayersCheckTask(), 0,
                    PLAYER_STATUSES_PERIOD_IN_SEC, TimeUnit.SECONDS);

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

            if (scheduler != null) {
                getLog().debug("Shutdown scheduler");
                scheduler.shutdown();
                scheduler = null;
            }
        }
    }

    class PrivateRoomPlayersCheckTask implements Runnable {

        public final long MAX_LOADING_TIME_MS = 180000; //3 min
        public final long MAX_PLAYING_ROUND_TIME_MS = 120000; //2 min

        @Override
        public void run() {
            PrivateRoom privateRoom = getPrivateRoomPlayersStatus();
            if (privateRoom != null) {
                //check if there are stuck players having LOADING or PLAYING status
                //if there are any stuck players move their status to ACCEPTED
                List<Player> players = privateRoom.getPlayers();
                long now = System.currentTimeMillis();

                if (players != null && !players.isEmpty()) {
                    List<Player> playersStuck = players.stream()
                            .filter(p -> p != null && p.getStatus() != null
                                            && (
                                            (
                                                    (p.getStatus() == Status.LOADING &&
                                                            (now - p.getUpdateTime()) >= MAX_LOADING_TIME_MS))
                                                    || (
                                                    (p.getStatus() == Status.PLAYING &&
                                                            (now - p.getUpdateTime()) >= MAX_PLAYING_ROUND_TIME_MS))
                                    )
                            )
                            .collect(Collectors.toList());

                    if (playersStuck != null && !playersStuck.isEmpty()) {
                        playersStuck.forEach(
                                p -> p.setStatus(Status.ACCEPTED)
                        );
                        privateRoom.setPlayers(playersStuck);
                        privateRoomPlayersStatusService.updatePlayersStatusInPrivateRoom(privateRoom, false, true);
                    }
                }
            }
        }
    }

    @Override
    public String getOwnerExternalId() {

        if (privateRoomPlayersStatusService != null) {
            return privateRoomPlayersStatusService.getOwnerExternalId(roomInfo);
        }

        return null;
    }

    @Override
    public void processFriendsChange(Friends friends) {

        sendGameInfoToOwner();

        //retrieve onlineStatus for friends
        if (friends == null) {
            getLog().error("processFriendsChange: friends is null");
            return;
        }

        if (friends.getFriends() == null || friends.getFriends().isEmpty()) {
            getLog().error("processFriendsChange: friends Map is empty, friends:{}", friends);
            return;
        }

        int serverId = getGameServerId();

        privateRoomPlayersStatusService
                .getOnlineStatusFromCanexAsync(serverId, friends.getFriends().values());
    }

    @Override
    public Set<String> getOnlinePlayerExternalIds() {

        if(roomInfo == null) {
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
        sendGameInfoToOwner();
    }

    @Override
    public void sendGameInfoToSocketClient(IGameSocketClient socketClient) {
        if (socketClient != null) {
            try {
                IFullGameInfo fullGameInfo = getFullGameInfo(null, socketClient);
                socketClient.sendMessage(fullGameInfo);
            } catch (Exception e) {
                getLog().error("sendGameInfoToSocketClient: exception, {}", e.getMessage(), e);
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
        Collection<IGameSocketClient> roomNodeObservers =  getObservers();

        if(roomNodeObservers != null && !roomNodeObservers.isEmpty()) {

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
        sendGameInfoToSocketClient(ownerSocketClient);
    }

    @Override
    public PrivateRoom getPrivateRoomPlayersStatus() {

        if (privateRoomPlayersStatusService != null) {
            return privateRoomPlayersStatusService.getPrivateRoomPlayersStatus(roomInfo);
        }

        return null;
    }

    @Override
    public void processPrivateRoomChange(PrivateRoom privateRoom) {

        if(privateRoom != null) {
            //if there are any recorded updates to privateRoom object
            //update room owner with changes
            long updateTime = privateRoom.getUpdateTime();
            if (updateTime > lastUpdateTime) {
                lastUpdateTime = updateTime;
                sendGameInfoToOwner();
            }
        }
    }

    @Override
    public String getPrivateRoomId() {
        if (privateRoomPlayersStatusService != null) {
            return privateRoomPlayersStatusService.getPrivateRoomId(this, roomInfo);
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
    public void updatePlayerStatusForObserver(String nickname, Status status) {

        getLog().debug("updatePlayerStatusForObserver: nickname={}, status={}", nickname, status);

        if (StringUtils.isTrimmedEmpty(nickname)) {
            getLog().error("updatePlayerStatusForObserver: nickname is null or empty");
            return;
        }

        if (!roomInfo.isPrivateRoom()) {
            getLog().error("updatePlayerStatusForObserver: room is not Private: {}", roomInfo);
            return;
        }

        for (IGameSocketClient observer : getObservers()) {
            if (observer != null && observer.getNickname() != null && observer.getNickname().equals(nickname)) {
                if (observer.isOwner()) {
                    updatePlayersStatus(Arrays.asList(observer), status, false, false);
                } else {
                    updatePlayersStatusAndSendToOwner(Arrays.asList(observer), status);
                }
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

        if (this.getRoomInfo() != null) {
            if (serverId == 0) {
                serverId = this.getRoomInfo().getGameServerId();
            }

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
            getLog().error("invitePlayersToPrivateRoomAtCanex: room is not private:{}, skip", getRoomId());
            return false;
        }

        if (StringUtils.isTrimmedEmpty(getRoomInfo().getPrivateRoomId())) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: getRoomInfo().getPrivateRoomId() is empty:{}, skip", getRoomId());
            return false;
        }

        int serverId = getGameServerId();
        PrivateRoom privateRoom = getPrivateRoomPlayersStatus();

        if (privateRoom == null) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: privateRoom is null for:{} skip", getRoomId());
            return false;
        }

        if (StringUtils.isTrimmedEmpty(privateRoom.getOwnerExternalId())) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: privateRoom.getOwnerExternalId() is empty for:{}, " +
                    "{} skip", getRoomId(), privateRoom);
            return false;
        }

        Friends friends = privateRoomPlayersStatusService.getFriendsForExternalId(privateRoom.getOwnerExternalId());

        if (friends == null ) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: friends is null:{}, " +
                    "OwnerExternalId:{} skip", getRoomId(), privateRoom.getOwnerExternalId());
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

        if(friendsSet == null || friendsSet.isEmpty()) {
            getLog().error("invitePlayersToPrivateRoomAtCanex: friendsSet is empty:{}, " +
                    "OwnerExternalId:{} skip", friends, privateRoom.getOwnerExternalId());
            return false;
        }

        return privateRoomPlayersStatusService.invitePlayersToPrivateRoomAtCanex(serverId, friendsSet,
                getRoomInfo().getPrivateRoomId());
    }

    @Override
    public short getRealSeatsCount() {
        short count = 0;
        for (SEAT seat : getAllSeats()) {
            if (seat == null) {
                continue;
            }
            if (seat.getAmmoAmount() > 0) {
                getLog().debug("getRealSeatsCount: Found bg seat accountId:{} seater with ammo: {}, seat.getNickname() :{}, getRoundBuyInAmount: {} ",
                        seat.getAccountId(), seat.getAmmoAmount(), seat.getNickname(), seat.getPlayerInfo().getRoundBuyInAmount());
                count++;
            } else if ((seat.getSocketClient() != null || seat.getAmmoAmount() > 0 || seat.getRoundWin().greaterThan(Money.ZERO)) &&
                    !seat.isBot() &&
                    !seat.isSitOutStarted()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public short getRealConfirmedSeatsCount() {
        short count = 0;
        for (SEAT seat : getAllSeats()) {
            if (seat == null || seat.getSocketClient() == null || seat.isSitOutStarted()) {
                continue;
            }
            ILobbySession lobbySession = lobbySessionService.get(seat.getSocketClient().getSessionId());
            if (lobbySession != null && lobbySession.isConfirmBattlegroundBuyIn() &&
                    !seat.getPlayerInfo().isPendingOperation()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<Integer> getRealConfirmedSeatsId() {
        List<Integer> confirmedSeats = new ArrayList<>();
        for (SEAT seat : getAllSeats()) {
            if (seat == null || seat.getSocketClient() == null || seat.isSitOutStarted()) {
                continue;
            }
            ILobbySession lobbySession = lobbySessionService.get(seat.getSocketClient().getSessionId());
            if (lobbySession != null && lobbySession.isConfirmBattlegroundBuyIn() &&
                    !seat.getPlayerInfo().isPendingOperation()) {
                confirmedSeats.add(seat.getNumber());
            }
        }
        return confirmedSeats;
    }

    @Override
    public boolean isBattlegroundMode() {
        return true;
    }

    public Pair<List<Integer>, List<IBattleScoreInfo>> getCurrentKings() {

        List<IBattleScoreInfo> listScoreInfo = new LinkedList<>();

        Set<Integer> kingOfHillIds = new HashSet<>();
        long kingSeatWin = getSeats().stream()
                .filter(Objects::nonNull)
                .mapToLong(seat -> seat.getCurrentPlayerRoundInfo().getTotalPayouts().toCents())
                .max()
                .orElse(0L);

        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                IActionGamePlayerRoundInfo currentPlayerRoundInfo = getCurrentPlayerRoundInfo(seat);
                long realWinsCents = currentPlayerRoundInfo.getTotalPayouts().toCents();
                long realBetsCents = currentPlayerRoundInfo.getTotalBets().toCents() +
                        currentPlayerRoundInfo.getTotalBetsSpecialWeapons().toCents();
                IBattleScoreInfo battleScoreInfo = getTOFactoryService().createBattleScoreInfo(
                        seat.getNumber(), realBetsCents, realWinsCents, seat.isKingOfHill());
                listScoreInfo.add(battleScoreInfo);
                if (realWinsCents > 0 && realWinsCents >= kingSeatWin) {
                    kingOfHillIds.add(seat.getNumber());
                }
            }
        }

        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                seat.setKingOfHill(kingOfHillIds.contains(seat.getNumber()));
            }
        }

        listScoreInfo.sort(Comparator.comparingLong(IBattleScoreInfo::getWinAmount)
                .thenComparingLong(IBattleScoreInfo::getBetAmount)
                .reversed());

        return new Pair<>(kingOfHillIds.isEmpty() ? Collections.singletonList(-1) : new ArrayList<>(kingOfHillIds), listScoreInfo);
    }

    private long getBattlegroundWin(long roomBuyInSummary, double rakePercentage) {

        if (roomBuyInSummary <= 0) {
            return 0;
        }

        short count = 0;
        for (SEAT seat : getAllSeats()) {
            if (seat != null && seat.getPlayerInfo() != null && !seat.getPlayerInfo().isPendingOperation()) {
                count++;
            }
        }

        long roomBuyIn = (long) getRoomInfo().getStake().toDoubleCents();
        long allSeatsBuyIn = roomBuyIn * count;

        getLog().debug("getCurrentKings: roomBuyIn={}, roomBuyInSummary={}, rakePercentage={}, allSeatsBuyIn={}",
                roomBuyIn, roomBuyInSummary, rakePercentage, allSeatsBuyIn);

        BigDecimal winDecimal = BigDecimal.valueOf(roomBuyInSummary);

        BigDecimal rake = BigDecimal.valueOf(rakePercentage)
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL128)
                .multiply(winDecimal, MathContext.DECIMAL128);

        BigDecimal winWithoutRake = winDecimal.subtract(rake, MathContext.DECIMAL128);

        long resultWin = winWithoutRake.longValue();
        if (resultWin <= 0) {
            return roomBuyInSummary == 1 ? 1 : roomBuyInSummary - 1; //get minimal rake, one cent
        }

        return resultWin;
    }

    private long splitBattlegroundWinByWinners(long win, int countWinner) {
        if (countWinner == 0) {
            return 0;
        }

        BigDecimal splitWin = BigDecimal.valueOf(win)
                .divide(BigDecimal.valueOf(countWinner), MathContext.DECIMAL128);

        return splitWin.longValue();
    }

    private List<IBattlegroundRoundResult> processBattleGroundMode() {
        List<IBattlegroundRoundResult> battlegroundRoundResults = new ArrayList<>();
        IRoomInfo roomInfo = getRoomInfo();

        StringBuilder winnerName = new StringBuilder();
        double rakePercent = 5.0;
        long roomBuyInSummary = 0;
        Map<Integer, Long> seatsWinsMap = new LinkedHashMap<>(); // Integer - seat nr, Long - total win by seat during the round
        SEAT kingOfHillSeat = null;
        int winnersCounter = 0;
        int playersWithBetsCounter = 0;
        int allPlayersCounter = 0;
        long roomBuyInCents = roomInfo.getStake().toCents();

        for (SEAT seat : getAllSeats()) {
            if (seat != null) {

                allPlayersCounter++;

                IBattlegroundRoomPlayerInfo playerInfo = seat.getPlayerInfo();

                if (playerInfo != null) {

                    if (seat.isKingOfHill()) {
                        winnersCounter++;
                        kingOfHillSeat = seat;
                        winnerName.append(playerInfo.getNickname());
                    }

                    if (playerInfo.getSessionId() != null) {//Adjust rake if required
                        rakePercent = playerInfo.getBattlegroundRake();
                        if (rakePercent < 0.0 || rakePercent > 100.0) {
                            getLog().warn("processBattleGroundMode: Battleground rakePercent has wrong value={}, correct to default;t 5.0",
                                    rakePercent);
                            rakePercent = 5.0;
                        }
                    }

                    long roundBuyInAmount = playerInfo.getRoundBuyInAmount();
                    if (roundBuyInAmount != roomBuyInCents) {
                        getLog().warn("processBattleGroundMode: found strange case, roundBuyInAmount: {} is  differ from roomBuyInCents {}",
                                roundBuyInAmount, roomBuyInCents);
                    }

                    if (roundBuyInAmount > 0) {

                        roomBuyInSummary += roomBuyInCents;

                        IPlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();

                        if (roundInfo != null) {

                            long totalSeatBet = roundInfo.getTotalBets().toCents();
                            if (totalSeatBet > 0L) {
                                playersWithBetsCounter++;
                            }

                            int seatId = seat.getNumber();
                            long totalSeatWin = (long) roundInfo.getTotalPayouts().toDoubleCents();

                            seatsWinsMap.put(seatId, totalSeatWin);

                            getLog().debug("processBattleGroundMode: seat={}, totalSeatWin={}",
                                    seat.getAccountId(), totalSeatWin);
                        }

                    } else {
                        getLog().debug("processBattleGroundMode: seater without buiIn found: seat={}, seat.getPlayerInfo(): {}",
                                seat.getAccountId(), seat.getPlayerInfo());
                    }
                }
            }
        }

        long totalPotForWin = getBattlegroundWin(roomBuyInSummary, rakePercent);

        List<IBgPlace> places = new ArrayList<>();
        MutableInt rate = new MutableInt();

        final Map<Integer, Long> seatsWinsMapSorted = seatsWinsMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)
                );

        final BattlegroundModeStatus status = kingOfHillSeat != null ?
                BattlegroundModeStatus.COMPLETED : BattlegroundModeStatus.CANCELLED;

        final boolean allPlayersHaveZeroScore =
                playersWithBetsCounter == 0 ||
                        (winnersCounter == 0 && playersWithBetsCounter > 0);

        long potForWinner = splitBattlegroundWinByWinners(totalPotForWin,
                allPlayersHaveZeroScore ? allPlayersCounter : winnersCounter);

        for (Map.Entry<Integer, Long> entry : seatsWinsMapSorted.entrySet()) {
            int seatNr = entry.getKey();

            SEAT seat = getSeat(seatNr);

            long winAmount = seat.isKingOfHill() || allPlayersHaveZeroScore ? potForWinner : 0;

            IActionGamePlayerRoundInfo roundInfo = getCurrentPlayerRoundInfo(seat);
            long winSum = (long) roundInfo.getTotalPayouts().toDoubleCents();

            Money totalBets = roundInfo.getTotalBets().add(roundInfo.getTotalBetsSpecialWeapons());

            IBattlegroundRoomPlayerInfo playerInfo = seat.getPlayerInfo();

            IBgPlace bgPlace = playerInfo.createBattlegroundRoundInfo(
                    playerInfo.getRoundBuyInAmount(),
                    winAmount,
                    (long) totalBets.toDoubleCents(),
                    winSum,
                    status.name(),
                    seat.getNumber(),
                    winnerName.toString(),
                    seat.getAccountId(),
                    rate.getValue(),
                    playerInfo.getGameSessionId(),
                    winSum,
                    roomInfo.getRoundId(),
                    roomInfo.getLastRoundStartDate(),
                    -1D,
                    roomInfo.getPrivateRoomId());

            places.add(bgPlace);
            //need save because getPlayerInfo().createBattlegroundRoundInfo() initialize battlegroundRoundInfo
            savePlayerInfo(playerInfo);

            rate.getAndIncrement();
        }

        //need fix rank in places
        places.sort(Comparator.comparingLong(IBgPlace::getWin)
                .thenComparingLong(IBgPlace::getWinSum)
                .thenComparingLong(IBgPlace::getBetsSum)
                .reversed());

        int rateIndex = 1;
        for (IBgPlace place : places) {
            if (kingOfHillSeat != null) {
                int rank = winnersCounter-- > 0 ? 1 : ++rateIndex;
                place.setRank(rank);
            } else {
                //no winner, all have same rank
                place.setRank(1);
            }
        }

        long totalBetOfPlayersInPlaces = places.size() * roomBuyInCents;

        if (roomBuyInSummary != totalBetOfPlayersInPlaces) {
            getLog().warn("processBattleGroundMode: strange case found, roomBuyInSummary: {} is  differ from waiting amount {}",
                    roomBuyInSummary, totalBetOfPlayersInPlaces);
        }

        getLog().debug("processBattleGroundMode: totalBetOfPlayersInPlaces={}, totalPotForWin: {}, status: {}",
                totalBetOfPlayersInPlaces, totalPotForWin, status);
        getLog().debug("processBattleGroundMode: places={}", places);

        for (IBgPlace place : places) {
            SEAT seatByAccountId = getSeatByAccountId(place.getAccountId());
            IBattlegroundRoundInfo battlegroundRoundInfo = seatByAccountId.getPlayerInfo().getBattlegroundRoundInfo();
            battlegroundRoundInfo.setPlaces(places);

            long potForSeat = seatByAccountId.isKingOfHill() || allPlayersHaveZeroScore ? potForWinner : 0;

            IBattlegroundRoundResult battlegroundRoundResult = getTOFactoryService().createBattlegroundRoundResult(
                    seatByAccountId.getNumber(),
                    place.getGameScore(),
                    place.getRank(),
                    potForSeat,
                    "");

            battlegroundRoundResults.add(battlegroundRoundResult);
        }

        getLog().debug("processBattleGroundMode: battlegroundRoundResults={}", battlegroundRoundResults);
        return battlegroundRoundResults;
    }

    @Override
    public void sitOutAllPlayersWithoutConfirmedRebuy() {
        for (SEAT seat : getAllSeats()) {
            if (seat == null || seat.getSocketClient() == null || seat.isSitOutStarted()) {
                continue;
            }
            ILobbySession lobbySession = lobbySessionService.get(seat.getSocketClient().getSessionId());
            try {
                if (lobbySession == null) {
                    processSitOut(seat.getSocketClient(), null, getSeatNumber(seat), seat.getAccountId(), false);
                }
                if (lobbySession != null && (!lobbySession.isConfirmBattlegroundBuyIn() || seat.getPlayerInfo().getRoundBuyInAmount() == 0)) {
                    getLog().debug("sitOutAllPlayersWithoutConfirmedRebuy: found seat without rebuy, number={}, getRoundBuyInAmount: {}",
                            getSeatNumber(seat), seat.getPlayerInfo().getRoundBuyInAmount());
                    processSitOut(seat.getSocketClient(), null, getSeatNumber(seat), seat.getAccountId(), false);
                }
            } catch (CommonException e) {
                getLog().error("sitOutAllPlayersWithoutConfirmedRebuy: sitOut failed, seat.accountId={}", seat.getAccountId(), e);
            }
        }
    }

    @Override
    public void sitOutAllDisconnectedPlayers() {
        for (SEAT seat : getAllSeats()) {
            if (seat == null || seat.getSocketClient() == null || seat.isSitOutStarted()) {
                continue;
            }
            ILobbySession lobbySession = lobbySessionService.get(seat.getSocketClient().getSessionId());
            try {
                if (lobbySession == null) {
                    getLog().debug("sitOutAllDisconnectedPlayers: Start sitOutAllDisconnectedPlayers for accountId: {}", seat.getAccountId());
                    processSitOut(seat.getSocketClient(), null, getSeatNumber(seat), seat.getAccountId(), false);
                }
            } catch (CommonException e) {
                getLog().error("sitOutAllDisconnectedPlayers: sitOutAllDisconnectedPlayers failed, seat.accountId={}", seat.getAccountId(), e);
            }
        }
    }

    @Override
    protected boolean isSitOutNotAllowed(SEAT seat) {
        if (!isSeaterWithConfirmedByuIn(seat) && seat.getPlayerInfo().getRoundBuyInAmount() == 0) {
            return false;
        }
        return !(getRealSeatsCount() <= 1 || getGameState().isBattlegroundSitOutAllowed());
    }

    private boolean isSeaterWithConfirmedByuIn(SEAT seat) {
        if (seat == null) {
            return false;
        }
        IGameSocketClient socketClient = seat.getSocketClient();
        if (socketClient == null) {
            return false;
        }
        ILobbySession lobbySession = lobbySessionService.get(seat.getSocketClient().getSessionId());
        return lobbySession != null && lobbySession.isConfirmBattlegroundBuyIn();
    }

    @Override
    public void sentBattlegroundMessageToPlayers(List<Integer> oldKings, int rid) {
        Pair<List<Integer>, List<IBattleScoreInfo>> currentKings = getCurrentKings();
        List<Integer> newKings = currentKings.getKey();
        if (!(oldKings.size() == newKings.size() && oldKings.containsAll(newKings))) {
            sendChanges(getTOFactoryService()
                    .createKingOfHillChanged(System.currentTimeMillis(), SERVER_RID, newKings));
        }
        sendChanges(getTOFactoryService().createBattlegroundScoreBoard(gameState.getStartRoundTime(),
                gameState.getEndRoundTime(),
                currentKings.getValue(), getScoreForBoss(), rid));
    }

    public Map<Integer, Long> getScoreForBoss() {
        Map<Integer, Long> scoreByBoss = new HashMap<>();
        for (SEAT seat : getSeats()) {
            if (seat != null) {
                scoreByBoss.put(getSeatNumber(seat), seat.getTotalBossPayout());
            }
        }
        return scoreByBoss;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void innerProcessShot(SEAT seat, IShot shot, boolean isInternalShot) throws CommonException {
        assertRoomStarted();
        long now = System.currentTimeMillis();
        MoneyType moneyType = getRoomInfo().getMoneyType();
        IGameConfig config = getGame().getGameConfig(getId());

        if (getGameType().isCheckWeaponPrices() && config != null
                && config.getWeaponPrices().get(shot.getWeaponId()) != shot.getWeaponPrice()) {
            seat.sendMessage(getTOFactoryService().createError(WRONG_WEAPON,
                    "wrong weapon price", System.currentTimeMillis(),
                    shot.getRid()), shot);
            return;
        }

        if (moneyType.equals(MoneyType.FRB)) {
            if (seat.getBetLevel() > 1 || shot.isPaidSpecialShot()) {
                seat.sendMessage(getTOFactoryService().createError(WRONG_WEAPON,
                        "paid shots is not allowed in bonus modes", System.currentTimeMillis(),
                        shot.getRid()), shot);
                return;
            }
        }

        if (!seat.getStake().equals(roomInfo.getStake())) {
            getLog().error("innerProcessShot: Cannot make shot, found bad stake seat.getStake()={}, roomInfo.getStake(): {}",
                    seat.getStake(), roomInfo.getStake());
            seat.sendMessage(getTOFactoryService().createError(WRONG_WEAPON,
                    "wrong player stake", System.currentTimeMillis(),
                    shot.getRid()), shot);
            return;
        }

        Money totalBetsBefore = seat.getCurrentPlayerRoundInfo().getTotalBets();
        Pair<List<Integer>, List<IBattleScoreInfo>> currentKingOfHillSeatId = getCurrentKings();
        List<Integer> kingIdForBattleGroundOld = currentKingOfHillSeatId.getKey();

        ILobbySession lobbySession = lobbySessionService.get(seat.getAccountId());
        if (lobbySession != null && !lobbySession.isConfirmBattlegroundBuyIn()) {
            getLog().debug("innerProcessShot: processShot error in BG mode: lobbySession isConfirmBattlegroundBuyIn is false: {} ", lobbySession);
        }
        IGameState gameState = getGameState();
        if (!(gameState instanceof AbstractActionPlayGameState)) {
            gameState.throwUnsupportedOperationException("Shot");
        }
        @SuppressWarnings("ConstantConditions")
        AbstractActionPlayGameState playGameState = (AbstractActionPlayGameState) gameState;

        if (isInternalShot) {
            playerInfoService.lock(seat.getAccountId());
            getLog().debug("innerProcessShot: lock: {}", seat.getAccountId());
            try {
                playGameState.processShot(seat, shot, true);
            } finally {
                playerInfoService.unlock(seat.getAccountId());
                getLog().debug("innerProcessShot: unlock: {}", seat.getAccountId());
            }
        } else {
            playGameState.processShot(seat, shot, false);
        }

        sentBattlegroundMessageToPlayers(kingIdForBattleGroundOld, SERVER_RID);

        StatisticsManager.getInstance().updateRequestStatistics("GameRoom: processShot",
                System.currentTimeMillis() - now, seat.getPlayerInfo().getSessionId() + ":" + shot.getRid());

        IPlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
        currentPlayerRoundInfo.setRoomRoundId(getRoomInfo().getRoundId());
        Money totalBets = currentPlayerRoundInfo.getTotalBets();
        Money totalPayouts = currentPlayerRoundInfo.getTotalPayouts();
        getLog().debug("innerProcessShot: getPossibleBalanceAmount: {} ", seat.getPossibleBalanceAmount());
        getLog().debug("innerProcessShot: Shot :: AID: {}, Round Win: {},  getRebuyFromWin: {}, VBA totalBetsBefore: {}, " +
                        "VBA totalBets: {}, VBA totalPayouts: {}, getLastWin: {}, ammo={}", seat.getAccountId(),
                seat.getRoundWin(), seat.getRebuyFromWin(), totalBetsBefore, totalBets, totalPayouts, seat.getLastWin(),
                seat.getAmmoAmount());
    }

    @Override
    public void sendRoundResults() {
        List<ITransportSeat> transportSeats = getTransportSeats();

        List<IBattlegroundRoundResult> battlegroundRoundResults = processBattleGroundMode();

        List<Pair<ISeat, IRoundResult>> seatsRoundResultsPairs = new ArrayList<>();

        List<SEAT> seats = getAllSeats();

        if (seats != null) {
            for (SEAT seat : seats) {
                if (seat != null) {
                    if (!seat.isDisconnected()) {
                        IRoundResult roundResult = getRoundResult(seat, transportSeats, battlegroundRoundResults);

                        seatsRoundResultsPairs.add(new Pair<>(seat, roundResult));

                        seat.sendMessage(roundResult);

                    } else {
                        getLog().warn("sendRoundResults: Stop send round result, seat is disconnected");
                    }
                }
            }

            List<Map<String, Object>> roundResults = analyticsDBClientService.prepareBattlegroundRoundResults(seatsRoundResultsPairs, this);
            asyncExecutorService.execute(() ->
                    analyticsDBClientService.saveRoundResults(roundResults)
            );

            getLog().debug("sendRoundResults end");

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

                    updatePlayersStatus(clients, Status.WAITING, false, false);

                } else {
                    getLog().debug("sendRoundResults: Room is not private skip updatePlayersStatus");
                }
            }
        }
    }

    private IRoundResult getRoundResult(SEAT seat, List<ITransportSeat> transportSeats, List<IBattlegroundRoundResult> battlegroundRoundResults) {
        IPlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
        roundInfo.updateStatOnEndRound(seat.getAmmoAmountTotalInRound(), seat.getCurrentScore(), seat.getAmmoAmount());

        IExperience prevScore = seat.getPlayerInfo().getPrevXP();
        int prevLevel = AchievementHelper.getPlayerLevel(prevScore);
        int level = AchievementHelper.getPlayerLevel(seat.getTotalScore());

        long totalKillsXP = seat.getCurrentScore().getLongAmount() - seat.getTotalTreasuresXPAsLong();

        double winAmount = seat.getRoundWin().add(seat.getRebuyFromWin()).toDoubleCents();

        ILevelInfo beforeRoundLevelInfo = getTOFactoryService().createLevelInfo(prevLevel, prevScore.getLongAmount(),
                AchievementHelper.getXP(prevLevel), AchievementHelper.getXP(prevLevel + 1));

        ILevelInfo afterRoundLevelInfo = getTOFactoryService().createLevelInfo(level, seat.getTotalScore().getLongAmount(),
                AchievementHelper.getXP(level), AchievementHelper.getXP(level + 1));

        IRoundResult roundResult = getTOFactoryService().createRoundResult(
                getCurrentTime(),
                TObject.SERVER_RID,
                winAmount,
                seat.getRebuyFromWin().toDoubleCents(),
                0,
                seat.getCurrentScore().getLongAmount(),
                seat.getTotalScore().getLongAmount(),
                seat.getHitCount(),
                seat.getMissCount(),
                nextMapId,
                transportSeats,
                seat.getEnemiesKilledCount(),
                seat.getRoundWinInCredits(),
                seat.getAmmoAmount(),
                seat.getStake().multiply(seat.getAmmoAmount()).toDoubleCents(),
                seat.getStake().multiply(seat.getAmmoAmountTotalInRound()).toDoubleCents(),
                AchievementHelper.getXP(level),
                seat.getWeaponSurplus(),
                totalKillsXP,
                seat.getTotalTreasuresCount(),
                seat.getTotalTreasuresXPAsLong(),
                beforeRoundLevelInfo,
                afterRoundLevelInfo,
                0,
                seat.getQuestsCompletedCount(),
                seat.getQuestsPayouts(),
                getRoomInfo().getRoundId(),
                seat.getWeaponsReturned(),
                seat.getBulletsFired(),
                winAmount,
                roundInfo.getFreeShotsWon(),
                roundInfo.getMoneyWheelCompleted(),
                roundInfo.getMoneyWheelPayouts(),
                roundInfo.getTotalDamage(),
                battlegroundRoundResults
        );
        return roundResult;
    }

    @Override
    protected void processSitOutForNonFrbMode(SEAT seat, int ammoAmount,
                                              IActiveCashBonusSession activeCashBonusSession,
                                              boolean bulletsConvertedToMoney,
                                              IGameSocketClient socketClient, IGameSocketClient client,
                                              ISitOut request, int serverId, int seatNumber, int oldSeatNumber,
                                              ITournamentSession tournamentSession, long accountId) throws CommonException {
        boolean needRefundInBattle = getState().equals(RoomState.WAIT) && seat.getAmmoAmount() > 0;

        int seatAmmoAmount = seat.getAmmoAmount();
        Money roundWin = Money.ZERO;
        Money returnedBet = needRefundInBattle ? Money.fromCents(getRoomInfo().getStake().toCents()) : Money.ZERO;
        IBattlegroundRoomPlayerInfo playerInfo = seat.getPlayerInfo();

        IPlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
        roundInfo.updateStatOnEndRound(seat.getAmmoAmountTotalInRound(), seat.getCurrentScore(), seat.getAmmoAmount());
        final IPlayerBet playerBet = roundInfo.getPlayerBet(playerInfo.createNewPlayerBet(), seatAmmoAmount);

        long battleBet = roundInfo.getBattleBet();
        long battleWin = roundInfo.getBattleWin();

        if (battleBet > 0 || battleWin > 0) {
            playerBet.setBet(battleBet);
            playerBet.setWin(battleWin);
            returnedBet = Money.ZERO;
            if (battleWin > 0) {
                roundWin = Money.fromCents(battleWin);
            }
        }

        getLog().debug("processSitOutForNonFrbMode: roundInfo: {}, playerBet: {}, returnedBet: {}, battleBet: {},  battleWin:{} ",
                roundInfo, playerBet, returnedBet, battleBet, battleWin);

        playerInfo.setPendingOperation(true,
                "sitOut, roundWin={}, ammoAmount={} " + roundWin.toCents()
                        + ", ammoAmount=" + ammoAmount + ", returnedBet=" + returnedBet
                        + ", needRefundInBattle: " + needRefundInBattle);

        playerInfoService.put(playerInfo);

        try {
            IPlayerProfile playerProfile = playerProfileService.load(playerInfo.getBankId(), seat.getAccountId());
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
            if (playerInfo.getBattlegroundRoundInfo() == null) {
                // for checking on gs that btg roundInfo != null
                playerInfo.createBattlegroundRoundInfo(roomInfo.getBattlegroundBuyIn(), 0,
                        0, 0, null, seat.getNumber(),
                        null, seat.getAccountId(), 1, playerInfo.getGameSessionId(), seat.getTotalScore().getLongAmount(),
                        roomInfo.getRoundId(), roomInfo.getLastRoundStartDate(), -1D, roomInfo.getPrivateRoomId());
            }
            IPendingOperation pendingOperation = pendingOperationService.get(accountId);
            if (pendingOperation == null) {
                if (roundWin.toCents() > 0 || returnedBet.toCents() > 0) {
                    IAddWinRequest winRequest = getTOFactoryService().createAddWinRequest(playerInfo.getSessionId(), playerInfo.getGameSessionId(), roundWin.toCents(), returnedBet.toCents(),
                            accountId, playerBet, seat.getPlayerInfo().getBattlegroundRoundInfo(), playerInfo.getExternalRoundId(), true);
                    if (winRequest != null) {
                        Map<Long, IAddWinResult> addWinResults = addBatchWin(new HashSet<>(Collections.singleton(winRequest)));
                        IAddWinResult result = addWinResults.get(accountId);
                        if (result == null || !result.isSuccess()) {
                            setPendingWinForPlayer(seat, roundWin, ammoAmount, returnedBet.toCents());
                        }
                    }
                    if (needRefundInBattle && client != null && !client.isKicked()) {

                        getLog().debug("processSitOutForNonFrbMode: needRefundInBattle is true, returnedBet={}, send " +
                                "CancelBattlegroundRound, reason NOT_ENOUGH_PLAYERS", returnedBet.toCents());

                        ICancelBattlegroundRound cancelBattlegroundRound =
                                getTOFactoryService().createCancelBattlegroundRound(returnedBet.toCents(), "NOT_ENOUGH_PLAYERS");
                        seat.sendMessage(cancelBattlegroundRound);

                    }
                } else {
                    boolean sitOutResult = socketService.closeGameSession(serverId, playerInfo.getSessionId(), accountId, playerInfo.getGameSessionId(),
                            getId(), getGameType().getGameId(), playerInfo.getBankId(), roomInfo.getStake().toCents());
                    if (!sitOutResult) {
                        handleCloseGameSessionError(seat, playerInfo, client, request, socketClient, seatNumber, oldSeatNumber);
                        return;
                    }
                }
            } else {
                getLog().warn("processSitOutForNonFrbMode: skip call closeGameSession, found pendingOperation={}", pendingOperation);
            }

            checkResultAndFinishSitOut(null, seat, playerInfo, client, request, roundWin, ammoAmount,
                    socketClient, null, serverId, seatNumber, oldSeatNumber);
        } catch (Exception e) {
            handleSitOutError(e, seat, roundWin, ammoAmount, playerInfo, request, seatNumber, oldSeatNumber, client);
        }
    }

    protected void handleCloseGameSessionError(SEAT seat, IRoomPlayerInfo playerInfo, IGameSocketClient client, ISitOut request, IGameSocketClient socketClient, int seatNumber, int oldSeatNumber) throws CommonException {
        getLog().error("handleCloseGameSessionError: failed, but pending transaction created, rollback not required");
        if (client != null) {
            client.sendMessage(getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION,
                    "close game session operation in progress", getCurrentTime(),
                    request != null ? request.getRid() : TObject.SERVER_RID));
        }
        processFinishSitOut(socketClient, playerInfo, seat, request, seatNumber, oldSeatNumber);
        getGameState().processSitOut(seat);
    }

    @Override
    protected void checkResultAndFinishSitOut(ISitOutResult sitOutResult, SEAT seat, IRoomPlayerInfo playerInfo, IGameSocketClient client,
                                              ISitOut request, Money roundWin, int ammoAmount, IGameSocketClient socketClient,
                                              IActiveCashBonusSession activeCashBonusSession,
                                              int serverId, int seatNumber, int oldSeatNumber) throws CommonException {
        boolean finishSitOut = true;
        if (sitOutResult != null && !sitOutResult.isSuccess()) {
            getLog().warn("checkResultAndFinishSitOut: External call to sitOut return error={}, seat={}", sitOutResult, seat);
            if (sitOutResult.getErrorCode() == TransactionErrorCodes.FOUND_PENDING_TRANSACTION) {
                getLog().error("checkResultAndFinishSitOut: processSitOut: failed, but pending transaction created, " +
                        "rollback not required");
                playerInfo.setPendingOperation(false);
                savePlayerInfo(playerInfo);
                if (client != null) {
                    client.sendMessage(getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION,
                            "Payment operation in progress", getCurrentTime(),
                            request != null ? request.getRid() : TObject.SERVER_RID));
                }
            } else { //process same as doOnError
                getLog().error("checkResultAndFinishSitOut: failed, rollback roundWin={} and ammoAmount={}",
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
            processFinishSitOut(socketClient, playerInfo, seat, request, seatNumber, oldSeatNumber);
        }
        getGameState().processSitOut(seat);
    }

    private void processFinishSitOut(IGameSocketClient socketClient, IRoomPlayerInfo playerInfo, SEAT seat, ISitOut request, int seatNumber, int oldSeatNumber) {
        long nextRoomId = -1;
        String sessionId = socketClient != null ? socketClient.getSessionId() : playerInfo.getSessionId();
        ILobbySession lobbySession = lobbySessionService.get(sessionId);
        if (lobbySession != null) {
            getLog().debug("processFinishSitOut: " +
                            "setConfirmBattlegroundBuyIn to false account={}"
                    , seat.getAccountId());
            lobbySession.setConfirmBattlegroundBuyIn(false);
            lobbySessionService.add(lobbySession);
            getLog().debug("processFinishSitOut: reset setConfirmBattlegroundBuyIn for BTG" +
                    " sid={}, lobbySession={}", playerInfo.getSessionId(), lobbySession);
        }

        finishSitOut(request, seatNumber, seat, oldSeatNumber, nextRoomId, false, false);
        long balance = getBalance(seat);
        getLog().debug("processFinishSitOut: sendNotifyRoundCompleted sid={}, balance={}, xp={}",
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

    protected IAddWinRequest _convertBulletsToMoneyForSeat(SEAT seat, Set<SEAT> wantSitOutCandidates,
                                                           boolean isAllSeatsWithoutShoot, boolean isAllSeatsWithoutPayout) {
        IGameSocketClient socketClient = seat.getSocketClient();
        long accountId = seat.getAccountId();
        IAddWinRequest addWinRequest = null;
        getLog().debug("_convertBulletsToMoneyForSeat: convertBulletsToMoney,  seat.getPlayerInfo(): {}", seat.getPlayerInfo());
        try {
            final IRoomPlayerInfo playerInfoFromService = playerInfoService.get(accountId);
            //may be already sitOut
            if (playerInfoFromService == null) {
                getLog().debug("_convertBulletsToMoneyForSeat: convertBulletsToMoney, after lock player not " +
                        "found in playerInfoService, accountId={}", accountId);
                return null;
            } else if (playerInfoFromService.getRoomId() != getRoomInfo().getId()) {
                getLog().debug("_convertBulletsToMoneyForSeat: convertBulletsToMoney, after lock player found " +
                        "playerInfo for other room, playerInfoFromService={}", playerInfoFromService);
                return null;
            }
            IPendingOperation pendingOperation = pendingOperationService.get(seat.getAccountId());
            if (pendingOperation != null && pendingOperation.getOperationType() == PendingOperationType.ADD_WIN) {
                getLog().debug("_convertBulletsToMoneyForSeat: player {} has pendingOperation already", seat.getAccountId());
                return null;
            }
            final IBattlegroundRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            String sessionId = socketClient != null ? socketClient.getSessionId() : playerInfo.getSessionId();
            long gameSessionId = playerInfo.getGameSessionId();
            getLog().debug("_convertBulletsToMoneyForSeat: seat={}", seat);

            IPlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
            IPlayerBet newPlayerBet = playerInfo.createNewPlayerBet();
            IPlayerBet playerBet = currentPlayerRoundInfo.getPlayerBet(newPlayerBet, -1);

            if (isBattlegroundMode()) {
                ILobbySession lobbySession = null;
                if (socketClient != null && socketClient.getSessionId() != null) {
                    lobbySession = lobbySessionService.get(seat.getAccountId());
                }

                if (lobbySession != null && (!lobbySession.isConfirmBattlegroundBuyIn() || playerInfo.getRoundBuyInAmount() == 0)) {
                    getLog().debug("_convertBulletsToMoneyForSeat: convertBulletsToMoney found player without " +
                            "ConfirmBattlegroundBuyIn, add to wantSitOutCandidates for later sitOut, accountId={}, " +
                            "lobbySession: {}", seat.getAccountId(), lobbySession);
                    seat.setWantSitOut(true);
                    wantSitOutCandidates.add(seat);
                    return null;
                }

                long betBattle = getRoomInfo().getStake().toCents();
                IBattlegroundRoundInfo battlegroundRoundInfo = playerInfo.getBattlegroundRoundInfo();
                if (battlegroundRoundInfo == null) {
                    getLog().error("_convertBulletsToMoneyForSeat: Possible error, battlegroundRoundInfo is null in battleground mode");
                }

                long battleBet = battlegroundRoundInfo != null && !isAllSeatsWithoutShoot ? betBattle : 0;
                long winAmountBattle = battlegroundRoundInfo == null ? 0 : battlegroundRoundInfo.getWinAmount();

                currentPlayerRoundInfo.setBattleBet(battleBet);
                currentPlayerRoundInfo.setBattleWin(winAmountBattle);

                playerBet.setBet(battleBet);
                playerBet.setWin(winAmountBattle);
                playerBet.setStartRoundTime(getGameState().getStartRoundTime());

                getLog().debug("_convertBulletsToMoneyForSeat: convertBulletsToMoney accountId: {}, currentPlayerRoundInfo after update: {} ", currentPlayerRoundInfo, accountId);
                getLog().debug("_convertBulletsToMoneyForSeat: convertBulletsToMoney accountId: {}, playerBet after update: {} ", playerBet, accountId);
                getLog().debug("_convertBulletsToMoneyForSeat: convertBulletsToMoney accountId: {}, battle player bet: betBattle={}, winAmountBattle: {}, battleBet: {}",
                        accountId, winAmountBattle, battleBet, betBattle);
                if (isAllSeatsWithoutPayout && !isAllSeatsWithoutShoot) {
                    playerBet.addData(";refundMoney=" + winAmountBattle + ";");
                }
            }
            boolean noActivity = seat.getAmmoAmountTotalInRound() == 0
                    && playerBet.getBet() == 0 && playerBet.getWin() == 0 && seat.getRoundWin().equals(Money.ZERO)
                    && seat.getAmmoAmount() == 0;

            if (noActivity) {
                getLog().debug("_convertBulletsToMoneyForSeat: Player has no activity in round");
                playerBet.setData("");
            }

            final Money roundWin = Money.fromCents((long) playerBet.getWin());
            final Money returnedBet;

            long betBattle = seat.getPlayerInfo().getRoundBuyInAmount();
            RoomState roomState = getState();
            boolean allSeatsReadyButWithoutActivity = (roomState.equals(RoomState.WAIT) && seat.getBulletsFired() == 0
                    && seat.getAmmoAmount() > 0) ||
                    (roomState.equals(RoomState.QUALIFY) && isAllSeatsWithoutShoot);
            long expectedErrorTime = TimeUnit.SECONDS.toMillis(15L);
            boolean roundFinishedInNotExpectedTime = Math.abs(gameState.getEndRoundTime() - getCurrentTime()) < expectedErrorTime;
            if (allSeatsReadyButWithoutActivity && roundFinishedInNotExpectedTime) {
                returnedBet = Money.fromCents(getBattlegroundWin(betBattle, playerInfo.getBattlegroundRake()));
                currentPlayerRoundInfo.setBattleBet(betBattle);
                long battleWinTie = returnedBet.toCents();
                currentPlayerRoundInfo.setBattleWin(battleWinTie);
                playerBet.setBet(betBattle);
                playerBet.setWin(battleWinTie);
                getLog().debug("_convertBulletsToMoneyForSeat: {}, betBattle: {}, battleWinTie: {} ", accountId, betBattle, battleWinTie);
                playerBet.addData(";refundMoney=" + battleWinTie + ";");
            } else {
                returnedBet = allSeatsReadyButWithoutActivity ? Money.fromCents(betBattle) : Money.ZERO;
            }
            if (roomState.equals(RoomState.WAIT) && seat.getBulletsFired() == 0 && seat.getAmmoAmount() == 0) {
                //player not make Rebuy, need sitOut
                seat.setWantSitOut(true);
                wantSitOutCandidates.add(seat);
            }
            long externalRoundId = seat.getPlayerInfo().getExternalRoundId();
            getLog().debug("_convertBulletsToMoneyForSeat: accountId: {} convertBulletsToMoney for battle: roundWin={}, betBattle={}, " +
                            "returnedBet={}, roomState={}, externalRoundId={}, allSeatsReadyButWithoutActivity:{}, roundFinishedInNotExpectedTime:{} " +
                            "isAllSeatsWithoutShoot={}", accountId, roundWin.toCents(), betBattle,
                    returnedBet.toCents(), roomState, externalRoundId,
                    allSeatsReadyButWithoutActivity, roundFinishedInNotExpectedTime, isAllSeatsWithoutShoot);
            addWinRequest = getTOFactoryService().createAddWinRequest(sessionId, gameSessionId, roundWin.toCents(), returnedBet.toCents(),
                    accountId, playerBet, seat.getPlayerInfo().getBattlegroundRoundInfo(), playerInfo.getExternalRoundId(), false);
        } catch (Exception e) {
            getLog().error("_convertBulletsToMoneyForSeat: ConvertBullets failed for accountId={}", accountId, e);
        }
        IGameSocketClient gameSocketClient = seat.getSocketClient();
        if (seat.isWantSitOut() && !isSeatClientDisconnected(seat)) {
            // reset wantSitOut cause seat played in round
            seat.setWantSitOut(false);
        }
        if (seat.isWantSitOut() || gameSocketClient == null || gameSocketClient.isDisconnected()) {
            wantSitOutCandidates.add(seat);
        }
        return addWinRequest;
    }

    @Override
    public IFullGameInfo getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client) {

        SEAT alreadySeat = null;

        Set<SeatBullet> allBullets = new HashSet<>();

        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                allBullets.addAll(seat.getBulletsOnMap());
                if (seat.getAccountId() == client.getAccountId()) {
                    alreadySeat = seat;
                }
            }
        }

        Map<Integer, Integer> seatGems = alreadySeat == null ? new HashMap<>() : getSeatGems(alreadySeat);
        long timeToStart = getGameState().getTimeToStart();

        sentBattlegroundMessageToPlayers(Collections.singletonList(-1), SERVER_RID);
        Map<Integer, Double> gemPrizes = getGame().getGameConfig(getId()).getGemPrizes(Money.BG_STAKE, getDefaultBetLevel());

        int rid = request != null ? request.getRid() : SERVER_RID;

        IFullGameInfo fullGameInfo = getTOFactoryService().createFullGameInfo(getCurrentTime(), rid, gameState.getCurrentMapId(),
                gameState.getSubround().name(), gameState.getStartTime(), gameState.getRoomState(),
                getLiveRoomEnemies(), getTransportSeats(), getAllMinePlaces(), gameState.getFreezeTimeRemaining(),
                isBossImmortal(client), getRoomInfo().getRoundId(), seatGems,
                alreadySeat == null ? getDefaultBetLevel() : alreadySeat.getBetLevel(), map.getAdditionalEnemyModes(),
                allBullets, timeToStart, getReels(), getCurrentPowerUpMultiplierBySeat(alreadySeat),
                gemPrizes
        );

        List<ITransportObserver> transportObservers = getTransportObservers();
        fullGameInfo.setObservers(transportObservers);

        if (roomInfo.isPrivateRoom()) {

            if (client.isOwner()) {
                List<OnlinePlayer> onlinePlayersNotInPrivateRoom =
                        privateRoomPlayersStatusService.getOnlinePlayersNotInPrivateRoom(roomInfo);

                List<com.betsoft.casino.mp.model.onlineplayer.Friend> friendsNotInPrivateRoom =
                        privateRoomPlayersStatusService.convertOnlinePlayersToGameInfoFriends(onlinePlayersNotInPrivateRoom);

                List<com.betsoft.casino.mp.model.onlineplayer.Friend> onlineFriendsNotInPrivateRoom =
                        friendsNotInPrivateRoom.stream()
                                .filter(f -> f.isOnline())
                                .collect(Collectors.toList());

                fullGameInfo.setFriends(onlineFriendsNotInPrivateRoom);
            }
        }

        return fullGameInfo;
    }

    @Override
    public IGetRoomInfoResponse getRoomInfoResponse(int requestId, IGameSocketClient client, String playerCurrency)
            throws CommonException {

        this.checkAndStartRoom();

        SEAT alreadySeat = null;
        Set<SeatBullet> allBullets = new HashSet<>();

        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                allBullets.addAll(seat.getBulletsOnMap());
                if (seat.getAccountId() == client.getAccountId()) {
                    alreadySeat = seat;
                }
            }
        }

        float minBuyIn = getRoomInfo().getMinBuyIn();
        double stake = getRoomInfo().getStake().toCents();

        getLog().debug("getRoomInfoResponse: room.stake={}, player currency={}, seatNumber={}",
                getRoomInfo().getStake().toCents(), playerCurrency,
                (alreadySeat == null ? -1 : alreadySeat.getNumber()));

        Map<Integer, Integer> seatGems = alreadySeat == null ? new HashMap<>() : getSeatGems(alreadySeat);

        long alreadySitInWin = alreadySeat == null ? 0 : (long) alreadySeat.getRoundWin().toDoubleCents();

        ILobbySession lobbySession = lobbySessionService == null ? null :
                lobbySessionService.get(client.getSessionId());

        boolean confirmBattlegroundBuyIn = lobbySession != null && lobbySession.isConfirmBattlegroundBuyIn();
        long timeToStart = getGameState().getTimeToStart();
        int timeToStartSec = 0;
        long currentTimeMillis = System.currentTimeMillis();

        if (timeToStart > currentTimeMillis && timeToStart != Long.MAX_VALUE) {
            timeToStartSec = (int) ((timeToStart - currentTimeMillis) / 1000);
        }

        long buyInAmountOfEachSeat = getRoomInfo().getStake().toCents();
        long pot = getRealSeatsCount() * buyInAmountOfEachSeat;
        double potTaxPercent = lobbySession != null ? lobbySession.getBattlegroundRakePercent() : 0;
        List<Integer> kingsOfHill = new ArrayList<>();

        for (SEAT seat : getAllSeats()) {
            if (seat != null && seat.isKingOfHill()) {
                kingsOfHill.add(seat.getNumber());
            }
        }

        IRoomBattlegroundInfo roomBattlegroundInfo = getTOFactoryService()
                .createRoomBattlegroundInfo(getRoomInfo().getStake().toCents(), confirmBattlegroundBuyIn, timeToStartSec,
                kingsOfHill.isEmpty() ? Collections.singletonList(-1) : kingsOfHill,
                0, 0, pot, potTaxPercent, this.roomInfo.getJoinUrl(), getRealConfirmedSeatsId());

        List<ITransportObserver> transportObservers = getTransportObservers();
        roomBattlegroundInfo.setObservers(transportObservers);

        int betLevel = getDefaultBetLevel();
        if (alreadySeat != null) {
            betLevel = alreadySeat.getBetLevel();
        }

        Map<Integer, Double> gemPrizes = getGame()
                .getGameConfig(getId())
                .getGemPrizes(Money.BG_STAKE, getDefaultBetLevel());

        IGetRoomInfoResponse getRoomInfoResponse = getTOFactoryService()
                .createGetRoomInfoResponse(getCurrentTime(), getId(), requestId,
                getName(), getMaxSeats(), minBuyIn, stake, stake,
                getState(), getTransportSeats(), getTimeToNextState(), this.roomInfo.getWidth(), this.roomInfo.getHeight(),
                getTransportEnemies(), getLiveRoomEnemies(),
                alreadySeat == null ? -1 : alreadySeat.getNumber(),
                alreadySeat == null ? 0 : alreadySeat.getAmmoAmount(),
                alreadySeat == null ? 0 : getBalance(alreadySeat),
                alreadySitInWin,
                gameState.getCurrentMapId(),
                gameState.getSubround().name(),
                GameType.getAmmoValues(getRoomInfo().getMoneyType(), getRoomInfo().getStake().toFloatCents()),
                getAllMinePlaces(), gameState.getFreezeTimeRemaining(), false, getRoomInfo().getRoundId(),
                seatGems, null, null,
                betLevel, getMap().getAdditionalEnemyModes(), allBullets,
                roomBattlegroundInfo, getReels(), gemPrizes
        );

        IRoomInfo roomInfo = getRoomInfo();

        if (roomInfo.isPrivateRoom()) {
            getRoomInfoResponse.getBattlegroundInfo().setObservers(roomBattlegroundInfo.getObservers());
            getRoomInfoResponse.setOwner(client.isOwner());
            getRoomInfoResponse.setKicked(client.isKicked());
            getLog().debug("getRoomInfoResponse: set Owner client {}", client);
        }

        return getRoomInfoResponse;
    }

    @Override
    public void sendStartNewRoundToAllPlayers(List<ISeat> seats) {
        try {
            List<IStartNewRoundResult> iStartNewRoundResults = socketService.startNewRoundForManyPlayers(seats, roomInfo.getId(),
                    roomInfo.getRoundId(), roomInfo.getLastRoundStartDate(), roomInfo.isBattlegroundMode(),
                    roomInfo.isBattlegroundMode() ? roomInfo.getBattlegroundBuyIn() : roomInfo.getStake().toCents());
            for (IStartNewRoundResult newRoundResult : iStartNewRoundResults) {
                getLog().debug("sendStartNewRoundToAllPlayers: newRoundResult={}", newRoundResult);
                long newRoundId = newRoundResult.getPlayerRoundId();
                //newRoundId not generated for Battleground mode and equal to 0, need ignore
                SEAT seat = getSeatByAccountId(newRoundResult.getAccountId());
                if (newRoundId > 0 && newRoundResult.isSuccess()) {
                    if (seat == null) {
                        getLog().error("sendStartNewRoundToAllPlayers: seat is null for accountId={}", newRoundResult.getAccountId());
                        continue;
                    }
                    IBattlegroundRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                    if (playerInfo == null) { //impossible, remove after check
                        getLog().error("sendStartNewRoundToAllPlayers: playerInfo is null for seat={}", seat);
                        continue;
                    }
                    playerInfo.setExternalRoundId(newRoundId);
                    seat.updatePlayerRoundInfo(newRoundId);
                    playerInfoService.put(playerInfo);
                }
                getLog().debug("sendStartNewRoundToAllPlayers: success accountId: {}", seat.getAccountId());
            }
        } catch (Exception e) {
            getLog().error("sendStartNewRoundToAllPlayers: error, seat={}", seats, e);
        }
    }

    @Override
    public void convertBulletsToMoney() {
        lock();
        try {
            assertRoomStarted();
            long now = System.currentTimeMillis();
            Set<SEAT> seatsForProcess = new HashSet<>(getSeats());
            Set<SEAT> wantSitOutCandidates = new HashSet<>();
            boolean isAllSeatsWithoutShoot = isAllSeatsWithoutShoot(seatsForProcess);
            boolean isAllSeatsWithoutPayout = isAllSeatsWithoutPayout(seatsForProcess);
            Map<Long, IAddWinRequest> winRequests = new HashMap<>(seatsForProcess.size());
            for (SEAT seat : seatsForProcess) {
                IAddWinRequest winRequest = _convertBulletsToMoneyForSeat(seat, wantSitOutCandidates, isAllSeatsWithoutShoot, isAllSeatsWithoutPayout);
                if (winRequest != null) {
                    winRequests.put(seat.getAccountId(), winRequest);
                }
            }
            if (!winRequests.isEmpty()) {
                Map<Long, IAddWinResult> addWinResults = addBatchWin(new HashSet<>(winRequests.values()));
                for (SEAT seat : seatsForProcess) {
                    if (getRoomInfo().isPrivateRoom() && getRoomInfo().isDeactivated()) {
                        seat.setWantSitOut(true);
                    }
                    if (winRequests.containsKey(seat.getAccountId())) {
                        long accountId = seat.getAccountId();
                        IAddWinResult result = addWinResults.get(accountId);
                        final int ammoAmount = getAmmoAmount(seat);
                        IAddWinRequest request = winRequests.get(accountId);
                        final Money roundWin = Money.fromCents(request.getWinAmount());
                        long returnedBet = request.getReturnedBet();
                        if (result != null) {
                            int seatNumber = getSeatNumber(seat);
                            IGameSocketClient socketClient = seat.getSocketClient();
                            String sessionId = socketClient != null ? socketClient.getSessionId() : seat.getPlayerInfo().getSessionId();
                            handleAddWinResult(result, seat, socketClient, accountId,
                                    roundWin, ammoAmount, seatNumber, wantSitOutCandidates, sessionId,
                                    Money.fromCents(returnedBet), null);
                        } else {
                            setPendingWinForPlayer(seat, roundWin, ammoAmount, returnedBet);
                        }
                    }
                }
            }
            if (!seatsForProcess.isEmpty()) {
                updateRoomInfo();
            }
            try {
//            addSitOutCandidatesIfFoundAnyPendingOperation(wantSitOutCandidates);
                for (SEAT sitOutCandidate : wantSitOutCandidates) {
                    getLog().info("convertBulletsToMoney: sitOut disconnected seat={}", sitOutCandidate);
                    processSitOut(sitOutCandidate.getSocketClient(), null, sitOutCandidate.getNumber(),
                            sitOutCandidate.getAccountId(), false, true);
                }
                StatisticsManager.getInstance().updateRequestStatistics("GameRoom::convertBulletsToMoney",
                        System.currentTimeMillis() - now, "" + getRoomInfo().getId() + ":" + getRoomInfo().getRoundId());
                getLog().debug("convertBulletsToMoney: all seats process");
            } catch (Exception e) {
                getLog().error("convertBulletsToMoney: Interrupted", e);
            }
        } finally {
            unlock();
        }
    }

    @Override
    public int processSitIn(SEAT seat, ISitIn request) throws CommonException {
        int result = super.processSitIn(seat, request);

        this.sendGameInfoToAllObservers();

        return result;
    }

    @Override
    public SEAT processSitOut(IGameSocketClient client, ISitOut request, int seatNumber, long accountId,
                              boolean updateStats, boolean bulletsConvertedToMoney) {
        if (getRoomInfo().isPrivateRoom() && getRoomInfo().isDeactivated() && client != null) {
            client.sendMessage(getTOFactoryService().createError(ErrorCodes.ROOM_WAS_DEACTIVATED, "Room was deactivated",
                    getCurrentTime(), TObject.SERVER_RID));
        }

        SEAT seat = super.processSitOut(client, request, seatNumber, accountId, updateStats, bulletsConvertedToMoney);

        this.sendGameInfoToAllObservers();

        return seat;
    }

    protected void finishSitOut(ISitOut request, int seatNumber, SEAT seat, int oldSeatNumber, long nextRoomId,
                                boolean hasNextFrb, boolean frbSitOut) {
        super.finishSitOut(request, seatNumber, seat, oldSeatNumber, nextRoomId, hasNextFrb, frbSitOut);
        if (getRoomInfo().isPrivateRoom() && getRoomInfo().isDeactivated()) {
            try {
                processCloseRoom(seat.getAccountId());
            } catch (CommonException e) {
                getLog().debug("processSitOut: cannot close room accountId: {}, roomId:{}", seat.getAccountId(), roomInfo.getPrivateRoomId());
            }
        }
    }

    private void sendErrorMessage(IGameSocketClient client, int errorCode, String message) {
        IError errorMessage = getTOFactoryService().createError(errorCode, message, getCurrentTime(), TObject.SERVER_RID);
        getLog().error("sendErrorMessage: error, message=  {}, client={}", errorMessage, client);
        client.sendMessage(errorMessage);
    }

    @Override
    public void setGameState(IGameState newState) throws CommonException {
        super.setGameState(newState);
        updateRoomInfoForDeactivation();
        if (newState.isAllowedRemoving() && getRoomInfo().isPrivateRoom() && getRoomInfo().isDeactivated()) {
            removePlayersFromPrivateRoom();
        }
    }

    public void updateRoomInfoForDeactivation() {
        updateRoomInfo(newRoomInfo -> {
            getRoomInfo().setDeactivated(roomInfoService.getRoom(getRoomInfo().getId()).isDeactivated());
        });
    }

    public void removePlayersFromPrivateRoom() {
        try {
            Collection<IGameSocketClient> observes = getObservers();
            getLog().debug("removePlayersFromPrivateRoom: Room observers: {}", observes);
            for (IGameSocketClient client : observes) {
                if (client.getSeatNumber() == -1) {
                    sendErrorMessage(client, ErrorCodes.ROOM_WAS_DEACTIVATED, "Room was deactivated");
                } else {
                    IBattlegroundSeat seat = getSeatByAccountId(client.getAccountId());
                    IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                    processSitOut(seat.getSocketClient(), null, playerInfo.getSeatNumber(), seat.getAccountId(), true);
                    super.processCloseRoom(seat.getAccountId());
                }
            }
        } catch (Exception e) {
            getLog().error("removePlayersFromPrivateRoom: Unable to remove players: ", e);
        }
    }

    private void sitOutAllPlayers() {
        try {
            for (SEAT seat : getAllSeats()) {
                IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                processSitOut(seat.getSocketClient(), null, playerInfo.getSeatNumber(), seat.getAccountId(), true);
                super.processCloseRoom(seat.getAccountId());
                getLog().debug("sitOutAllPlayers: SitOut call remove roomId: {} accountId: {} seatNumber: {}",
                        getId(), seat.getAccountId(), playerInfo.getSeatNumber());
            }
        } catch (CommonException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<Long, IAddWinResult> addBatchWin(Set<IAddWinRequest> winRequests) {
        IRoomInfo roomInfo = getRoomInfo();
        return socketService.addBatchWin(roomInfo.getId(), roomInfo.getRoundId(), getGameType().getGameId(), winRequests, roomInfo.getBankId(),
                TimeUnit.SECONDS.toMillis(3));
    }

    private void updateRoomInfo() {
        if (roomInfo.isPrivateRoom()) {
            long roomId = roomInfo.getId();
            roomInfoService.lock(roomId);
            try {
                //roomInfo.updateLastTimeActivity();
                roomInfo.incrementCountGamesPlayed();
                roomInfo.setDeactivated(roomInfoService.getRoom(roomId).isDeactivated());
                roomInfoService.update(roomInfo);

            } finally {
                roomInfoService.unlock(roomId);
            }
        }
    }

    @Override
    protected void processSuccessAddWin(SEAT seat, long accountId, IAddWinResult addWinResult, IGameSocketClient socketClient, int seatNumber,
                                        Set<SEAT> wantSitOutCandidates, String sessionId, Money returnedBet) {
        getLog().debug("processSuccessAddWin: success, reset seat player info data, old playerRoundInfo: " +
                "{}", seat.getCurrentPlayerRoundInfo());
        IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
        if (roomPlayerInfo == null) {
            getLog().error("processSuccessAddWin: cannot load roomPlayerInfo from " +
                    "playerInfoService, accountId={}", accountId);
            //roomPlayerInfo = seat.getPlayerInfo();
            //getLog().error("convertBulletsToMoney: loaded roomPlayerInfo from seat");
        } else {
            seat.initCurrentRoundInfo(roomPlayerInfo);
            getLog().debug("processSuccessAddWin: new playerRoundInfo: {}", seat.getCurrentPlayerRoundInfo());
            roomPlayerInfo.finishCurrentRound();
            roomPlayerInfo.setExternalRoundId(0);
            seat.updatePlayerRoundInfo(0);
            seat.setKingOfHill(false);
            seat.setAmmoAmount(0);
            seat.setRoundWin(Money.ZERO);
            getLog().debug("processSuccessAddWin: reset seat: {}", seat);
            setBalance(seat, addWinResult.getBalance());
            if (addWinResult.isSuccess()) {
                roomPlayerInfo.setPendingOperation(false);
                savePlayerInfo(roomPlayerInfo);
            }
            if (socketClient != null) {
                setBalance(seat, addWinResult.getBalance());
                try {
                    socketClient.sendMessage(getTOFactoryService().createBalanceUpdated(
                            getCurrentTime(),
                            0,
                            seat.getAmmoAmount()));
                } catch (Exception exc) {
                    getLog().error("processSuccessAddWin: Cannot send BalanceUpdated message", exc);
                }
            }
            getLog().debug("processSuccessAddWin: after addWin, roomPlayerInfo={}, " +
                    "addWinResult={}", roomPlayerInfo, addWinResult);
            if (addWinResult.isPlayerOffline() || seat.isWantSitOut()) {
                getLog().warn("processSuccessAddWin: seat '{}' is offline, " +
                        "need force sitOut", seatNumber);
                wantSitOutCandidates.add(seat);
            }
            //need change locker before put
            playerInfoService.forceUnlock(accountId);
            getLog().debug("processSuccessAddWin: convertBulletsToMoney 9 forceUnlock : {}", accountId);
            playerInfoService.lock(accountId);
            getLog().debug("processSuccessAddWin: convertBullet 5 HS lock: {}", accountId);
            playerInfoService.put(roomPlayerInfo);
            seat.setPlayerInfo(roomPlayerInfo);
            createRoundCompletedTask(seat, sessionId, roomPlayerInfo, addWinResult.getBalance());

            if (socketClient != null) {
                finishProcessAddWin(seat, socketClient, returnedBet);
            }
        }
    }

    public void setPendingWinForPlayer(SEAT seat, Money roundWin, int ammoAmount, long returnedBet) {
        super.setPendingWinForPlayer(seat, roundWin, ammoAmount, returnedBet);
        IGameSocketClient socketClient = seat.getSocketClient();
        if (socketClient != null) {
            finishProcessAddWin(seat, socketClient, Money.fromCents(returnedBet));
        } else {
            getLog().debug("setPendingWinForPlayer: socketClient=null for accountId={}"
                    , seat.getAccountId());
        }
    }

    public void finishProcessAddWin(SEAT seat, IGameSocketClient socketClient, Money returnedBet) {
        ILobbySession lobbySession = lobbySessionService.get(socketClient.getSessionId());
        //lobby session may be already removed
        if (lobbySession != null && lobbySession.isConfirmBattlegroundBuyIn()) {
            getLog().debug("finishProcessAddWin: setConfirmBattlegroundBuyIn to false accountId={}", seat.getAccountId());
            lobbySession.setConfirmBattlegroundBuyIn(false);
            seat.setBetLevel(getDefaultBetLevel());
            lobbySessionService.add(lobbySession);
            if (returnedBet.greaterThan(Money.ZERO)) {
                getLog().debug("finishProcessAddWin: returnedBet={} > 0, send CancelBattlegroundRound, reason NOT_ENOUGH_PLAYERS",
                        returnedBet.toCents());

                ICancelBattlegroundRound cancelBattlegroundRound =
                        getTOFactoryService().createCancelBattlegroundRound(returnedBet.toCents(), "NOT_ENOUGH_PLAYERS");

                socketClient.sendMessage(cancelBattlegroundRound);
            }
        }
    }

    @Override
    protected int getMaxObservers() {
        return getGameType().getMaxSeats();
    }

    @Override
    public boolean isNotAllowPlayWithAnyPendingPlayers() {
        return false;
    }

    @Override
    public void saveMinesWithLock(SEAT seat) {
    }

    @Override
    public ITransportObject processOpenRoom(IGameSocketClient client, IOpenRoom request, String currency)
            throws CommonException {

        if (roomInfo.isPrivateRoom()) {
            client.setKicked(roomInfo.isPlayerKicked(client.getAccountId()));
        }

        ITransportObject message = super.processOpenRoom(client, request, currency);

        if (roomInfo.isPrivateRoom() && !(message instanceof IError)) {
            sendRoomWasOpenedToOwner(client.getNickname(), client.isKicked());
            if(!client.isKicked()) {
                updatePlayerStatusForObserver(client.getNickname(), Status.WAITING);
            }
        }

        return message;
    }

    @Override
    public void removeObserverByAccountId(long accountId) {
        IGameSocketClient client = observePlayers.get(accountId);
        String nickname = client != null ? client.getNickname() : null;

        super.removeObserverByAccountId(accountId);

        if (roomInfo.isPrivateRoom()) {

            if(!StringUtils.isTrimmedEmpty(nickname)) {
                IObserverRemoved message =
                        getTOFactoryService().createObserverRemoved(System.currentTimeMillis(), SERVER_RID, nickname);
                IGameSocketClient owner = getOwnerClient();
                if (owner != null) {
                    owner.sendMessage(message);
                }
            }
        }  else {

            sendGameInfoToAllObservers();
        }
    }

    private void sendRoomWasOpenedToOwner(String newPlayerNickname, boolean newPlayerKick) {
        IRoomWasOpened message = getTOFactoryService().createRoomWasOpened(System.currentTimeMillis(), SERVER_RID, newPlayerNickname, newPlayerKick);
        IGameSocketClient owner = getOwnerClient();
        if (owner != null) {
            owner.sendMessage(message);
        }
    }

    private SEAT getOwnerSeat() {
        for (SEAT seat : getAllSeats()) {
            if (seat != null && seat.isOwner()) {
                return seat;
            }
        }
        return null;
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

    @Override
    public long getRoomId() {
        if(getRoomInfo() != null) {
            return getId();
        }
        return 0;
    }

    @Override
    public void processKickAndCancelKick(ROOM_INFO roomInfo) {
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

        if (kickGameSocket != null) {
            if (kickGameSocket.getSeatNumber() == -1) {
                getLog().debug("processKick: kickNotReadyPlayer for {} ", kickNickname);
                this.kickNotReadyPlayer(kickGameSocket);
            } else {
                getLog().debug("processKick: kickReadyPlayer for {} ", kickNickname);
                this.kickReadyPlayer(kickGameSocket);
            }
        } else {
            getLog().debug("processKick: kickPlayer for {} ", kickNickname);
            this.kickPlayer(null);
        }

        if (kickGameSocket != null) {
            sendGameInfoToSocketClient(kickGameSocket);
        }
    }

    public void kickNotReadyPlayer(IGameSocketClient client) {
        kickPlayer(client);
        getLog().debug("kickNotReadyPlayer: set kick in the RoomInfo {}", getRoomInfo());
    }

    public void kickReadyPlayer(IGameSocketClient client) {
        kickPlayer(client);
        getLog().debug("kickReadyPlayer: set kick in the RoomInfo {}", getRoomInfo());

        IBattlegroundSeat seat = getSeatByAccountId(client.getAccountId());
        if (seat == null) {
            getLog().error("kickReadyPlayer: seat is null, for client={}", client);
            return;
        } else {
            getLog().debug("kickReadyPlayer: seat is {}", seat);
        }

        try {
            processSitOut(client, null, seat.getNumber(), seat.getAccountId(), true);
        } catch (CommonException e) {
            getLog().error("kickReadyPlayer: Cannot sitOut, seat={}, room={}", seat, getId(), e);
        }
    }

    public void kickPlayer(IGameSocketClient client) {
        getLog().debug("kickPLayer: client is {}", client);

        if (client != null) {
            client.setKicked(true);
        }

        this.updateRoomInfo(newRoomInfo -> {});

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

        if(status != null) {

            try {
                getLog().debug("processCancelKick: updatePlayersStatusAndSendToOwner to {} for {} ",
                        status, cancelKickNickname);

                UpdatePrivateRoomResponse updatePrivateRoomResponse =
                        updatePlayersStatusAndSendToOwnerNicknamesOnly(Arrays.asList(cancelKickNickname), status);

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
                        privateRoomId, getGameServerId(), (int) roomInfo.getBankId(),
                        cancelKickNickname, null, accountId, status);

            } catch (Exception e) {
                getLog().error("processCancelKick: Exception to sendPlayerStatusInPrivateRoomToCanex, {}",
                        e.getMessage(), e);
            }
        }

        if (cancelKickGameSocket != null) {
            sendGameInfoToSocketClient(cancelKickGameSocket);
        }
    }

    public void cancelKickPlayer(IGameSocketClient client) {
        getLog().debug("cancelKickPlayer: client is {}", client);

        if (client != null) {
            client.setKicked(false);
        }

        this.updateRoomInfo(newRoomInfo -> {});

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
    public boolean hasNotReadyNotKickedSeat() {
        return getSeats().stream()
                .anyMatch(seat ->
                        seat != null && seat.getSocketClient() != null
                                && !seat.getSocketClient().isKicked()
                                && !isSeaterWithConfirmedByuIn(seat)
                );
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
}
