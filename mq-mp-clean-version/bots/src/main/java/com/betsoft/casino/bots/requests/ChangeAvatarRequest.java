package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.mp.transport.ChangeAvatar;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class ChangeAvatarRequest extends AbstractBotRequest {
    private final ILobbyBot bot;
    private final ISocketClient client;
    private final int border;
    private final int hero;
    private final int background;

    public ChangeAvatarRequest(ILobbyBot bot, ISocketClient client, int border, int hero, int background) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.border = border;
        this.hero = hero;
        this.background = background;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new ChangeAvatar(System.currentTimeMillis(), rid, border, hero, background));
    }

    @Override
    public void handle(ITransportObject response) {
        switch (response.getClassName()) {
            case "Ok":
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
                bot.sendGetStartGameUrlRequest();
                break;
            case "Error":
                getLogger().error("ChangeAvatarRequest: unexpected error: {}", response);
                bot.stop();
                break;
            default:
                getLogger().error("ChangeAvatarRequest: unexpected message: {}", response);
                break;
        }
    }
}
