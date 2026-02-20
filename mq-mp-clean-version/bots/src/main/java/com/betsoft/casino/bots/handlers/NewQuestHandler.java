package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.LevelUp;
import com.betsoft.casino.mp.transport.NewQuest;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class NewQuestHandler implements IServerMessageHandler<NewQuest> {
    private final IRoomBot bot;

    public NewQuestHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(NewQuest response) {

    }
}
