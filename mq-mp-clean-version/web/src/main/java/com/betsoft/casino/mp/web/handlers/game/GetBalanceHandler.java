package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.GetBalance;
import com.betsoft.casino.mp.transport.GetBalanceResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketSession;

public class GetBalanceHandler extends AbstractRoomHandler<GetBalance, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetBalanceHandler.class);
    private final LobbySessionService lobbySessionService;

    public GetBalanceHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                             MultiNodeRoomInfoService multiNodeRoomInfoService,
                             RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                             LobbySessionService lobbySessionService, ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
    }

    @Override
    public void handle(WebSocketSession session, GetBalance message, IGameSocketClient client) {
        if (client.getRoomId() == null || client.getSeatNumber() < 0) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
            return;
        }

        // TODO: refresh from GS
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
        } else if (lobbySession.getTournamentSession() != null) {
            client.sendMessage(new GetBalanceResponse(System.currentTimeMillis(), message.getRid(),
                    lobbySession.getTournamentSession().getBalance()));
        } else {
            client.sendMessage(new GetBalanceResponse(System.currentTimeMillis(), message.getRid(),
                    lobbySession.getBalance()), message);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
