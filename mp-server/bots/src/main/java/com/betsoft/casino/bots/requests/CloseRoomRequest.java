package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.mp.transport.CloseRoom;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class CloseRoomRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private final long roomId;

    public CloseRoomRequest(IRoomBot bot, ISocketClient client, long roomId) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.roomId = roomId;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new CloseRoom(System.currentTimeMillis(), roomId, rid));
    }

    @Override
    public void handle(ITransportObject response) {
        boolean restartBot = false;
        switch (response.getClassName()) {
            case "Ok":
                if (!(bot instanceof ManagedBattleGroundRoomBot)) {
                    if (bot.isBattleBot()) {
                        restartBot = true;
                    } else {
                        bot.openNewRoom();
                    }
                }
                break;
            case "Error":
                bot.count(Stats.ERRORS);
                // TODO: handle error
                restartBot = true;
                break;
            default:
                getLogger().error("CloseRoomRequest: Unexpected response type={}", response);
                restartBot = true;
                break;
        }
        if (restartBot) {
            getLogger().debug("CloseRoomRequest: {} bot will be restarted", bot.getNickname());
            bot.restart();
        }
    }
}
