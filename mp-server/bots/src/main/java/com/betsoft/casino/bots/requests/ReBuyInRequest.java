package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BattleGroundRoomBot;
import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class ReBuyInRequest  extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private int failedCount;

    public ReBuyInRequest(IRoomBot bot, ISocketClient client, int failedCount) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.failedCount = failedCount;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new ReBuy(System.currentTimeMillis(), rid));
    }

    @Override
    public void handle(ITransportObject response) {
        boolean needSleep = true;
        switch (response.getClassName()) {
            case "ReBuyResponse":
                ReBuyResponse buyInResponse = (ReBuyResponse) response;
                long balance = buyInResponse.getBalance();
                bot.setBalance(balance);
                if(balance >= 0) {
                    if(buyInResponse.getAmmoAmount() > 0) {
                        if (bot.isBattleBot()) {
                            bot.setServerAmmo((int) buyInResponse.getAmmoAmount());
                        } else {
                            bot.addServerAmmo((int) buyInResponse.getAmmoAmount());
                        }
                    }
                }

                bot.count(Stats.RE_BUY_INS);

                if (bot.isBattleBot()) {
                    if (bot.getState() == BotState.WAITING_FOR_RESPONSE) {
                        ((BattleGroundRoomBot) bot).setBattlegroundBuyInConfirmed(true);
                        bot.setState(BotState.WAIT_BATTLE_PLAYERS, "Battle ReBuyInRequest: ReBuyInResponse");
                    }
                    BattleGroundRoomBot battleGroundRoomBot = (BattleGroundRoomBot) bot;
                    battleGroundRoomBot.setNeedReBuyInRoom(false);
                } else {
                    if (bot.getState() == BotState.WAITING_FOR_RESPONSE) {
                        bot.setState(BotState.PLAYING, "ReBuyInRequest: ReBuyInResponse");
                    }
                }

                break;
            case "Error":
                bot.count(Stats.ERRORS);
                Error errorResponse = (Error) response;
                int code = errorResponse.getCode();
                if(code == ErrorCodes.INTERNAL_ERROR && errorResponse.getMsg().startsWith("ReBuy in failed, try again")) {
                    failedCount++;
                    needSleep = false;
                    if(failedCount < 5) {
                        bot.sendReBuyInRequest(failedCount);
                        break;
                    } else {
                        bot.stop();
                    }

                    if(bot.isBattleBot()){
                        if (bot.getState() == BotState.WAITING_FOR_RESPONSE) {
                            bot.setState(BotState.OBSERVING, "ReBuyInRequest: ReBuyInResponse");
                        }
                    }else {
                        bot.setState(BotState.PLAYING, "ReBuyInRequest: Error");
                    }
                } else if(code == ErrorCodes.BUYIN_NOT_ALLOWED || code == ErrorCodes.ROUND_NOT_STARTED
                        || code == ErrorCodes.NOT_SEATER){
                    bot.setState(BotState.WAITING_FOR_RESPONSE, "ReBuyInRequest: Sit Out");
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
            bot.doActionWithSleep(300, "RebBuyInRequest[" + response.getClassName() + "]");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RebBuyInRequest [");
        sb.append("bot=").append(bot);
        sb.append(", client=").append(client);
        sb.append(']');
        return sb.toString();
    }
}
