package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;

public class LobbyErrorHandler implements IServerMessageHandler {

    private final ILobbyBot bot;

    public LobbyErrorHandler(ILobbyBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ITransportObject response) {
        bot.count(Stats.ERRORS);
    }
}
