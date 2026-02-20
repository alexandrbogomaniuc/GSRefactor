package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.*;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.mp.transport.SitOut;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class SitOutRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;

    public SitOutRequest(IRoomBot bot, ISocketClient client) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new SitOut(System.currentTimeMillis(), rid));
    }

    @Override
    public void handle(ITransportObject response) {
        boolean needCloseRoom = true;
        switch (response.getClassName()) {
            case "SitOutResponse":
                if (bot instanceof ManagedBattleGroundRoomBot) {
                    bot.stop();
                    needCloseRoom = false;
                }
                if (bot instanceof UnifiedBot) {
                    ((UnifiedBot) bot).setRoundsCount(0);
                    bot.restart();
                }
                break;
            case "Error":
                bot.count(Stats.ERRORS);
                if (bot instanceof UnifiedBot) {
                    ((UnifiedBot) bot).setRoundsCount(0);
                }
                bot.restart();
                needCloseRoom = false;
                break;
            default:
                getLogger().error("SitOutRequest: unexpected response type");
                break;
        }

        if(bot instanceof IUnifiedBot){
            needCloseRoom = false;
        }

        if (needCloseRoom) {
            getLogger().debug("SitOutRequest: make close room");
            Mono.delay(Duration.ofMillis(1000)).subscribe(t -> bot.sendCloseRoomRequest());
        }
    }

    private void additionalCheck() {
        if(bot instanceof UnifiedBot){
            UnifiedBot unifiedBot = (UnifiedBot) bot;
            IUnifiedBotStrategy unifiedBotStrategy = (IUnifiedBotStrategy) unifiedBot.getStrategy();
            if(unifiedBot.getRoundsCount() >= unifiedBotStrategy.getNumberRoundBeforeRestart()) {
                ((UnifiedBot) bot).setRoundsCount(0);
                bot.restart();
            }
        }
    }

    @Override
    public String toString() {
        return "SitOutRequest{" +
                "bot=" + bot +
                ", client=" + client +
                '}';
    }
}
