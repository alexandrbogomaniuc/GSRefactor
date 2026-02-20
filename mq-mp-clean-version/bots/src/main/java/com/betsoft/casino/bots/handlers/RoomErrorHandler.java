package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;

public class RoomErrorHandler implements IServerMessageHandler {

    private final IRoomBot bot;

    public RoomErrorHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ITransportObject response) {
        bot.count(Stats.ERRORS);
    }
}
