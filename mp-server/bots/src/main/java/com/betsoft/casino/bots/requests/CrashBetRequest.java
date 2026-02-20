package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IUnifiedBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.mp.transport.CrashBet;
import com.betsoft.casino.mp.transport.CrashBetResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class CrashBetRequest extends AbstractBotRequest {
    private final int crashBetAmount;
    private final double multiplier;
    private final boolean autoPlay;
    private final String betId;
    private final IUnifiedBot bot;
    private final ISocketClient client;

    public CrashBetRequest(IUnifiedBot bot, ISocketClient client, int crashBetAmount, double multiplier, String betId) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.crashBetAmount = crashBetAmount;
        this.multiplier = multiplier;
        this.autoPlay = multiplier > 1.0;
        this.betId = betId;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new CrashBet(System.currentTimeMillis(), rid, crashBetAmount, multiplier, autoPlay, betId));
    }

    @Override
    public void handle(ITransportObject response) {

        switch (response.getClassName()) {

            case "CrashBetResponse":

                if(bot instanceof ManagedMaxBlastChampionsRoomBot) {
                    ManagedMaxBlastChampionsRoomBot maxBlastBot = (ManagedMaxBlastChampionsRoomBot)bot;
                    CrashBetResponse crashBetResponse = (CrashBetResponse) response;
                    maxBlastBot.processCrashBet(crashBetResponse.getCrashBetKey());
                }

                bot.incrementAstronaut();
                bot.count(Stats.CRASH_BET);
                bot.setState(BotState.PLAYING, "CrashBetRequest: betSuccess");

                break;

            case "Error":

                com.betsoft.casino.mp.transport.Error error = (com.betsoft.casino.mp.transport.Error) response;

                //not error, round already started
                if (error.getCode() == ErrorCodes.BUYIN_NOT_ALLOWED) {
                    getLogger().warn("CrashBetRequest: round started, failed to bet: {}", response);
                } else {
                    getLogger().error("CrashBetRequest: failed to bet: {}", response);
                }
                bot.count(Stats.FAILED_CRASH_BET);
                bot.setState(BotState.WAIT_BATTLE_PLAYERS, "CrashBetRequest: betError");
                if (bot.getAstronautsCount() <= 0) {
                    getLogger().debug("CrashBetRequest: first bet failed, sitOut");
                    bot.sendSitOutRequest();
                }
                break;
            default:
                getLogger().error("CrashBetRequest: Unexpected response type: {}", response.getClassName());
                break;
        }
    }
}
