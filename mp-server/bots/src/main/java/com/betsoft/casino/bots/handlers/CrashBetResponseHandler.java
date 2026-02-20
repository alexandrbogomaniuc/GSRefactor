package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.IUnifiedBot;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.mp.transport.CrashBetResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrashBetResponseHandler implements IServerMessageHandler<CrashBetResponse> {
    private static final Logger LOG = LogManager.getLogger(CrashBetResponseHandler.class);
    private final IUnifiedBot bot;

    public CrashBetResponseHandler(IUnifiedBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(CrashBetResponse response) {
        LOG.debug("CrashBetResponseHandler::handle: botId={}, botNickname={}", bot.getId(), bot.getNickname());

        if (bot instanceof ManagedMaxBlastChampionsRoomBot) {
            ManagedMaxBlastChampionsRoomBot maxBlastBot = (ManagedMaxBlastChampionsRoomBot) bot;
            maxBlastBot.processCrashBet(response.getCrashBetKey());
        }

    }
}
