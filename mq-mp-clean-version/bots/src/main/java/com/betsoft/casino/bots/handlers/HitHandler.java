package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.Awards;
import com.betsoft.casino.mp.transport.Hit;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class HitHandler implements IServerMessageHandler<Hit> {
    private final IRoomBot bot;

    public HitHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Hit response) {
        if (response.getServerAmmo() > 0 && response.getSeatId() == bot.getSeatId()) {
            bot.setServerAmmo(response.getServerAmmo());
        }
    }
}
