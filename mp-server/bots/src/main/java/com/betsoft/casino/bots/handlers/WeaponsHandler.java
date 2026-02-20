package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.Weapons;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class WeaponsHandler implements IServerMessageHandler<Weapons> {
    private final IRoomBot bot;

    public WeaponsHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Weapons response) {

    }
}
