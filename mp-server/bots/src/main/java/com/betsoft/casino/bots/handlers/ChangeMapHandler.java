package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.mp.transport.ChangeMap;

/**
 * User: flsh
 * Date: 14.09.18.
 */
public class ChangeMapHandler implements IServerMessageHandler<ChangeMap> {
    private final IRoomBot bot;

    public ChangeMapHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(ChangeMap response) {
        if(bot instanceof RoomBot){
            RoomBot roomBot = (RoomBot) bot;
            roomBot.setCurrentMapId(response.getMapId());
            roomBot.setCurrentSubround(response.getSubround());
            bot.getLogger().debug("changed mapId: {}, subround: {} for bot: {}", response.getMapId(), response.getSubround(), bot.getId());
        }
    }
}
