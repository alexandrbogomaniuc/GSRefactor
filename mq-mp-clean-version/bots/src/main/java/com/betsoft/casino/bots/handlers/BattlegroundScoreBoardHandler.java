package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.BattleGroundRoomBot;
import com.betsoft.casino.utils.ITransportObject;

public class BattlegroundScoreBoardHandler implements IServerMessageHandler {

    private final BattleGroundRoomBot bot;

    public BattlegroundScoreBoardHandler(BattleGroundRoomBot  bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ITransportObject response) {
    }
}
