package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.transport.NewEnemies;

public class NewEnemiesHandler implements IServerMessageHandler<NewEnemies> {
    private final IRoomBot bot;

    public NewEnemiesHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(NewEnemies response) {
        bot.count(Stats.NEW_ENEMY, response.getEnemies().size());
        bot.addEnemies(response.getEnemies());
    }
}
