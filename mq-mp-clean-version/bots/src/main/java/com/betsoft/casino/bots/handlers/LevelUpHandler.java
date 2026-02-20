package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.Hit;
import com.betsoft.casino.mp.transport.LevelUp;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class LevelUpHandler implements IServerMessageHandler<LevelUp> {
    private final IRoomBot bot;

    public LevelUpHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(LevelUp response) {

    }
}
