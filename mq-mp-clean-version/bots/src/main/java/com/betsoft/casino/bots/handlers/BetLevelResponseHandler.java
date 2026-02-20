package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.mp.transport.BetLevelResponse;

public class BetLevelResponseHandler implements IServerMessageHandler<BetLevelResponse> {

    private final RoomBot bot;

    public BetLevelResponseHandler(RoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(BetLevelResponse response) {

    }
}
