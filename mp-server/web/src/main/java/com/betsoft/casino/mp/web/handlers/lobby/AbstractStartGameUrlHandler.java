package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.onlineplayer.SocketClientInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.GetStartGameUrlResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.IMessageHandler;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.utils.TInboundObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.google.gson.Gson;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract start game handler. Handle StartGameUrls messages from client.
 */
public abstract class AbstractStartGameUrlHandler<MESSAGE extends TInboundObject, CLIENT extends ILobbySocketClient>
        extends MessageHandler<MESSAGE, CLIENT> {
    private final SingleNodeRoomInfoService singleNodeRoomInfoService;
    private final MultiNodeRoomInfoService multiNodeRoomInfoService;
    private final ServerConfigService serverConfigService;
    private final RoomServiceFactory roomServiceFactory;
    private final RoomTemplateService roomTemplateService;
    protected final RoomPlayerInfoService playerInfoService;
    protected final BGPrivateRoomInfoService bgPrivateRoomInfoService;
    protected final MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService;
    private final RoomPlayersMonitorService roomPlayersMonitorService;

    protected AbstractStartGameUrlHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                                          SingleNodeRoomInfoService singleNodeRoomInfoService, MultiNodeRoomInfoService multiNodeRoomInfoService,
                                          ServerConfigService serverConfigService,
                                          RoomServiceFactory roomServiceFactory, RoomTemplateService roomTemplateService,
                                          RoomPlayerInfoService playerInfoService, BGPrivateRoomInfoService bgPrivateRoomInfoService,
                                          MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService,
                                          RoomPlayersMonitorService roomPlayersMonitorService) {
        super(gson, lobbySessionService, lobbyManager);
        this.singleNodeRoomInfoService = singleNodeRoomInfoService;
        this.multiNodeRoomInfoService = multiNodeRoomInfoService;
        this.serverConfigService = serverConfigService;
        this.roomServiceFactory = roomServiceFactory;
        this.roomTemplateService = roomTemplateService;
        this.playerInfoService = playerInfoService;
        this.bgPrivateRoomInfoService = bgPrivateRoomInfoService;
        this.multiNodePrivateRoomInfoService = multiNodePrivateRoomInfoService;
        this.roomPlayersMonitorService = roomPlayersMonitorService;
    }

    /**
     * Handles StartGameUrl messages from clients. Finds free room for player and create url getRoomUrl for next game socket connection.
     * @param session web socket session
     * @param message StartGameUrl message (GetStartGameUrl|GetBattlegroundStartGameUrl|GetPrivateBattlegroundStartGameUrl)
     * @param client socket client
     */
    @Override
    public void handle(WebSocketSession session, MESSAGE message, CLIENT client) {
        if (checkLogin(message, client)) {
            try {

                LobbySession lobbySession = lobbySessionService.get(client.getSessionId());

                if (lobbySession == null) {
                    getLog().warn("lobbySession is null, client={}", client);
                    sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Lobby session not found", message.getRid());
                    return;
                }

                if (checkIsBadMessageAndSendError(message, client, lobbySession)) {
                    return;
                }

                IRoomInfo roomInfo;

                Money stakeFromMessage = this.getStakeFromMessage(message);
                long roomIdFromMessage = this.getRequestedRoomIdFromMessage(message);

                IRoomInfoService roomInfoService = this.getRoomInfoService(client);
                IRoomPlayerInfo roomPlayerInfo = getPlayerInfoService().get(client.getAccountId());

                String nickname = client.getNickname();
                String sid = client.getSessionId();

                if (roomIdFromMessage != -1) {

                    roomInfo = getRoomInfo(message.getRid(), roomIdFromMessage, client, roomInfoService);
                    getLog().debug("handle {}, {}: roomIdFromMessage={}, roomInfo found {}", nickname, sid,
                            roomIdFromMessage, roomInfo);

                } else {

                    GameType gameType = client.getGameType();
                    MoneyType moneyType = client.getMoneyType();
                    IPlayerInfo playerInfo = client.getPlayerInfo();

                    if (roomPlayerInfo != null && roomPlayerInfo.getRoomId() > 0) {

                        getLog().debug("handle {}, {}: there is a roomPlayerInfo for account={} and " +
                                        "roomPlayerInfo.getRoomId()={}, roomPlayerInfo={}", nickname, sid,
                                client.getAccountId(), roomPlayerInfo.getRoomId(), roomPlayerInfo);

                        roomInfo = getRoomInfo(message.getRid(), roomPlayerInfo.getRoomId(), client, roomInfoService);

                        getLog().debug("handle {}, {}: roomPlayerInfo.getRoomId()={} roomInfo found {}",
                                nickname, sid, roomPlayerInfo.getRoomId(), roomInfo);

                        String currency = playerInfo.getCurrency().getCode();

                        if (roomInfo == null) {

                            getLog().debug("handle {}, {}: roomInfo is null, try find more appropriate roomInfo. " +
                                    "currency={}, roomInfo={}, client={}", nickname, sid, currency, roomInfo, client);

                            roomInfo = this.getBestRoomForStake(lobbySession, gameType, client, currency, message);

                            getLog().debug("handle {}, {}: roomInfo returned from getBestRoomForStake (1) " +
                                    "roomInfo={}, client={}", nickname, sid, roomInfo, client);

                        } else {

                            //if number of players is bigger or equal the max allowed try to get other RoomInfo
                            if(thereAreNoEmptySeats(roomInfo, nickname, sid, client)) {

                                getLog().debug("handle {}, {}: numberOfPlayers >= maxSeats, try " +
                                                "find more appropriate roomInfo. currency={}, roomInfo={}, client={}",
                                        nickname, sid, currency, roomInfo, client);

                                roomInfo = this.getBestRoomForStake(lobbySession, gameType, client, currency, message);

                                getLog().debug("handle {}, {}: roomInfo returned from getBestRoomForStake (2) " +
                                        "roomInfo={}, client={}", nickname, sid, roomInfo, client);

                                //if existing roomInfo defers from requested MoneyType and GameType
                            } else if (roomInfo.getMoneyType() != moneyType || roomInfo.getGameType() != gameType) {

                                getLog().debug("handle {}, {}: Found roomInfo, but LobbySocketClient params " +
                                                "mismatch to roomInfo specification, try find more appropriate roomInfo. " +
                                                "(roomInfo:MoneyType={},GameType={}), (SocketClient:MoneyType={},GameType={})",
                                        nickname, sid, roomInfo.getMoneyType(), roomInfo.getGameType(), moneyType, gameType);

                                roomInfo = this.getBestRoomForStake(lobbySession, gameType, client, currency, message);

                                getLog().debug("handle {}, {}: roomInfo returned from  getBestRoomForStake (3) " +
                                        "roomInfo={}, client={}", nickname, sid, roomInfo, client);

                                //if existing roomInfo is Battleground and non-Crash
                            } else if (roomInfo.isBattlegroundMode() && !roomInfo.getGameType().isCrashGame()) {

                                getLog().debug("handle {}, {}: Found roomInfo is Battleground and non-Crash " +
                                        "roomInfo={}", nickname, sid, roomInfo);

                                long buyInAmount = roomPlayerInfo.getRoundBuyInAmount();
                                RoomState state = roomInfo.getState();
                                boolean buyInConfirmed = lobbySession.isConfirmBattlegroundBuyIn();

                                getLog().debug("handle {}, {}: buyInAmount:{}, buyInConfirmed:{}, roomState:{}",
                                        nickname, sid, buyInAmount, buyInConfirmed, state);

                                if (!buyInConfirmed || buyInAmount == 0) {

                                    getLog().debug("handle {}, {}: Need get other roomInfo, found BTG roomInfo without confirm " +
                                            "or buyInAmount=0 roomInfo={}, client={}", nickname, sid, roomInfo, client);

                                    roomInfo = this.getBestRoomForStake(lobbySession, gameType, client, playerInfo.getCurrency().getCode(), message);

                                    getLog().debug("handle {}, {}: roomInfo returned from  getBestRoomForStake (4) " +
                                            "roomInfo={}, client={}", nickname, sid, roomInfo, client);
                                }
                            }
                        }
                    } else {

                        getLog().debug("handle {}, {}: roomIdFromMessage is -1 and no roomPlayerInfo.getRoomId()>0 " +
                                "found in roomPlayerInfo={}", nickname, sid, roomPlayerInfo);

                        roomInfo = this.getBestRoomForStake(lobbySession, gameType, client, playerInfo.getCurrency().getCode(), message);

                        getLog().debug("handle {}, {}: roomInfo returned from  getBestRoomForStake (5) " +
                                "roomInfo={}, client={}", nickname, sid, roomInfo, client);
                    }
                }

                //error message must be already send, just exit
                if (roomInfo == null) {
                    getLog().warn("handle {}, {}: Cannot find roomInfo, exit. message={}, client={}, roomPlayerInfo={}",
                            nickname, sid, message, client, roomPlayerInfo);
                    sendErrorMessage(client, ErrorCodes.ROOM_NOT_FOUND, "Room not found", message.getRid());
                    return;
                }

                int serverId;
                if (roomInfo instanceof ISingleNodeRoomInfo) {

                    if (((ISingleNodeRoomInfo) roomInfo).getGameServerId() == IRoomInfo.NOT_ASSIGNED_ID) {

                        getRoomServiceFactory().getRoom(roomInfo.getGameType(), roomInfo.getId());
                        roomInfo = getRoomInfoService(client).getRoom(roomInfo.getId());
                        getLog().debug("handle {}, {}: Room Found={} not started, try to start it: {}", client.getNickname(), client.getSessionId(),
                                roomInfo.getId(), roomInfo);
                    }

                    if (thereAreNoEmptySeats(roomInfo, nickname, sid, client)) {
                        getLog().debug("handle {}, {}: Too many players in roomInfo={}", client.getNickname(), client.getSessionId(), roomInfo);
                        sendErrorMessage(client, ErrorCodes.TOO_MANY_PLAYER, "Too many players", message.getRid());
                        return;
                    }

                    serverId = ((ISingleNodeRoomInfo) roomInfo).getGameServerId();

                } else {
                    serverId = getServerConfigService().getServerId();
                }

                if (serverId != IRoomInfo.NOT_ASSIGNED_ID) {

                    IServerConfig serverConfig = getServerConfigService().getConfig(serverId);
                    if (serverConfig == null) {
                        getLog().warn("ServerConfig not found for serverId=" + serverId + ". Seems that server is down. Migrating down games.");
                        roomServiceFactory.repairRoomsOnDownServer(serverId);
                        serverId = getServerConfigService().getServerId();
                        serverConfig = getServerConfigService().getConfig();
                    }

                    String roomUrl = getRoomUrl(session, roomInfo.getId(), serverConfig, client, stakeFromMessage.toCents(),
                            roomInfo.getGameType());

                    GetStartGameUrlResponse response = new GetStartGameUrlResponse(
                            System.currentTimeMillis(),
                            message.getRid(),
                            roomInfo.getId(),
                            roomUrl
                    );

                    getLog().debug("handle {}, {}: GetStartGameUrlResponse={}", client.getNickname(), client.getSessionId(), response);
                    client.sendMessage(response, message);

                } else {
                    getLog().debug("handle {}, {}: Room not assigned for message={}", client.getNickname(), client.getSessionId(), message);
                    sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Room not assigned", message.getRid());
                }
            } catch (Exception e) {
                getLog().error("Unable to get room url", e);
                sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Cannot load room", message.getRid());
            }
        }
    }

    abstract long getRequestedRoomIdFromMessage(MESSAGE message);

    /**
     * Check input message for incorrect data.
     * @param message message from client
     * @param client lobby web socket client
     * @param lobbySession lobby session
     * @return true if data is incorrect and sends error to client
     */
    abstract boolean checkIsBadMessageAndSendError(MESSAGE message, ILobbySocketClient client, ILobbySession lobbySession);

    public static String getRoomUrl(WebSocketSession session, long roomId, IServerConfig serverConfig,
                                    ILobbySocketClient client, long stake, GameType gameType) {
        String host = serverConfig.getHost();
        int serverId = serverConfig.getId();
        String domain = serverConfig.getDomain();
        String origin = IMessageHandler.getOrigin(session);
        String roomWebSocketUrl;
        if (host.endsWith("mp.local") || host.endsWith("mp.local.com") || host.endsWith(".mydomain")) { //hack for local/dev deploy
            // For local/dev use browser-origin host and current MP runtime port (server.port),
            // so returned URL works in dockerized local environments.
            String localHost = host;
            try {
                URI originUri = URI.create(origin);
                if (!StringUtils.isTrimmedEmpty(originUri.getHost())) {
                    localHost = originUri.getHost();
                }
            } catch (Exception ignored) {
                // Keep fallback host from server config.
            }
            String localPort = System.getProperty("server.port", "8081");
            roomWebSocketUrl = IMessageHandler.getWsProtocol(origin) + localHost + ":" + localPort + "/websocket/";
        } else {
            roomWebSocketUrl = IMessageHandler.getWsProtocol(origin) + "games" + domain + "/" + serverId + "/websocket/";
        }
        roomWebSocketUrl += gameType.isCrashGame() ? "mpunified" : "mpgame";

        return "?SID=" + client.getSessionId() +
                "&serverId=" + serverConfig.getId() +
                "&lang=" + (StringUtils.isTrimmedEmpty(client.getLang()) ? "en" : client.getLang()) +
                "&roomId=" + roomId +
                (stake > 0 ? "&stake=" + stake : "") +
                "&WEB_SOCKET_URL=" + roomWebSocketUrl;
    }

    /**
     * Find free room for player
     * @param client lobby socket client
     * @param stake stake
     * @param gameType gameType
     * @param currency currency of player
     * @param moneyType moneyType
     * @return {@code IRoomInfo} room info
     */
    public IRoomInfo getRoomInfoByStake(CLIENT client, Money stake, GameType gameType, String currency,
                                        MoneyType moneyType) {
        RoomTemplate template = getRoomTemplateService().getMostSuitable(client.getBankId(), stake,
                moneyType, gameType);
        getLog().debug("getBestRoomForStake: accountId={}, bankId={}, stake={}, gameType={}, moneyType={}, template={}",
                client.getAccountId(), client.getBankId(), stake, gameType, moneyType, template);

        IRoomInfoService roomInfoService = getRoomInfoService(client);

        if (roomInfoService != null) {

            Collection<IRoomInfo> roomInfos = roomInfoService.getRooms(client.getBankId(), template, stake, currency);
            getLog().debug("getBestRoomForStake: before removing deactivated roomInfos.size()={}", roomInfos.size());
            roomInfos = roomInfos.stream()
                    .filter(ri -> !ri.isDeactivated())
                    .collect(Collectors.toSet());
            getLog().debug("getBestRoomForStake: after removing deactivated roomInfos.size()={}", roomInfos.size());
            getLog().debug("getBestRoomForStake: accountId={}, roomInfos={}", client.getAccountId(), roomInfos);

            IRoomInfo roomInfo = roomInfoService.tryFindThisServerRoomAndNotFull(roomInfos, getServerConfigService().getServerId());
            getLog().debug("getBestRoomForStake: accountId={}, best roomInfo={}", client.getAccountId(), roomInfo);

            if(roomInfo == null) {
                roomInfo = roomInfoService.createForTemplate(template, client.getBankId(), stake, currency);
            }

            return roomInfo;
        }

        getLog().error("getBestRoomForStake: accountId={}, bankId={}, stake={}, gameType={}, moneyType={}, " +
                        "roomInfoService is null", client.getAccountId(), client.getBankId(), stake, gameType, moneyType);

        return null;
    }

    /**
     * Find free room for special modes (FRB,TOURNAMENT)
     * @param gameType gameType
     * @param client lobby socket client
     * @param moneyType moneyType
     * @param stake stake
     * @return {@code IRoomInfo} room info
     */
    protected IRoomInfo getSpecialRoom(GameType gameType, CLIENT client, MoneyType moneyType, Money stake) {
        IRoomInfo bestRoom;

        IRoomInfoService roomInfoService = getRoomInfoService(client);
        Collection<IRoomInfo> roomInfos = roomInfoService.getSpecialRooms(
                client.getBankId(),
                gameType,
                stake,
                client.getPlayerInfo().getCurrency().getCode(),
                moneyType);

        getLog().debug("getSpecialRoom: before removing deactivated roomInfos.size()={}", roomInfos.size());
        roomInfos = roomInfos.stream()
                .filter(ri -> !ri.isDeactivated())
                .collect(Collectors.toSet());
        getLog().debug("getSpecialRoom: after removing deactivated roomInfos.size()={}", roomInfos.size());

        boolean isFRB = moneyType == MoneyType.FRB;
        getLog().debug("getSpecialRoom: roomInfos={}", roomInfos);
        if (moneyType != MoneyType.TOURNAMENT) {
            roomInfos = roomInfos.stream().filter(roomInfo -> {
                try {
                    @SuppressWarnings("rawtypes")
                    IRoom room = getRoomServiceFactory().getRoomWithoutCreationById(roomInfo.getId());
                    if (room == null) {
                        return true;
                    } else {
                        if (isFRB) {
                            room.removeDisconnectedObservers();
                        }
                        return room.getObserverCount() == 0;
                    }
                } catch (Exception e) {
                    return true;
                }
            }).collect(Collectors.toSet());
        }
        getLog().debug("getSpecialRoom: roomInfosWithoutObservers={}", roomInfos);
        bestRoom = getRoomInfoService(client).tryFindThisServerRoomAndNotFull(roomInfos, getServerConfigService().getServerId());
        return bestRoom;
    }

    /**
     * Find best room for stake.
     * @param lobbySession lobby session of player
     * @param gameType gameType
     * @param client lobby socket client
     * @param currency currency of player
     * @param message message from client
     * @return {@code IRoomInfo} room info
     * @throws CommonException if any unexpected error occur
     */
    abstract IRoomInfo getBestRoomForStake(LobbySession lobbySession, GameType gameType,
                                           ILobbySocketClient client, String currency, MESSAGE message)
            throws CommonException;

    abstract Money getStakeFromMessage(MESSAGE message) throws CommonException;

    public IRoomInfoService getRoomInfoService(CLIENT client) {
        if (client.isPrivateRoom()) {
            return client.getGameType().isSingleNodeRoomGame() ? bgPrivateRoomInfoService : multiNodePrivateRoomInfoService;
        }
        return client.getGameType().isSingleNodeRoomGame() ? singleNodeRoomInfoService : multiNodeRoomInfoService;
    }

    public ServerConfigService getServerConfigService() {
        return serverConfigService;
    }

    public RoomServiceFactory getRoomServiceFactory() {
        return roomServiceFactory;
    }

    public RoomTemplateService getRoomTemplateService() {
        return roomTemplateService;
    }

    public RoomPlayerInfoService getPlayerInfoService() {
        return playerInfoService;
    }

    protected boolean thereAreNoEmptySeats(IRoomInfo roomInfo, String nickname, String sid, ILobbySocketClient client) {

        short maxSeats = roomInfo.getMaxSeats();
        boolean thereAreNoEmptySeats = false;

        IRoom roomWithoutCreation = null;
        try {
            roomWithoutCreation = getRoomServiceFactory()
                    .getRoomWithoutCreation(roomInfo.getGameType(), roomInfo.getId());

        } catch (Exception e) {
            getLog().error("thereAreNoEmptySeats {}, {}: Exception={}", nickname, sid, e.getMessage());
        }

        //try to check the room if it is running locally on the node
        if (roomWithoutCreation != null) {
            short seatsCount = roomWithoutCreation.getSeatsCount();
            boolean clientNotSeater = roomWithoutCreation
                    .getSeatByAccountId(client.getAccountId()) == null;

            getLog().debug("thereAreNoEmptySeats {}, {}: seatsCount={}, clientNotSeater={} for roomInfo={}",
                    nickname, sid, seatsCount, clientNotSeater, roomInfo);

            thereAreNoEmptySeats = (clientNotSeater && seatsCount >= maxSeats);

            //if room does not run on the local node
        } else {

            int numberOfPlayers = getNumberOfPlayers(roomInfo, client.getAccountId(), client);

            getLog().debug("thereAreNoEmptySeats {}, {}: numberOfPlayers={}, maxSeats={}",
                    nickname, sid, numberOfPlayers, maxSeats);

            thereAreNoEmptySeats = numberOfPlayers >= maxSeats;
        }

        getLog().debug("thereAreNoEmptySeats {}, {}: thereAreNoEmptySeats={}", nickname, sid, thereAreNoEmptySeats);

        return thereAreNoEmptySeats;
    }

    protected int getNumberOfPlayersForSingleNodeRoom(IRoomInfo roomInfo, long accountId, ILobbySocketClient client) {

        int numberOfPlayers = 0;

        if(roomInfo instanceof SingleNodeRoomInfo) { //for single node room request the node for number of observers

            long serverId = ((SingleNodeRoomInfo) roomInfo).getGameServerId();

            if(serverId == IRoomInfo.NOT_ASSIGNED_ID) { //SingleNodeRoom is not started, node is not assigned serverId == IRoomInfo.NOT_ASSIGNED_ID

                getLog().debug("getNumberOfPlayersForSingleNodeRoom {}, {}: SingleNodeRoom is not started, no node is " +
                        "assigned to the room, serverId == {}", client.getNickname(), client.getSessionId(), IRoomInfo.NOT_ASSIGNED_ID);
            } else {

                getLog().debug("getNumberOfPlayersForSingleNodeRoom {}, {}: SingleNode room, serverId={}",
                        client.getNickname(), client.getSessionId(), serverId);

                //int numberOfObservers = getTotalObserversCount(client, serverId, roomInfo.getId(), (int) roomInfo.getGameType().getGameId());
                //getLog().debug("getNumberOfPlayers {}, {}: roomId={}, gameId={}, numberOfObservers={}",
                //        client.getNickname(), client.getSessionId(), roomInfo.getId(), roomInfo.getGameType().getGameId(),
                //        numberOfObservers);
                //numberOfPlayers = numberOfObservers;

                Collection<SocketClientInfo> socketClientInfos = roomPlayersMonitorService.socketClientInfosGetAll();
                Map<Long, IRMSRoom> trmsRooms = roomPlayersMonitorService.convertSocketClientInfoToTRMSRoomsMap(socketClientInfos);
                getLog().debug("getNumberOfPlayersForSingleNodeRoom {}, {}: accountId={}, trmsRooms={}", client.getNickname(), client.getSessionId(), accountId, trmsRooms);

                if(trmsRooms.isEmpty()){

                    getLog().debug("getNumberOfPlayersForSingleNodeRoom {}, {}: accountId={}, trmsRooms.isEmpty={}",
                            client.getNickname(), client.getSessionId(), accountId, trmsRooms.isEmpty());
                } else {

                    IRMSRoom trmsRoom = trmsRooms.get(roomInfo.getId());

                    if (trmsRoom == null || trmsRoom.getPlayers() == null) {
                        getLog().debug("getNumberOfPlayersForSingleNodeRoom {}, {}: accountId={}, roomId={}, trmsRoom={}",
                                client.getNickname(), client.getSessionId(), accountId, roomInfo.getId(), trmsRoom);
                    } else {

                        List<IRMSPlayer> filteredPlayers = trmsRoom.getPlayers().stream()
                                .filter(player -> !player.getNickname().equals(client.getNickname()))
                                .collect(Collectors.toList());

                        getLog().debug("getNumberOfPlayersForSingleNodeRoom {}, {}: roomId={}, gameId={}, filteredPlayers.size()={}",
                                client.getNickname(), client.getSessionId(), roomInfo.getId(),
                                roomInfo.getGameType().getGameId(), filteredPlayers.size());

                        numberOfPlayers = filteredPlayers.size();
                    }
                }
            }
        }

        return numberOfPlayers;
    }

    protected int getNumberOfPlayersForMultiNodeRoom(IRoomInfo roomInfo, long accountId, ILobbySocketClient client) {

        int numberOfPlayers = 0;

        try {

            getLog().debug("getNumberOfPlayersForMultiNodeRoom {}, {}: accountId={} MultiNode room",
                    accountId, client.getNickname(), client.getSessionId());

            IRoom roomWithoutCreation = getRoomServiceFactory()
                    .getRoomWithoutCreation(roomInfo.getGameType(), roomInfo.getId());

            if(roomWithoutCreation != null) {

                List<ISeat> seats = roomWithoutCreation.getAllSeats();

                if(seats != null) {

                    getLog().debug("getNumberOfPlayersForMultiNodeRoom {}, {}: roomId={}, gameId={}, seats.size()={}",
                            client.getNickname(), client.getSessionId(), roomInfo.getId(),
                            roomInfo.getGameType().getGameId(), seats.size());

                    numberOfPlayers = seats.size();
                }
            } else {
                getLog().error("getNumberOfPlayersForMultiNodeRoom {}, {}: roomId={}, gameId={}, roomWithoutCreation is null",
                        client.getNickname(), client.getSessionId(), roomInfo.getId(), roomInfo.getGameType().getGameId());

            }

        } catch (Exception e) {

            getLog().error("getNumberOfPlayersForMultiNodeRoom {}, {}: roomId={}, gameId={}, exception={}",
                    client.getNickname(), client.getSessionId(), roomInfo.getId(),
                    roomInfo.getGameType().getGameId(), e.getMessage(), e);

            numberOfPlayers = roomInfo.getMaxSeats();
        }

        return numberOfPlayers;
    }

    protected int getNumberOfPlayers(IRoomInfo roomInfo, long accountId, ILobbySocketClient client) {

        getLog().debug("getNumberOfPlayers {}, {}: accountId={}, roomInfo={}", client.getNickname(), client.getSessionId(), accountId, roomInfo);

        int numberOfPlayers;

        if(roomInfo instanceof SingleNodeRoomInfo) { //for single node room request the node for number of observers

            numberOfPlayers = getNumberOfPlayersForSingleNodeRoom(roomInfo, accountId, client);

        } else { //for multinode room, get all seats

            numberOfPlayers = getNumberOfPlayersForMultiNodeRoom(roomInfo, accountId, client);
        }

        getLog().debug("getNumberOfPlayers {}, {}: roomId={}, gameId={}, numberOfPlayers={}",
                client.getNickname(), client.getSessionId(), roomInfo.getId(),
                roomInfo.getGameType().getGameId(), numberOfPlayers);

        return numberOfPlayers;
    }
}
