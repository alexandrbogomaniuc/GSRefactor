package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.PendingOperationStatus;

public class PendingOperationStatusHandler implements IServerMessageHandler<PendingOperationStatus> {
    private final IRoomBot bot;

    public PendingOperationStatusHandler(IRoomBot bot) {
        this.bot = bot;
    }


    @Override
    public void handle(PendingOperationStatus response) {

    }
}
