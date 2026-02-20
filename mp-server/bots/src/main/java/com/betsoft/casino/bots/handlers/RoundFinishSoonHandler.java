package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.mp.transport.RoundFinishSoon;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class RoundFinishSoonHandler implements IServerMessageHandler<RoundFinishSoon> {
    private final IRoomBot bot;

    public RoundFinishSoonHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(RoundFinishSoon response) {
        if (bot instanceof RoomBot) {
            bot.getLogger().debug("RoundFinishSoonHandler RoundFinishSoon response: {}", response);
            ((RoomBot) bot).setRoundFinishSoonReceived(true);
        }
    }
}
