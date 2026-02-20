package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.MinePlace;

public class MinePlaceHandler implements IServerMessageHandler<MinePlace>{
    private final IRoomBot bot;

    public MinePlaceHandler(IRoomBot bot) {
        this.bot = bot;
    }


    @Override
    public void handle(MinePlace response) {

    }
}
