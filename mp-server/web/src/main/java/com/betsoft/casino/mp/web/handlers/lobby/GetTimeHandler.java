package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.transport.GetLobbyTime;
import com.betsoft.casino.mp.transport.LobbyTimeUpdated;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

@Component
public class GetTimeHandler extends MessageHandler<GetLobbyTime, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetTimeHandler.class);

    public GetTimeHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager) {
        super(gson, lobbySessionService, lobbyManager);
    }

    @Override
    public void handle(WebSocketSession session, GetLobbyTime message, ILobbySocketClient client) {
        client.sendMessage(new LobbyTimeUpdated(getCurrentTime(), message.getRid()), message);

    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
