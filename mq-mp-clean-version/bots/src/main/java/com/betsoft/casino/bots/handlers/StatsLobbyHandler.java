package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.mp.transport.Stats;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class StatsLobbyHandler implements IServerMessageHandler<Stats> {
    private final ILobbyBot bot;

    public StatsLobbyHandler(ILobbyBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Stats response) {

    }
}
