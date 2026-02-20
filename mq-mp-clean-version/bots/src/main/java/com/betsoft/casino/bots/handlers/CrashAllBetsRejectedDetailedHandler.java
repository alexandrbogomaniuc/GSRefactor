package com.betsoft.casino.bots.handlers;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.CrashAllBetsRejectedDetailedResponse;

public class CrashAllBetsRejectedDetailedHandler implements IServerMessageHandler<CrashAllBetsRejectedDetailedResponse> {
    private final IRoomBot bot;

    public CrashAllBetsRejectedDetailedHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(CrashAllBetsRejectedDetailedResponse response) {

    }
}