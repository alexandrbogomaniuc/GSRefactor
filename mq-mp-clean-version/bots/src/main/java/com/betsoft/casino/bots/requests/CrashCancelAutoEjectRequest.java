package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.IUnifiedBot;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class CrashCancelAutoEjectRequest extends AbstractBotRequest {
    private String betId;

    private final IUnifiedBot bot;
    private final ISocketClient client;

    public CrashCancelAutoEjectRequest(IUnifiedBot bot, ISocketClient client) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
    }

    @Override
    public void send(int rid) {

    }

    @Override
    public void handle(ITransportObject response) {

    }
}
