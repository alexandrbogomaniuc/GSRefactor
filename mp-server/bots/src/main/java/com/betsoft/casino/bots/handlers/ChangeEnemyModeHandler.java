package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.BulletResponse;
import com.betsoft.casino.mp.transport.ChangeEnemyMode;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class ChangeEnemyModeHandler implements IServerMessageHandler<ChangeEnemyMode> {
    private final IRoomBot bot;

    public ChangeEnemyModeHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ChangeEnemyMode response) {

    }
}
