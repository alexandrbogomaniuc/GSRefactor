package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.ILobbySession;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.RoomPlayersMonitorService;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.lobby.*;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class LobbyWebSocketHandler extends AbstractWebSocketHandler<LobbySocketClient> implements WebSocketHandler {

    private static final Logger LOG = LogManager.getLogger(LobbyWebSocketHandler.class);
    private final IMessageSerializer serializer;

    private final ConcurrentHashMap<String, LobbySocketClient> clients = new ConcurrentHashMap<>();
    private final LobbySessionService lobbySessionService;
    protected final RoomPlayerInfoService playerInfoService;
    protected final RoomPlayersMonitorService roomPlayersMonitorService;

    @Autowired
    public LobbyWebSocketHandler(IMessageSerializer serializer,
                                 LobbySessionService lobbySessionService,
                                 RoomPlayerInfoService playerInfoService,
                                 RoomPlayersMonitorService roomPlayersMonitorService,
                                 ServerConfigService serverConfigService,
                                 EnterLobbyHandler enterLobbyHandler,
                                 GetRoomInfoHandler getRoomInfoHandler,
                                 GetStartGameUrlHandler getStartGameUrlHandler,
                                 CheckNicknameAvailabilityHandler checkNicknameAvailabilityHandler,
                                 ChangeNicknameHandler changeNicknameHandler,
                                 ChangeAvatarHandler changeAvatarHandler,
                                 LobbyRefreshBalanceHandler lobbyRefreshBalanceHandler,
                                 GetTimeHandler getTimeHandler,
                                 CloseRoundResultNotificationHandler closeRoundResultNotificationHandler,
                                 ChangeTooltipsHandler changeTooltipsHandler,
                                 CollectQuestsHandler collectQuestsHandler,
                                 GetQuestsHandler getQuestsHandler,
                                 GetWeaponsHandler getWeaponsHandler,
                                 LobbyReBuyHandler lobbyReBuyHandler,
                                 GetBattlegroundStartGameUrlHandler battlegroundStartGameUrlHandler,
                                 GetPrivateBattlegroundStartGameUrlHandler privateBattlegroundStartGameUrlHandler,
                                 PendingOperationLobbyHandler pendingOperationHandler,
                                 FinishGameSessionHandler finishGameSessionHandler) {

        this.serializer = serializer;
        this.lobbySessionService = lobbySessionService;
        this.playerInfoService = playerInfoService;
        this.roomPlayersMonitorService = roomPlayersMonitorService;
        register(EnterLobby.class, enterLobbyHandler);
        register(GetRoomInfo.class, getRoomInfoHandler);
        register(GetStartGameUrl.class, getStartGameUrlHandler);
        register(CheckNicknameAvailability.class, checkNicknameAvailabilityHandler);
        register(ChangeNickname.class, changeNicknameHandler);
        register(ChangeAvatar.class, changeAvatarHandler);
        register(RefreshBalance.class, lobbyRefreshBalanceHandler);
        register(GetLobbyTime.class, getTimeHandler);
        register(CloseRoundResultNotification.class, closeRoundResultNotificationHandler);
        register(ChangeToolTips.class, changeTooltipsHandler);
        register(CollectQuest.class, collectQuestsHandler);
        register(GetQuests.class, getQuestsHandler);
        register(GetWeapons.class, getWeaponsHandler);
        register(ReBuy.class, lobbyReBuyHandler);
        register(GetBattlegroundStartGameUrl.class, battlegroundStartGameUrlHandler);
        register(GetPrivateBattlegroundStartGameUrl.class, privateBattlegroundStartGameUrlHandler);
        register(CheckPendingOperationStatus.class, pendingOperationHandler);
        register(FinishGameSession.class, finishGameSessionHandler);
        setLocalDevOriginsAllowed(serverConfigService.getConfig().isLocalDevAllowed());
    }

    @Override
    void createConnection(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        getLog().debug("Lobby createConnection: sessionId={}, handshakeInfo={}", session.getId(),
                session.getHandshakeInfo());
        clients.put(session.getId(), new LobbySocketClient(session, session.getId(), sink, serializer));
        startPing(session, sink);
    }

    @Override
    protected boolean isConnected(WebSocketSession session) {
        return clients.containsKey(session.getId());
    }

    @PostConstruct
    private void init() {
        // nop
    }

    @Override
    void closeConnection(WebSocketSession session) {
        getLog().debug("closeConnection: {}", session.getId());

        roomPlayersMonitorService.removeSocketClientInfo(session.getId());

        ILobbySocketClient lobbySocketClient = clients.get(session.getId());

        if (lobbySocketClient != null) {

            Long accountId = lobbySocketClient.getAccountId();
            String nickname = lobbySocketClient.getNickname();

            getLog().debug("closeConnection: accountId={}, nickname={}, sessionId={} found lobbySocketClient={}",
                    accountId, nickname, session.getId(), lobbySocketClient);

            if (accountId != null) {
                roomPlayersMonitorService.removeSocketClientInfoForAccountId(accountId);
            }

            lobbySocketClient.stopBalanceUpdater();
            lobbySocketClient.stopTouchSession();

            String sid = lobbySocketClient.getSessionId();

            if (!StringUtils.isTrimmedEmpty(sid)) {

                ILobbySession lobbySession = lobbySessionService.get(sid);

                if (lobbySession != null && lobbySession.getWebsocketSessionId() != null &&
                        lobbySession.getWebsocketSessionId().equals(session.getId())) {

                    accountId = lobbySession.getAccountId();

                    getLog().debug("closeConnection: accountId={}, nickname={}, found lobbySession with same " +
                            "web websocketSessionId", accountId, nickname);

                    boolean locked = false;
                    try {

                        locked = playerInfoService.tryLock(accountId, 10, TimeUnit.SECONDS);
                        if (locked) {
                            lobbySessionService.remove(sid);
                        }

                    } catch (InterruptedException e) {
                        LOG.warn("Cannot player lock closeConnection, interrupted", e);
                    } finally {
                        if (locked) {
                            playerInfoService.unlock(accountId);
                        }
                    }
                }
            }
            clients.remove(session.getId());

            try {
                lobbySessionService.processCloseLobbyConnection(lobbySocketClient);
            } catch (Exception e) {
                LOG.error("Exception during processCloseLobbyConnection run: {}", e.getMessage(), e);
            }
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
    LobbySocketClient getClient(WebSocketSession session) {
        return clients.get(session.getId());
    }

    @Override
    void onSuccess(TObject message, LobbySocketClient client) {
    }
}
