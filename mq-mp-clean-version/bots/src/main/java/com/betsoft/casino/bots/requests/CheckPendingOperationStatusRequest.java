package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.mp.transport.CheckPendingOperationStatus;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.PendingOperationStatus;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

import java.util.StringJoiner;

import static com.betsoft.casino.bots.BotState.OBSERVING;

public class CheckPendingOperationStatusRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private int failedCount;

    public CheckPendingOperationStatusRequest(IRoomBot bot, ISocketClient client, int failedCount) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.failedCount = failedCount;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new CheckPendingOperationStatus(System.currentTimeMillis(), rid, bot.getId()));
    }

    @Override
    public void handle(ITransportObject response) {
        switch (response.getClassName()) {
            case "PendingOperationStatus":
                PendingOperationStatus pendingOperationStatus = (PendingOperationStatus) response;
                if (pendingOperationStatus.getPending()) {
                    handeErrorResponse();
                } else {
                    bot.setState(OBSERVING, "CheckPendingOperationStatusRequest handle");
                }
                break;
            case "Error":
                handeErrorResponse();
                break;
            default:
                getLogger().error("ConfirmBattlegroundBuyInRequest: unexpected response type: {}", response);
                break;
        }
    }

    private void handeErrorResponse() {
        failedCount++;
        if (failedCount < 5) {
            bot.sentCheckPendingStatus(failedCount);
        } else {
            if(!bot.isMqbBattleBot()){
                bot.setState(BotState.IDLE, "CheckPendingOperationStatusRequest failedCount more 5 times, bot will be stop");
                bot.stop();
                if(bot instanceof RoomBot) {
                    ((RoomBot) bot).getLobbyBot().stop();
                }
            }
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CheckPendingOperationStatusRequest.class.getSimpleName() + "[", "]")
                .add("bot=" + bot)
                .add("client=" + client)
                .add("failedCount=" + failedCount)
                .toString();
    }
}
