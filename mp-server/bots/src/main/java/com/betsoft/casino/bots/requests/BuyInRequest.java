package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.transport.BuyIn;
import com.betsoft.casino.mp.transport.BuyInResponse;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.utils.ITransportObject;

public class BuyInRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private final int ammoAmount;
    private int failedCount;

    public BuyInRequest(IRoomBot bot, ISocketClient client, int ammoAmount, int failedCount) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.ammoAmount = ammoAmount;
        this.failedCount = failedCount;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new BuyIn(System.currentTimeMillis(), rid, ammoAmount));
    }

    @Override
    public void handle(ITransportObject response) {
        boolean needSleep = true;
        switch (response.getClassName()) {
            case "BuyInResponse":
                BuyInResponse buyInResponse = (BuyInResponse) response;
                long balance = buyInResponse.getBalance();
                bot.setBalance(balance);
                if(balance >= 0) {
                    if(buyInResponse.getAmmoAmount() > 0) {
                        bot.addServerAmmo((int) buyInResponse.getAmmoAmount());
                    }
                }
                if(bot.getState() == BotState.WAITING_FOR_RESPONSE) {
                    bot.setState(BotState.PLAYING, "BuyInRequest: BuyInResponse");
                }
                bot.count(Stats.BUY_INS);
                break;
            case "Error":
                bot.count(Stats.ERRORS);
                Error errorResponse = (Error) response;
                int code = errorResponse.getCode();
                if(code == ErrorCodes.INTERNAL_ERROR && errorResponse.getMsg().startsWith("Buy in failed, try again")) {
                    failedCount++;
                    needSleep = false;
                    if(failedCount < 5) {
                        bot.sendBuyInRequest(failedCount);
                        break;
                    } else {
                        bot.stop();
                    }
                    bot.setState(BotState.PLAYING, "BuyInRequest: Error");
                } else if(code == ErrorCodes.BUYIN_NOT_ALLOWED || code == ErrorCodes.ROUND_NOT_STARTED
                 || code == ErrorCodes.NOT_SEATER){
                    bot.setState(BotState.WAITING_FOR_RESPONSE, "BuyInRequest: Sit Out");
                    needSleep = false;
                    bot.sendSitOutRequest();
                }

                // TODO: handle error
                break;
            default:
                getLogger().error("BuyInRequest: unexpected response type: {}", response.getClassName());
                break;
        }
        if(needSleep) {
            bot.doActionWithSleep(2000, "BuyInRequest[" + response.getClassName() + "]");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BuyInRequest [");
        sb.append("bot=").append(bot);
        sb.append(", client=").append(client);
        sb.append(", ammoAmount=").append(ammoAmount);
        sb.append(']');
        return sb.toString();
    }
}
