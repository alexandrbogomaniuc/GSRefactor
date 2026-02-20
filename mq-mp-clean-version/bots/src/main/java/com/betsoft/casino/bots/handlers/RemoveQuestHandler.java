package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.RemoveQuest;
import com.betsoft.casino.mp.transport.UpdateQuest;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class RemoveQuestHandler implements IServerMessageHandler<RemoveQuest> {
    private final IRoomBot bot;

    public RemoveQuestHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(RemoveQuest response) {

    }
}
