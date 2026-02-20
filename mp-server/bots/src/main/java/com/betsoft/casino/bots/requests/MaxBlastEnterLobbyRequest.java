package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.mp.transport.EnterLobby;
import com.betsoft.casino.mp.web.ISocketClient;

public class MaxBlastEnterLobbyRequest extends EnterLobbyRequest {
    private final Long selectedBuyIn;

    public MaxBlastEnterLobbyRequest(ILobbyBot bot, ISocketClient client, int gameId, int serverId, String sessionId, Long selectedBuyIn) {
        super(bot, client, gameId, serverId, sessionId);
        this.selectedBuyIn = selectedBuyIn;
    }

    @Override
    public void send(int rid) {
        EnterLobby enterLobby = new EnterLobby(System.currentTimeMillis(), sessionId, "en", rid, serverId, "real",
                false, gameId, selectedBuyIn, false, false);
        client.sendMessage(enterLobby);
    }
}
