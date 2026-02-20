package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IUnifiedBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.UnifiedBot;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.mp.transport.CrashCancelBet;
import com.betsoft.casino.mp.transport.CrashCancelBetResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class CrashCancelBetRequest extends AbstractBotRequest {

    private final String crashBetId;
    private final boolean placeNewBet;
    private final IUnifiedBot bot;
    private final ISocketClient client;

    public CrashCancelBetRequest(String crashBetId, boolean placeNewBet, IUnifiedBot bot, ISocketClient client) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.crashBetId = crashBetId;
        this.placeNewBet = placeNewBet;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new CrashCancelBet(System.currentTimeMillis(), rid, crashBetId));
    }

    @Override
    public void handle(ITransportObject response) {

        switch (response.getClassName()) {

            case "CrashCancelBetResponse":
                getLogger().debug("CrashCancelBetRequest: botId={}, nickname={}, botState={}",
                        bot.getId(), bot.getNickname(), bot.getState());
                bot.count(Stats.SELF_EJECT);

                if(bot instanceof ManagedMaxBlastChampionsRoomBot) {

                    ManagedMaxBlastChampionsRoomBot maxBlastRoomBot = (ManagedMaxBlastChampionsRoomBot) bot;
                    CrashCancelBetResponse crashBetResponse = (CrashCancelBetResponse) response;
                    maxBlastRoomBot.processCrashCancelBet(crashBetResponse.getCrashBetId());
                    maxBlastRoomBot.setState(BotState.WAIT_BATTLE_PLAYERS, "CrashCancelBetResponse: bet cancel Success");

                } else if(bot instanceof UnifiedBot){

                    bot.sleep(200).subscribe(t ->
                            bot.setState(BotState.PLAYING, "CrashCancelBetResponse: bet cansel Success"));
                }
                break;

            case "Error":

                com.betsoft.casino.mp.transport.Error error = (com.betsoft.casino.mp.transport.Error) response;

                //this error may be if round completed
                if (error.getCode() == ErrorCodes.BET_NOT_FOUND || error.getCode() == ErrorCodes.NOT_SEATER) {
                    bot.getLogger().warn("CrashCancelBetRequest failed: {}", response);
                } else {
                    bot.getLogger().error("CrashCancelBetRequest failed: {}", response);
                }

                bot.count(Stats.FAILED_SELF_EJECT);

                break;

            default:
                getLogger().error("CrashCancelBetRequest: Unexpected response type: {}", response.getClassName());
        }
    }
}
