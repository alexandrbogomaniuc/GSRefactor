package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.EnemyDestroyed;

public class EnemyDestroyedHandler implements IServerMessageHandler<EnemyDestroyed> {

    private final IRoomBot bot;

    public EnemyDestroyedHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(EnemyDestroyed response) {
        bot.removeEnemy(response.getEnemyId());
    }
}
