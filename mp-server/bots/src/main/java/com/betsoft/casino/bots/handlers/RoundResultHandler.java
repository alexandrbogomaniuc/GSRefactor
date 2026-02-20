package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.BattleGroundRoomBot;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TObject;

public class RoundResultHandler implements IServerMessageHandler {

    private final IRoomBot bot;

    public RoundResultHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ITransportObject response) {
        bot.count(Stats.ROUND_RESULT);
        bot.getStrategy().resetWeapons();
        if(bot instanceof RoomBot){
            ((RoomBot) bot).setRoundResultReceived(true);
        }
    }
}
