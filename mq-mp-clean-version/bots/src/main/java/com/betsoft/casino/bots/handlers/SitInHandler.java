package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.mp.transport.SitInResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles SitIn messages about other players
 */
public class SitInHandler implements IServerMessageHandler<SitInResponse> {
    private static final Logger LOG = LogManager.getLogger(SitInHandler.class);
    private final IRoomBot bot;

    public SitInHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(SitInResponse response) {
        if (response.getNickname().equals(bot.getNickname())) {
        } else {
            bot.count(Stats.OTHER_PLAYER_SIT_IN);
        }

        if (bot instanceof ManagedMaxBlastChampionsRoomBot) {
            ManagedMaxBlastChampionsRoomBot maxBlastBot = (ManagedMaxBlastChampionsRoomBot)bot;
            maxBlastBot.addPlayer(response.getNickname());
            LOG.debug("SitInHandler::handle: botId={}, user nickname {} added to players list:{}",
                    bot.getId(), response.getNickname(), maxBlastBot.getPlayers().keySet().toArray());
        }
    }
}
