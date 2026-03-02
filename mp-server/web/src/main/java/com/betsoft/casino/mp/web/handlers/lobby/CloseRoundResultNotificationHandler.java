package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.persister.RoundResultNotificationPersister;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.transport.CloseRoundResultNotification;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

@Component
public class CloseRoundResultNotificationHandler extends MessageHandler<CloseRoundResultNotification, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(CloseRoundResultNotificationHandler.class);

    private RoundResultNotificationPersister roundResultNotificationPersister;

    public CloseRoundResultNotificationHandler(Gson gson, LobbySessionService lobbySessionService,
                                               LobbyManager lobbyManager, CassandraPersistenceManager cpm) {
        super(gson, lobbySessionService, lobbyManager);
        this.roundResultNotificationPersister = cpm.getPersister(RoundResultNotificationPersister.class);
    }

    @Override
    public void handle(WebSocketSession session, CloseRoundResultNotification message, ILobbySocketClient client) {
        if (checkLogin(message, client)) {
            roundResultNotificationPersister.removeNotification(
                    client.getAccountId(), client.getGameType().getGameId(), message.getNotificationId());
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
