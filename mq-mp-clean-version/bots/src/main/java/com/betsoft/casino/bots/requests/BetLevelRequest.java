package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BetLevelRequest  extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;

    public BetLevelRequest(IRoomBot bot, ISocketClient client) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new BetLevel(System.currentTimeMillis(), rid, bot.getStrategy().requestedBetLevel()));
    }

    @Override
    public void handle(ITransportObject response) {
        bot.setState(BotState.PLAYING, "After BetLevelRequest");
        switch (response.getClassName()) {
            case "BetLevelResponse":
                BetLevelResponse betLevelResponse = (BetLevelResponse) response;
                bot.setCurrentBeLevel(betLevelResponse.getBetLevel());
                getLogger().error("BetLevelRequest: betLevelResponse " + response);
                break;
            case "Error":
                getLogger().error("BetLevelRequest: failed to purchase LootBox" + response);
                bot.count(Stats.ERRORS);
                Error errorResponse = (Error) response;
                int code = errorResponse.getCode();
                if(code == ErrorCodes.NOT_SEATER) {
                    bot.stop();
                    bot.start();
                }
                break;
            default:
                getLogger().error("BetLevelRequest: unexpected response type: {}", response.getClassName());
                break;
        }
        bot.doActionWithSleep(1000, "BetLevelRequest[" + response.getClassName() + "]");
    }
}