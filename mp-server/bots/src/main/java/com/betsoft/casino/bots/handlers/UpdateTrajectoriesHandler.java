package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;

public class UpdateTrajectoriesHandler implements IServerMessageHandler {
    private final RoomBot bot;

    public UpdateTrajectoriesHandler(RoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ITransportObject response) {
        // TODO: implement after release - check trajectories to avoid shooting at invisible zones
    }
}
