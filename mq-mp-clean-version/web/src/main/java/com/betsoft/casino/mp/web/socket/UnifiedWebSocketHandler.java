package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.ILobbySession;
import com.betsoft.casino.mp.model.room.ILatencyResponse;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.game.*;
import com.betsoft.casino.mp.web.handlers.lobby.*;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.utils.AESEncryptionDecryption;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.ConcurrentHashMap;

/**
 * User: flsh
 * Date: 20.12.2021.
 */
@Component
public class UnifiedWebSocketHandler extends AbstractWebSocketHandler<UnifiedSocketClient> implements WebSocketHandler {
    private static final Logger LOG = LogManager.getLogger(UnifiedWebSocketHandler.class);
    private final IMessageSerializer serializer;

    private final ConcurrentHashMap<String, UnifiedSocketClient> clients = new ConcurrentHashMap<>();
    private final LobbySessionService lobbySessionService;
    protected final RoomPlayersMonitorService roomPlayersMonitorService;
    protected final RoomServiceFactory roomServiceFactory;

    @Autowired
    public UnifiedWebSocketHandler(IMessageSerializer serializer,
                                   SingleNodeRoomInfoService singleNodeRoomInfoService,
                                   MultiNodeRoomInfoService multiNodeRoomInfoService,
                                   LobbySessionService lobbySessionService,
                                   RoomPlayerInfoService playerInfoService,
                                   RoomServiceFactory roomServiceFactory,
                                   RoomPlayersMonitorService roomPlayersMonitorService,
                                   ServerConfigService serverConfigService,
                                   EnterLobbyHandler enterLobbyHandler,
                                   GetRoomInfoHandler getRoomInfoHandler,
                                   CheckNicknameAvailabilityHandler checkNicknameAvailabilityHandler,
                                   ChangeNicknameHandler changeNicknameHandler,
                                   ChangeAvatarHandler changeAvatarHandler,
                                   LobbyRefreshBalanceHandler lobbyRefreshBalanceHandler,
                                   GetTimeHandler getTimeHandler,
                                   CloseRoundResultNotificationHandler closeRoundResultNotificationHandler,
                                   ChangeTooltipsHandler changeTooltipsHandler,
                                   LobbyReBuyHandler lobbyReBuyHandler,
                                   GetBattlegroundStartGameUrlHandler battlegroundStartGameUrlHandler,
                                   GetPrivateBattlegroundStartGameUrlHandler privateBattlegroundStartGameUrlHandler,
                                   OpenRoomHandler openRoomHandler, SitInHandler sitInHandler,
                                   SitOutHandler sitOutHandler, CrashBetHandler crashBetHandler,
                                   CrashCancelBetHandler crashCancelBetHandler,
                                   UpdateWeaponPaidMultiplierHandler updateWeaponPaidMultiplierHandler,
                                   GetFullGameInfoHandler getFullGameInfoHandler,
                                   CloseRoundResultsHandler closeRoundResultsHandler,
                                   ConfirmBattlegroundBuyInHandler confirmBattlegroundBuyInHandler,
                                   CrashCancelAllBetsHandler crashCancelAllBetsHandler,
                                   CrashCancelAutoEjectHandler crashCancelAutoEjectHandler,
                                   CrashChangeAutoEjectHandler crashChangeAutoEjectHandler,
                                   CrashBetsHandler crashBetsHandler,
                                   PendingOperationHandler pendingOperationHandler,
                                   LatencyHandler latencyHandler,
                                   StartBattlegroundPrivateRoomHandler startBattlegroundPrivateRoomHandler,
                                   KickHandler kickHandler,
                                   CancelKickHandler cancelKickHandler,
                                   PrivateRoomInviteHandler privateRoomInviteHandler,
                                   FinishGameSessionHandler finishGameSessionHandler
                                   ) {
        this.serializer = serializer;
        this.lobbySessionService = lobbySessionService;
        this.roomPlayersMonitorService = roomPlayersMonitorService;
        this.roomServiceFactory = roomServiceFactory;
        register(EnterLobby.class, enterLobbyHandler);
        register(GetRoomInfo.class, getRoomInfoHandler);
        register(CheckNicknameAvailability.class, checkNicknameAvailabilityHandler);
        register(ChangeNickname.class, changeNicknameHandler);
        register(ChangeAvatar.class, changeAvatarHandler);
        register(RefreshBalance.class, lobbyRefreshBalanceHandler);
        register(GetLobbyTime.class, getTimeHandler);
        register(CloseRoundResultNotification.class, closeRoundResultNotificationHandler);
        register(ChangeToolTips.class, changeTooltipsHandler);
        register(ReBuy.class, lobbyReBuyHandler);
        register(GetBattlegroundStartGameUrl.class, battlegroundStartGameUrlHandler);
        register(GetPrivateBattlegroundStartGameUrl.class, privateBattlegroundStartGameUrlHandler);
        register(OpenRoom.class, openRoomHandler);
        register(CloseRoom.class, new CloseRoomHandler(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService,
                roomServiceFactory, serverConfigService, this::closeConnection));
        register(SitIn.class, sitInHandler);
        register(SitOut.class, sitOutHandler);
        register(CrashBet.class, crashBetHandler);
        register(CrashBets.class, crashBetsHandler);
        register(CrashCancelBet.class, crashCancelBetHandler);
        register(CrashCancelAllBets.class, crashCancelAllBetsHandler);
        register(UpdateWeaponPaidMultiplier.class, updateWeaponPaidMultiplierHandler);
        register(GetFullGameInfo.class, getFullGameInfoHandler);
        register(CloseRoundResults.class, closeRoundResultsHandler);
        register(ConfirmBattlegroundBuyIn.class, confirmBattlegroundBuyInHandler);
        register(CrashCancelAutoEject.class, crashCancelAutoEjectHandler);
        register(CrashChangeAutoEject.class, crashChangeAutoEjectHandler);
        register(CheckPendingOperationStatus.class, pendingOperationHandler);
        register(Latency.class, latencyHandler);
        register(StartBattlegroundPrivateRoom.class, startBattlegroundPrivateRoomHandler);
        register(Kick.class, kickHandler);
        register(CancelKick.class, cancelKickHandler);
        register(PrivateRoomInvite.class, privateRoomInviteHandler);
        register(FinishGameSession.class, finishGameSessionHandler);
        setLocalDevOriginsAllowed(serverConfigService.getConfig().isLocalDevAllowed());
    }

