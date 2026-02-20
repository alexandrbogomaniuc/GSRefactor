package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.Awards;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class AwardsHandler implements IServerMessageHandler<Awards> {
    private final IRoomBot bot;

    public AwardsHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(Awards response) {

    }
}
