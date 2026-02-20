package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class CrashSitInRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private final String lang;

    public CrashSitInRequest(IRoomBot bot, ISocketClient client, String lang) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.lang = lang;
    }

    @Override
    public boolean isSingleResponse() {
        return false;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new SitIn(System.currentTimeMillis(), rid, lang));
    }

    @Override
    public void handle(ITransportObject response) {
        boolean needSleep = true;
        switch (response.getClassName()) {
            case "SitInResponse":
                SitInResponse sitInResponse = (SitInResponse) response;
                String sitInNickname = sitInResponse.getNickname();

                if (!bot.getNickname().equals(sitInNickname)) {
                    getLogger().debug("CrashSitInRequest::SitInResponse: other player sitInResponse: {}", sitInNickname);
                } else {
                    bot.setBalance(sitInResponse.getBalance());
                    bot.setSeatId(sitInResponse.getId());
                    bot.count(Stats.SELF_SIT_IN);

                    if (bot.getState() == BotState.PLAYING ) {
                        needSleep = false;
                    } else {
                        BotState botState = BotState.PLAYING;
                        if(bot instanceof ManagedMaxBlastChampionsRoomBot) {
                            botState = BotState.WAIT_BATTLE_PLAYERS;
                        }
                        getLogger().debug("CrashSitInRequest::SitInResponse: botState={} -> {}", bot.getState(), botState);
                        bot.setState(botState, "CrashSitInRequest: SitInResponse");
                    }
                }
                break;
            case "FullGameInfo":
                needSleep = false;
                break;
            case "Error":
                getLogger().error("CrashSitInRequest::Error: sitInResponse failed: {}", response);
                bot.count(Stats.ERRORS);
                Error errorResponse = (Error) response;
                int code = errorResponse.getCode();
                if (code == ErrorCodes.TOO_MANY_PLAYER) {
                    bot.count(Stats.TOO_MANY_PLAYERS_ERROR);
                    bot.setState(BotState.WAITING_FOR_RESPONSE, "CrashSitInRequest: Error=" + errorResponse.getMsg());
                    bot.sendCloseRoomRequest();
                } else {
                    bot.restart();
                }
                needSleep = false;
                break;
            default:
                getLogger().error("CrashSitInRequest: unexpected response type: {}", response);
                break;
        }
        if (needSleep) {
            bot.doActionWithSleep(1000, "CrashSitInRequest[" + response.getClassName() + "]");
        }
    }

    @Override
    public String toString() {
        return "CrashSitInRequest{" +
                "bot=" + bot +
                ", client=" + client +
                ", lang='" + lang + '\'' +
                '}';
    }
}
