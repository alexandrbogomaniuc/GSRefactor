package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.ConfirmBattlegroundBuyIn;
import com.betsoft.casino.mp.transport.Ok;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

@Component
public class ConfirmBattlegroundBuyInHandler extends AbstractRoomHandler<ConfirmBattlegroundBuyIn, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(ConfirmBattlegroundBuyInHandler.class);
    protected final LobbySessionService lobbySessionService;

    public ConfirmBattlegroundBuyInHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                           MultiNodeRoomInfoService multiNodeRoomInfoService,
                                           RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                           ServerConfigService serverConfigService,
                                           LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
    }

    @Override
    public void handle(WebSocketSession session, ConfirmBattlegroundBuyIn message, IGameSocketClient client) {
        if (client.getRoomId() == null || client.getAccountId() == null) {
            sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Room not open", message.getRid(), message);
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }
        if (!lobbySession.isBattlegroundAllowed()) {
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Battleground mode not allowed", message.getRid());
            return;
        }

        try {
            if (hasPendingOperations(client.getAccountId(), client, message)) {
                return;
            }
            playerInfoService.lock(client.getAccountId());
            getLog().debug("handle HS lock: {}", client.getAccountId());
            try {
                lobbySession.setConfirmBattlegroundBuyIn(true);
                lobbySessionService.add(lobbySession);
                getLog().debug("setConfirmBattlegroundBuyIn to true accountId={}, message={}", client.getAccountId(), message);
                client.sendMessage(new Ok(System.currentTimeMillis(), message.getRid()), message);
            } finally {
                playerInfoService.unlock(client.getAccountId());
                getLog().debug("handle HS unlock: {}", client.getAccountId());
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
