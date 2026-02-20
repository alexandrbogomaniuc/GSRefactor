package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.Hit;
import com.betsoft.casino.mp.transport.Miss;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class MissHandler implements IServerMessageHandler<Miss> {
    private final IRoomBot bot;

    public MissHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Miss response) {
        if (response.getServerAmmo() > 0 && response.getSeatId() == bot.getSeatId()) {
            bot.setServerAmmo(response.getServerAmmo());
        }
    }
}
