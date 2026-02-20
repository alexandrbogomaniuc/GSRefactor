package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.mp.transport.LevelUp;
import com.betsoft.casino.mp.transport.UpdateQuest;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class UpdateQuestHandler implements IServerMessageHandler<UpdateQuest> {
    private final IRoomBot bot;

    public UpdateQuestHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(UpdateQuest response) {

    }
}
