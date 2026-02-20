package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IUnifiedBot;
import com.betsoft.casino.mp.transport.CrashStateInfo;

public class CrashStateInfoHandler implements IServerMessageHandler<CrashStateInfo> {

    private final IUnifiedBot bot;

    public CrashStateInfoHandler(IUnifiedBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(CrashStateInfo response) {
        bot.setCurrentMultiplier(response.getCurrentMult());
    }
}
