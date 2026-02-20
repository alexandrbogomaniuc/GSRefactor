package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BattleGroundRoomBot;
import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

import static com.betsoft.casino.bots.BotState.OBSERVING;

public class ConfirmBattlegroundBuyInRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;

    public ConfirmBattlegroundBuyInRequest(IRoomBot bot, ISocketClient client) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
    }

    @Override
    public boolean isSingleResponse() {
        return false;
    }

    @Override
    public void send(int rid) {
        ConfirmBattlegroundBuyIn confirmBattlegroundBuyIn = new ConfirmBattlegroundBuyIn(System.currentTimeMillis(), rid);
        client.sendMessage(confirmBattlegroundBuyIn);
    }

    @Override
    public void handle(ITransportObject response) {
        boolean needSleep = true;
        BattleGroundRoomBot battleGroundRoomBot = (BattleGroundRoomBot) bot;
        battleGroundRoomBot.setBattlegroundBuyInConfirmed(false);
        switch (response.getClassName()) {
            case "Ok":
                battleGroundRoomBot.setBattlegroundBuyInConfirmed(true);
                bot.setState(OBSERVING,"ConfirmBattlegroundBuyInRequest handle");
                break;
            case "Error":

                Error errorResponse = (Error) response;
                int code = errorResponse.getCode();

                if(!battleGroundRoomBot.isMqbBattleBot() && code == ErrorCodes.FOUND_PENDING_OPERATION){

                    getLogger().debug("ConfirmBattlegroundBuyInRequest failed, try send CheckPendingOperationStatusRequest: {}", response);
                    bot.sentCheckPendingStatus(0);

                } else {

                    getLogger().error("ConfirmBattlegroundBuyInRequest failed: {}", response);

                    if (code == ErrorCodes.INVALID_SESSION) {
                        if (bot instanceof ManagedBattleGroundRoomBot) {
                            ((ManagedBattleGroundRoomBot) bot).markExpiredAndStop();
                        }
                    }
                }
                break;
            default:
                getLogger().error("ConfirmBattlegroundBuyInRequest: unexpected response type: {}", response);
                break;
        }
        if (needSleep) {
            bot.doActionWithSleep(1000, "ConfirmBattlegroundBuyInRequest[" + response.getClassName() + "]");
        }
    }

    private void handleError(Error error) {
        bot.setState(BotState.WAITING_FOR_RESPONSE, "ConfirmBattlegroundBuyInRequest: Error=" + error.getMsg());
        bot.sendCloseRoomRequest();
    }

    @Override
    public String toString() {
        return "ConfirmBattlegroundBuyInRequest{" +
                "bot=" + bot +
                ", client=" + client +
                '}';
    }
}
