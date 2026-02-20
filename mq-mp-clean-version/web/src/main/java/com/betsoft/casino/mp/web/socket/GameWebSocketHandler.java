package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.ICAFRoom;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.ILatencyResponse;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.web.GameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.game.*;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.utils.AESEncryptionDecryption;
import com.betsoft.casino.utils.TObject;
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
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 03.11.17.
 */
@Component
public class GameWebSocketHandler extends AbstractWebSocketHandler<GameSocketClient> implements WebSocketHandler {
    private static final Logger LOG = LogManager.getLogger(GameWebSocketHandler.class);
    protected final IMessageSerializer serializer;
    protected final SyncLobbyHandler syncLobbyHandler;
    protected final RoomPlayersMonitorService roomPlayersMonitorService;
    protected ConcurrentMap<String, GameSocketClient> connectedClients = new ConcurrentHashMap<>();
    protected final RoomServiceFactory roomServiceFactory;
    protected final BotManagerService botManagerService;

    @Autowired
    public GameWebSocketHandler(IMessageSerializer serializer,
                                SingleNodeRoomInfoService singleNodeRoomInfoService,
                                MultiNodeRoomInfoService multiNodeRoomInfoService,
                                RoomPlayerInfoService playerInfoService,
                                RoomServiceFactory roomServiceFactory,
                                RoomPlayersMonitorService roomPlayersMonitorService,
                                ServerConfigService serverConfigService,
                                OpenRoomHandler openRoomHandler,
                                SitInHandler sitInHandler,
                                SitOutHandler sitOutHandler,
                                BuyInHandler buyInHandler,
                                ReBuyHandler reBuyHandler,
                                GetFullGameInfoHandler getFullGameInfoHandler,
                                PurchaseWeaponLootBoxHandler purchaseWeaponLootBoxHandler,
                                RefreshBalanceHandler refreshBalanceHandler,
                                SwitchWeaponHandler switchWeaponHandler,
                                CloseRoundResultsHandler closeRoundResultsHandler,
                                MinesHandler minesHandler,
                                FreeShotHandler freeShotHandler,
                                BetLevelHandler betLevelHandler,
                                BulletHandler bulletHandler,
                                BulletClearHandler bulletClearHandler,
                                ConfirmBattlegroundBuyInHandler confirmBattlegroundBuyInHandler,
                                UpdateWeaponPaidMultiplierHandler updateWeaponPaidMultiplierHandler,
                                CrashBetHandler crashBetHandler,
                                CrashCancelBetHandler crashCancelBetHandler,
                                CrashCancelAllBetsHandler crashCancelAllBetsHandler,
                                CrashBetsHandler crashBetsHandler,
                                StartBattlegroundPrivateRoomHandler startBattlegroundPrivateRoomHandler,
                                PendingOperationHandler pendingOperationHandler,
                                KickHandler kickHandler,
                                CancelKickHandler cancelKickHandler,
                                PrivateRoomInviteHandler privateRoomInviteHandler,
                                LatencyHandler latencyHandler,
                                BotManagerService botManagerService) {
        this.serializer = serializer;
        this.roomPlayersMonitorService = roomPlayersMonitorService;
        this.roomServiceFactory = roomServiceFactory;
        this.botManagerService = botManagerService;
        register(OpenRoom.class, openRoomHandler);
        register(CloseRoom.class, new CloseRoomHandler(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService,
                roomServiceFactory, serverConfigService, this::closeConnection));
        register(SitIn.class, sitInHandler);
        register(SitOut.class, sitOutHandler);
        register(Shot.class, new ShotHandler(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory,
                serverConfigService));
        register(BuyIn.class, buyInHandler);
        register(ReBuy.class, reBuyHandler);
        register(GetFullGameInfo.class, getFullGameInfoHandler);
        register(PurchaseWeaponLootBox.class, purchaseWeaponLootBoxHandler);
        register(RefreshBalance.class, refreshBalanceHandler);
        register(SwitchWeapon.class, switchWeaponHandler);
        register(CloseRoundResults.class, closeRoundResultsHandler);
        syncLobbyHandler = new SyncLobbyHandler(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService,
                roomServiceFactory, serverConfigService);
        register(SyncLobby.class, syncLobbyHandler);
        register(MineCoordinates.class, minesHandler);
        register(AddFreeShotsToQueue.class, freeShotHandler);
        register(BetLevel.class, betLevelHandler);
        register(Bullet.class, bulletHandler);
        register(BulletClear.class, bulletClearHandler);
        register(ConfirmBattlegroundBuyIn.class, confirmBattlegroundBuyInHandler);
        register(UpdateWeaponPaidMultiplier.class, updateWeaponPaidMultiplierHandler);
        register(CrashBet.class, crashBetHandler);
        register(CrashBets.class, crashBetsHandler);
        register(CrashCancelBet.class, crashCancelBetHandler);
        register(CrashCancelAllBets.class, crashCancelAllBetsHandler);
        register(StartBattlegroundPrivateRoom.class, startBattlegroundPrivateRoomHandler);
        register(CheckPendingOperationStatus.class, pendingOperationHandler);
        register(Kick.class, kickHandler);
        register(CancelKick.class, cancelKickHandler);
        register(PrivateRoomInvite.class, privateRoomInviteHandler);
        register(Latency.class, latencyHandler);
        setLocalDevOriginsAllowed(serverConfigService.getConfig().isLocalDevAllowed());

    }

