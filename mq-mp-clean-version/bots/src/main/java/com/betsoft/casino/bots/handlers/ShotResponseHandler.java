package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.ShotResponse;

public class ShotResponseHandler implements IServerMessageHandler<ShotResponse> {
    private final IRoomBot bot;

    public ShotResponseHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ShotResponse response) {
        //TODO
    }
}
