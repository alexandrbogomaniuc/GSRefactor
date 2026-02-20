package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.BattleGroundRoomBot;
import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.Stats;
//import com.betsoft.casino.mp.transport.CancelBattlegroundRound;
import com.betsoft.casino.utils.ITransportObject;

public class CancelBattlegroundRoundHandler implements IServerMessageHandler {

    private final BattleGroundRoomBot  bot;

    public CancelBattlegroundRoundHandler(BattleGroundRoomBot  bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ITransportObject response) {
        bot.count(Stats.CANCEL_BATTLE_ROUND);
        bot.setState(BotState.NEED_SIT_OUT, "CancelBattlegroundRoundHandler");
    }
}