    @Override
    void createConnection(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        getLog().debug("Game createConnection: sessionId={}, handshakeInfo={}", session.getId(),
                session.getHandshakeInfo());
        connectedClients.put(session.getId(), new GameSocketClient(session, null, null, session.getId(), sink, serializer, null));
        startPing(session, sink);
        if (latencyStandardTypeEnabled) {
            Disposable latencyDisposable = startLatencyMeasurement(session);
            registerLatencyDisposable(session, latencyDisposable);
        }
    }

    @Override
    protected boolean isConnected(WebSocketSession session) {
        return connectedClients.containsKey(session.getId());
    }

    public void addBotConnection(String botId, BotGameClient botClient) {
        connectedClients.put(botId, botClient);
    }

    @Override
    void closeConnection(WebSocketSession session) {

        getLog().debug("closeConnection: WebSocketSession={}", session);

        roomPlayersMonitorService.removeSocketClientInfo(session.getId());

        GameSocketClient client = connectedClients.get(session.getId());
        getLog().debug("closeConnection: WebSocketSession.id={}, socketClient={}", session.getId(), client);

        if (client != null ) {

            ISeat seat = client.getSeat();
            getLog().debug("closeConnection: WebSocketSession.id={}, seat={}", session.getId(), seat);

            if(seat != null) {
                if (client.getRoomId() != null && seat instanceof IActionGameSeat) {
                    //noinspection rawtypes
                    syncLobbyHandler.syncLobby((IActionGameSeat) seat, client.getRoomId());
                }
                seat.setWantSitOut(true);
                ((BulletClearHandler) getHandler(BulletClear.class)).clearBullets(client);
                // TODO: Set seat disconnected
            }

            logSessionLatency(client);
            client.setDisconnected();
            connectedClients.remove(session.getId());

            IRoom room = getRoom(client);
            if(room != null) {
                if(this.botManagerService.isBot(client.getNickname())) {
                    room.removeObserverByAccountId(client.getAccountId());
                }
                sendGameInfoToAllObserversIfRequired(session.getId(), room);
            }
        }

        unregisterLatencyDisposable(session);
    }

    private IRoom getRoom(GameSocketClient client) {
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

    private void sendGameInfoToAllObserversIfRequired(String sessionId, IRoom room) {

        try {

            if (room == null) {
                getLog().warn("sendGameInfoToAllObserversIfRequired: WebSocketSession.id={}, room is null",
                        sessionId);
            } else {

                long roomId = room.getId();
                IRoomInfo roomInfo = room.getRoomInfo();

                if (roomInfo == null) {
                    getLog().error("sendGameInfoToAllObserversIfRequired: WebSocketSession.id={}, " +
                            "roomInfo is null for room {}", sessionId, roomId);
                } else {

                    if (!roomInfo.isBattlegroundMode()) {
                        getLog().debug("sendGameInfoToAllObserversIfRequired: WebSocketSession.id={}, " +
                                "room {} is non BG mode, skip", sessionId, roomId);
                    } else {

                        if (roomInfo.isPrivateRoom()) {
                            getLog().debug("sendGameInfoToAllObserversIfRequired: WebSocketSession.id={}, " +
                                    "room {} is private BG, skip", sessionId, roomId);
                        } else {

                            if (room instanceof ICAFRoom) {

                                getLog().debug("sendGameInfoToAllObserversIfRequired: WebSocketSession.id={}, " +
                                        "room {} call sendGameInfoToAllObservers", sessionId, roomId);

                                ((ICAFRoom) room).sendGameInfoToAllObservers();

                            } else {
                                getLog().debug("sendGameInfoToAllObserversIfRequired: WebSocketSession.id={}, " +
                                        "room {} is not ICAFRoom, skip", sessionId, roomId);
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            getLog().error("sendGameInfoToAllObserversIfRequired: WebSocketSession.id={}, error {}",
                    sessionId, exception.getMessage(), exception);
        }
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
    GameSocketClient getClient(WebSocketSession session) {
        return connectedClients.get(session.getId());
    }

    @Override
    void onSuccess(TObject message, GameSocketClient client) {
        client.setLastRequestId(message.getRid());
    }

    @Override
    protected void measurePingLatency(WebSocketMessage message, WebSocketSession session) {
        long now = System.currentTimeMillis();
        byte[] bytes = new byte[message.getPayload().readableByteCount()];
        message.getPayload().read(bytes);
        long pingTime = AESEncryptionDecryption.decryptTimestamp(bytes);
        long latency = now - pingTime;
        GameSocketClient client = getClient(session);

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
