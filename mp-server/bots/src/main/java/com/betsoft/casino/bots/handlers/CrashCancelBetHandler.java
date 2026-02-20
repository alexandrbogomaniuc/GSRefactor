package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IUnifiedBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.mp.transport.CrashCancelBetResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrashCancelBetHandler implements IServerMessageHandler<CrashCancelBetResponse> {
    private static final Logger LOG = LogManager.getLogger(CrashCancelBetHandler.class);
    private final IUnifiedBot bot;

    public CrashCancelBetHandler(IUnifiedBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(CrashCancelBetResponse response) {

        LOG.debug("CrashCancelBetHandler::handle: botId={}, botNickname={}, response.getName()={}",
                bot.getId(), bot.getNickname(), response.getName());

        if(bot instanceof ManagedMaxBlastChampionsRoomBot) {
            ManagedMaxBlastChampionsRoomBot maxBlastBot = (ManagedMaxBlastChampionsRoomBot) bot;
            maxBlastBot.processCrashCancelBet(response.getCrashBetId());
        }

        if (bot.getNickname().equals(response.getName())) {
            bot.count(Stats.AUTO_EJECT);
        }
    }
}
