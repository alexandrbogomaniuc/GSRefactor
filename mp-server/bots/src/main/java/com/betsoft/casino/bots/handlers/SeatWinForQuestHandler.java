package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.mp.transport.SeatWinForQuest;
import com.betsoft.casino.utils.ITransportObject;

public class SeatWinForQuestHandler implements IServerMessageHandler<SeatWinForQuest> {
    private final RoomBot bot;

    public SeatWinForQuestHandler(RoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(SeatWinForQuest response) {
        // TODO: implement after release - check trajectories to avoid shooting at invisible zones
    }
}
