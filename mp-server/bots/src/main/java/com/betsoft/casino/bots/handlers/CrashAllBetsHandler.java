package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.transport.CrashAllBetsResponse;

public class CrashAllBetsHandler implements IServerMessageHandler<CrashAllBetsResponse>{

    private final IRoomBot bot;

    public CrashAllBetsHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(CrashAllBetsResponse response) {
        if (bot.getNickname().equals(response.getName())) {
            bot.count(Stats.CRASH_BET);
        }
    }
}