    @Override
    void createConnection(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        getLog().debug("Game createConnection: sessionId={}, handshakeInfo={}", session.getId(),
                session.getHandshakeInfo());
        clients.put(session.getId(), new UnifiedSocketClient(session, session.getId(), sink, serializer));
        startPing(session, sink);
        if (latencyStandardTypeEnabled) {
            Disposable latencyDisposable = startLatencyMeasurement(session);
            registerLatencyDisposable(session, latencyDisposable);
        }
    }

    @Override
    protected boolean isConnected(WebSocketSession session) {
        return clients.containsKey(session.getId());
    }

    @Override
    void closeConnection(WebSocketSession session) {

        getLog().debug("closeConnection: WebSocketSession={}", session);

        roomPlayersMonitorService.removeSocketClientInfo(session.getId());

        UnifiedSocketClient client = clients.get(session.getId());
        getLog().debug("closeConnection: WebSocketSession.id={}, socketClient={}", session.getId(), client);

        if (client != null) {
            client.stopBalanceUpdater();
            client.stopTouchSession();
            String sessionId = client.getSessionId();
            if (!StringUtils.isTrimmedEmpty(sessionId)) {
                ILobbySession lobbySession = lobbySessionService.get(sessionId);
                if (lobbySession != null && lobbySession.getWebsocketSessionId() != null &&
                        lobbySession.getWebsocketSessionId().equals(session.getId())) {
                    getLog().debug("closeConnection: found lobbySession with same web websocketSessionId");
                    lobbySessionService.remove(sessionId);
                }
            }

            logSessionLatency(client);
            client.setDisconnected();
            clients.remove(session.getId());

            IRoom room = getRoom(client);
            if(room != null) {
                IRoomInfo roomInfo = room.getRoomInfo();
                if(roomInfo.isPrivateRoom()) {
                    room.removeObserverByAccountId(client.getAccountId());
                }
            }
        }

        unregisterLatencyDisposable(session);
    }

    private IRoom getRoom(UnifiedSocketClient client) {
        IRoom room = null;
        try {
            GameType gameType = client.getGameType();
            Long roomId = client.getRoomId();

            if (roomId == null || gameType == null) {
                getLog().warn("getRoom: gameType={}, roomId={}", gameType, roomId);
            } else {
                room = this.roomServiceFactory.getRoomWithoutCreation(client.getGameType(), client.getRoomId());
            }
        } catch (Exception exception) {
            getLog().error("getRoom: error {}", exception.getMessage(), exception);
        }
        return room;
    }

    @Override
    IMessageSerializer getSerializer() {
        return serializer;
    }

    @Override
    Logger getLog() {
        return LOG;
    }

    @Override
    UnifiedSocketClient getClient(WebSocketSession session) {
        return clients.get(session.getId());
    }

    @Override
    void onSuccess(TObject message, UnifiedSocketClient client) {
    }

    @Override
    protected void measurePingLatency(WebSocketMessage message, WebSocketSession session) {
        long now = System.currentTimeMillis();
        byte[] bytes = new byte[message.getPayload().readableByteCount()];
        message.getPayload().read(bytes);
        long pingTime = AESEncryptionDecryption.decryptTimestamp(bytes);
        long latency = now - pingTime;
        UnifiedSocketClient client = getClient(session);

        ILatencyResponse response = Latency.Builder.newBuilder(System.currentTimeMillis(), -1, 3)
                .withServerTs(pingTime)
                .withServerAckTs(now)
                .withClientTs(pingTime)
                .withClientAckTs(now)
                .withLatencyValue(latency)
                .build();
        client.sendMessage(response);

        if (latency > latencyThresholdMS){
            getLog().warn("HIGH PING LATENCY: {} ms; Player: {}; sessionId: {};  game: {};",
                    latency, client.getNickname(), client.getSessionId(), client.getGameType().name());
        }else{
            getLog().debug("LOW PING LATENCY: {} ms; Player: {}; sessionId: {};  game: {};",
                    latency, client.getNickname(), client.getSessionId(), client.getGameType().name());
        }
        client.getPingLatencyStatistic().update(latency, "PING LATENCY");
    }
}
