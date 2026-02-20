package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.UnifiedBot;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.mp.transport.SitOutResponse;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles SitOut messages
 */
public class SitOutHandler implements IServerMessageHandler<SitOutResponse> {
    private static final Logger LOG = LogManager.getLogger(SitOutHandler.class);
    private final IRoomBot bot;

    public SitOutHandler(IRoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(SitOutResponse response) {
        if (response.getNickname().equals(bot.getNickname())) {
            bot.count(Stats.SELF_SIT_OUT);
        } else {
            bot.count(Stats.OTHER_PLAYER_SIT_OUT);
        }

        String responseNickname = response.getNickname();
        String botNickname = StringUtils.isTrimmedEmpty(bot.getNickname()) ? "" : bot.getNickname();
        LOG.debug("SitOutHandler::handle: botId:{} botNickname={}, SitOut responseNickname={}, ",
                bot.getId(), botNickname, responseNickname);

        if (bot instanceof ManagedMaxBlastChampionsRoomBot) {
            ManagedMaxBlastChampionsRoomBot maxBlastBot = (ManagedMaxBlastChampionsRoomBot) bot;
            maxBlastBot.removePlayer(responseNickname);
            LOG.debug("SitOutHandler::handle: botId={}, user responseNickname={} removed from players list:{}",
                    bot.getId(), responseNickname, maxBlastBot.getPlayers().keySet().toArray());
        }

        if (bot instanceof ManagedBattleGroundRoomBot && bot.getNickname().equals(responseNickname)) {
            LOG.debug("SitOutHandler::handle: botId:{} call stop", bot.getId());
            bot.stop();
        }
    }
}
