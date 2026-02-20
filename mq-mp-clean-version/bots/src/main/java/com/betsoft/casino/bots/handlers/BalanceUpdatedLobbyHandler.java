package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.mp.transport.BalanceUpdated;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class BalanceUpdatedLobbyHandler implements IServerMessageHandler<BalanceUpdated> {
    private final ILobbyBot bot;

    public BalanceUpdatedLobbyHandler(ILobbyBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(BalanceUpdated response) {
        bot.setBalance(response.getBalance());
    }
}
