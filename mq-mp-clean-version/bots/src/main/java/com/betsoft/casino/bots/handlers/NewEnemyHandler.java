package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.transport.NewEnemy;

public class NewEnemyHandler implements IServerMessageHandler<NewEnemy> {

    private final IRoomBot bot;

    public NewEnemyHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(NewEnemy response) {
        bot.count(Stats.NEW_ENEMY);
        bot.addEnemy(response.getNewEnemy());
    }
}
