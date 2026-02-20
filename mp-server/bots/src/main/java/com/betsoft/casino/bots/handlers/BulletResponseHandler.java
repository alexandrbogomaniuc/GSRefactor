package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.Awards;
import com.betsoft.casino.mp.transport.BulletResponse;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class BulletResponseHandler implements IServerMessageHandler<BulletResponse> {
    private final IRoomBot bot;

    public BulletResponseHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(BulletResponse response) {

    }
}
