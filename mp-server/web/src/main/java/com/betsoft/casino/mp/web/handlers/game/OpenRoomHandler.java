package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.onlineplayer.SocketClientInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.OpenRoom;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketServer;
import com.betsoft.casino.mp.web.socket.UnifiedSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.service.AbstractRoomInfoService.DEFAULT_BATTLEGROUND_AMMO_AMOUNT;

/**
 * Open room handler. Handle open room message from client.
 */
@Component
public class OpenRoomHandler extends AbstractRoomHandler<OpenRoom, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(OpenRoomHandler.class);
    private final LobbySessionService lobbySessionService;
    private final RoomTemplateService roomTemplateService;
    private final RoomPlayersMonitorService roomPlayersMonitorService;
    private final SocketServer socketServer;
    private static final String MQC_NICKNAME_SUFFIX = "_MQC";

    public OpenRoomHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                           MultiNodeRoomInfoService multiNodeRoomInfoService,
                           RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                           LobbySessionService lobbySessionService, SocketServer socketServer,
                           ServerConfigService serverConfigService, RoomTemplateService roomTemplateService,
                           RoomPlayersMonitorService roomPlayersMonitorService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
        this.socketServer = socketServer;
        this.roomTemplateService = roomTemplateService;
        this.roomPlayersMonitorService = roomPlayersMonitorService;
    }

    /**
     * Handle OpenRoom message from client
     * @param session seb socket session
     * @param message OpenRoom message
     * @param client game socket client
     */
    @Override
    public void handle(WebSocketSession session, OpenRoom message, IGameSocketClient client) {

        long roomId = message.getRoomId();

        try {

            LobbySession lobbySession = lobbySessionService.get(message.getSid());
            if (lobbySession == null) {
                this.sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
                return;
            }

            IRoomInfo roomInfo;
            if (roomId <= 0) {

                if (!client.isSingleConnectionClient()) {
                    this.sendErrorMessage(client, ErrorCodes.ROOM_NOT_FOUND, "Bad roomId", message.getRid());
                    return;
                }

                UnifiedSocketClient unifiedClient = (UnifiedSocketClient) client;
                if (unifiedClient.getPlayerInfo() == null) {
                    this.sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Player not found in socket client", message.getRid());
                    return;
                }

                roomInfo = this.getMultiNodeRoomInfo(unifiedClient, message, lobbySession);

            } else {

                roomInfo = roomServiceFactory.getRoomInfo(roomId);

            }

            if (roomInfo == null) {
                if (!lobbySession.isPrivateRoom()) {
                    this.sendErrorMessage(client, ErrorCodes.ROOM_NOT_FOUND, "Room not found", message.getRid());
                } else {
                    this.sendErrorMessage(client, ErrorCodes.ROOM_WAS_DEACTIVATED, "Room was deactivated", message.getRid());
                }
                return;
            }

            if (!client.isSingleConnectionClient()) {
                client.setServerId(message.getServerId());
            } else {
                client.setServerId(socketServer.getServerId());
            }

            GameType gameType = roomInfo.getGameType();
            roomId = roomInfo.getId();
            IRoom room = this.getRoomWithCheck(message.getRid(), roomId, client, gameType);

            if (room != null) {

                LOG.debug("OpenRoomHandler, room info: {}", room.getRoomInfo());
                if (room.isBattlegroundMode() &&
                        (!lobbySession.isBattlegroundAllowed()
                                || !room.getRoomInfo().getGameType().isBattleGroundGame()
                                || !room.getRoomInfo().getMoneyType().equals(MoneyType.REAL)
                        )) {

                    LOG.error("Room has battleground mode, but mode is not allowed lobbySession={}, roomInfo={}",
                            lobbySession, room.getRoomInfo());
                    this.sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Bad room", message.getRid());

                } else if (room.getRoomInfo().isPrivateRoom() && room.getRoomInfo().isDeactivated()) {

                    LOG.error("The room is private and was deactivated early lobbySession={}, roomInfo={}",
                            lobbySession, room.getRoomInfo());
                    this.sendErrorMessage(client, ErrorCodes.ROOM_WAS_DEACTIVATED, "Room was deactivated", message.getRid());

                } else if (!room.getRoomInfo().getMoneyType().equals(lobbySession.getMoneyType())) {

                    LOG.error("Lobby and room moneyType mismatch, lobbySession={}, roomInfo={}", lobbySession,
                            room.getRoomInfo());
                    this.sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Bad room", message.getRid());

                } else {

                    boolean isAllowedPending = gameType.isCrashGame()
                            || gameType == GameType.BG_DRAGONSTONE || gameType == GameType.BG_MISSION_AMAZON || gameType == GameType.BG_SECTOR_X
                            || gameType == GameType.DRAGONSTONE || gameType == GameType.MISSION_AMAZON || gameType == GameType.SECTOR_X;

                    boolean hasPendingOperations = this.hasPendingOperations(lobbySession.getAccountId(), client, message);

                    if (!isAllowedPending && hasPendingOperations) {
                        //error message send in hasPendingOperations
                        return;
                    }

                    boolean isNotAllowPlayWithAnyPendingPlayers = room.isNotAllowPlayWithAnyPendingPlayers();
                    boolean hasPlayersWithPendingOperation = this.hasPlayersWithPendingOperation(room.getId());

                    if (isNotAllowPlayWithAnyPendingPlayers && hasPlayersWithPendingOperation) {

                        LOG.warn("Cannot open room, room has players with pending operations");
                        this.sendErrorMessage(client, ErrorCodes.FOUND_PENDING_OPERATION, "Found room with " +
                                "pending operation", message.getRid());
                        return;

                    }

                    client.setLog(room.getLog());
                    LOG.debug("loaded lobbySession: {}", lobbySession);

                    client.setAccountId(lobbySession.getAccountId());
                    client.setPrivateRoom(roomInfo instanceof BGPrivateRoomInfo || roomInfo instanceof MultiNodePrivateRoomInfo);
                    client.setBankId(lobbySession.getBankId());
                    client.setSessionId(message.getSid());
                    client.setGameType(gameType);
                    client.setNickname(lobbySession.getNickname());

                    if (lobbySession.isPrivateRoom() && room.getRoomInfo().isPrivateRoom()) {

                        if (lobbySession.getAccountId() == roomInfo.getOwnerAccountId()) {
                            lobbySession.setOwner(true);
                            client.setOwner(true);
                        }

                        LOG.debug("set Owner account id lobbySession: {}, room.getRoomInfo().getOwnerAccountId(): {} ", lobbySession, room.getRoomInfo().getOwnerAccountId());
                    }

                    boolean isSingleNodeBattleActionGame = gameType.isBattleGroundGame() && roomInfo instanceof SingleNodeRoomInfo;

                    if (isSingleNodeBattleActionGame) {

                        room.updateRoomInfo(newRoomInfo -> {

                            long accountId = lobbySession.getAccountId();
                            this.getLog().debug("OpenRoomHandler, remove account from waiting observers:  {}", accountId);

                            SingleNodeRoomInfo singleNodeRoomInfo = (SingleNodeRoomInfo) newRoomInfo;
                            singleNodeRoomInfo.removePlayerFromWaitingOpenRoom(accountId);

                            if (singleNodeRoomInfo.getBattlegroundBuyIn() == 0) {

                                this.getLog().debug("OpenRoomHandler, need fix wrong buyin:  {}", singleNodeRoomInfo);

                                singleNodeRoomInfo.setBattlegroundBuyIn(singleNodeRoomInfo.getStake().toCents());
                                singleNodeRoomInfo.setBattlegroundAmmoAmount(DEFAULT_BATTLEGROUND_AMMO_AMOUNT);

                                this.getLog().debug("OpenRoomHandler new:  {}", singleNodeRoomInfo);

                            }

                            if(!gameType.isCrashGame()) {

                                if(singleNodeRoomInfo.getMinSeats() != 2){
                                    singleNodeRoomInfo.setMinSeats((short) 2);
                                    this.getLog().debug("OpenRoomHandler singleNodeRoomInfo  setMinSeats fix:  {}", singleNodeRoomInfo);
                                }

                                if(gameType == GameType.BG_SECTOR_X){
                                    singleNodeRoomInfo.setRoundDuration(60);
                                    this.getLog().debug("OpenRoomHandler singleNodeRoomInfo setRoundDuration for BG SectorX fix:  {}", singleNodeRoomInfo);
                                }else{
                                    if(singleNodeRoomInfo.getRoundDuration() != 90){
                                        singleNodeRoomInfo.setRoundDuration(90);
                                        this.getLog().debug("OpenRoomHandler singleNodeRoomInfo setRoundDuration fix:  {}", singleNodeRoomInfo);
                                    }
                                }
                            }

                        });
                    }

                    this.openRoom(client, room, message, lobbySession);

                    room.updateRoomInfo(newRoomInfo -> {
                        newRoomInfo.updateLastTimeActivity();
                    });
                }
            }
        } catch (Exception e) {
            this.processUnexpectedError(client, message, e);
        }
    }

    private void saveSocketClientInfo(IGameSocketClient client, IRoom room) {

        if(client == null) {
            getLog().error("saveSocketClientInfo: client is null");
            return;
        }

        if(StringUtils.isTrimmedEmpty(client.getWebSocketSessionId())) {
            getLog().error("saveSocketClientInfo: client.getWebSocketSessionId() is empty: {}", client);
            return;
        }

        if(room == null) {
            getLog().error("saveSocketClientInfo: room is null");
            return;
        }

        IRoomInfo roomInfo = room.getRoomInfo();

        if(roomInfo == null) {
            getLog().error("saveSocketClientInfo: roomInfo is null in room: {}", room);
            return;
        }

        GameType gameType = roomInfo.getGameType();

        if(gameType == null) {
            getLog().error("saveSocketClientInfo: gameType is null in roomInfo: {}", roomInfo);
            return;
        }

        try {
            String playerExternalId = null;
            if (client instanceof ILobbySocketClient) {
                IPlayerInfo playerInfo = ((ILobbySocketClient) client).getPlayerInfo();
                if (playerInfo != null) {
                    playerExternalId = playerInfo.getExternalId();
                }
            }

            SocketClientInfo socketClientInfo
                    = roomPlayersMonitorService.convert(client, room, playerExternalId);

            roomPlayersMonitorService.upsertSocketClientInfo(socketClientInfo);

        } catch (Exception exception) {
            getLog().error("saveSocketClientInfo: exception: {}", exception.getMessage(), exception);
        }
    }

    /**
     * Player open room and get status observer.
     * @param client game socket client
     * @param room room
     * @param message OpenRoom message
     * @param lobbySession lobby session of player
     */
    @SuppressWarnings("unchecked")
    private void openRoom(IGameSocketClient client, IRoom room, OpenRoom message, LobbySession lobbySession) {

        if (message == null) {
            LOG.error("openRoom: message is null for lobbySession={}", lobbySession);
            this.sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Internal Error", -1);
            return;
        }

        if (lobbySession == null) {
            LOG.error("openRoom: lobbySession is null");
            this.sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Internal Error", message.getRid());
            return;
        }

        if (client == null) {
            LOG.error("openRoom: client is null for lobbySession={}", lobbySession);
            this.sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Internal Error", message.getRid());
            return;
        }

        if (room == null) {
            LOG.error("openRoom: room is null for lobbySession={}", lobbySession);
            this.sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Internal Error", message.getRid());
            return;
        }

        getLog().debug("openRoom start: getAccountId={}", client.getAccountId());
        ITransportObject openRoomResult = null;

        String uuid = lockSendGameInfo(room);
        try {
            openRoomResult = room.processOpenRoom(client, message, lobbySession.getCurrency().getCode());
            client.sendMessage(openRoomResult, message);
        } catch (Exception e) {
            LOG.error("openRoom: 1 failed open room for lobbySession={}", lobbySession, e);
            this.sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, e.getMessage(), message.getRid());
            return;
        } finally {
            unlockSendGameInfo(room, uuid);
        }

        try {

            if (!(openRoomResult instanceof IError)) {

                client.setEnterDate(System.currentTimeMillis());
                client.setRoomId(room.getId());

                ISeat seatByAccountId = room.getSeatByAccountId(lobbySession.getAccountId());
                int seatNumber = seatByAccountId != null ? room.getSeatNumber(seatByAccountId) : -1;

                client.setSeatNumber(seatNumber);

                lobbySession.setRoomId(room.getId());

                lobbySessionService.add(lobbySession);

                //save client info for BigQuery report. Save only in case if Open Room return successful message
                this.saveSocketClientInfo(client, room);
            }

            //if(room instanceof ICAFRoom) {
            //    ((ICAFRoom)room).sendGameInfoToAllObservers();
            //}

        } catch (Exception e) {
            LOG.error("openRoom: 2 failed open room for lobbySession={}", lobbySession, e);
            this.sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, e.getMessage(), message.getRid());
            return;
        }

        getLog().debug("openRoom end: getAccountId={}, message={}", client.getAccountId(), openRoomResult);

    }

    private String lockSendGameInfo(IRoom room) {
        if(room instanceof BattleAbstractCrashGameRoom) {
            return ((BattleAbstractCrashGameRoom)room).lockSendGameInfo();
        }
        return null;
    }

    private void unlockSendGameInfo(IRoom room, String uuid) {
        if (room instanceof BattleAbstractCrashGameRoom) {
            ((BattleAbstractCrashGameRoom) room).unlockSendGameInfo(uuid);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    /**
     * Gets multi node room info for crash games. Room info available on all servers.
     * @param client {@code UnifiedSocketClient} client
     * @param message OpenRoom message
     * @param lobbySession lobby session of player
     * @return {@code IRoomInfo} actual room info
     */
    public IRoomInfo getMultiNodeRoomInfo(UnifiedSocketClient client, OpenRoom message, LobbySession lobbySession) {

        GameType gameType = client.getGameType();
        Money stake = gameType.isBattleGroundGame()
                ? Money.fromCents(lobbySession.getBattlegroundBuyIns().get(0))
                : this.getStake(client, message);

        IPlayerInfo playerInfo = client.getPlayerInfo();

        String currency = playerInfo.getCurrency().getCode();

        MoneyType moneyType = client.getMoneyType();

        if (client.getPlayerInfo().isGuest()) {
            client.setMoneyType(MoneyType.FREE);
            moneyType = MoneyType.FREE;
        }

        RoomTemplate template = roomTemplateService.getMostSuitable(client.getBankId(), stake, moneyType, gameType);
        getLog().debug("getMultiNodeRoomInfo: bankId={}, stake={}, gameType={}, moneyType={}, template={}",
                client.getBankId(), stake, gameType, moneyType, template);

        Collection<MultiNodeRoomInfo> roomInfos = multiNodeRoomInfoService.getRooms(client.getBankId(), template, stake, currency);
        getLog().debug("getMultiNodeRoomInfo: before removing deactivated roomInfos.size()={}", roomInfos.size());
        roomInfos = roomInfos.stream()
                        .filter(ri -> !ri.isDeactivated())
                        .collect(Collectors.toList());
        getLog().debug("getMultiNodeRoomInfo: after removing deactivated roomInfos.size()={}", roomInfos.size());
        if(roomInfos.isEmpty()) {
            getLog().debug("getMultiNodeRoomInfo: roomInfos without deactivated is empty, create for template");
            multiNodeRoomInfoService.createForTemplate(template, roomInfos, client.getBankId(), stake, currency);
            roomInfos = multiNodeRoomInfoService.getRooms(client.getBankId(), template, stake, currency);
            roomInfos = roomInfos.stream()
                    .filter(ri -> !ri.isDeactivated())
                    .collect(Collectors.toList());
        }
        getLog().debug("getMultiNodeRoomInfo: roomInfos={}", roomInfos);

        MultiNodeRoomInfo multiNodeRoomInfo = multiNodeRoomInfoService.tryFindThisServerRoomAndNotFull(roomInfos, roomServiceFactory);

        return multiNodeRoomInfo;
    }

    private Money getStake(IGameSocketClient client, OpenRoom message) {
        //only Crash game supported, all templates with 1 cent stake.
        return Money.fromCents(1);
    }
}
