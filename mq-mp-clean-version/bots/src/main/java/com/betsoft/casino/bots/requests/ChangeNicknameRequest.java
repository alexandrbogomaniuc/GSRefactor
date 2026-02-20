package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.mp.transport.ChangeNickname;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class ChangeNicknameRequest extends AbstractBotRequest {

    private final ILobbyBot bot;
    private final ISocketClient client;
    private final String nickname;

    public ChangeNicknameRequest(ILobbyBot bot, ISocketClient client, String nickname) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.nickname = nickname;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new ChangeNickname(System.currentTimeMillis(), rid, nickname));
    }

    @Override
    public void handle(ITransportObject response) {
        switch (response.getClassName()) {
            case "Ok":
                //bot.pickAvatar();
                break;
            case "Error":
                int code = ((Error) response).getCode();
                if (code == ErrorCodes.ILLEGAL_NICKNAME || code == ErrorCodes.NICKNAME_NOT_AVAILABLE ||
                        code == ErrorCodes.REQUEST_FREQ_LIMIT_EXCEEDED) {
                    if (code == ErrorCodes.REQUEST_FREQ_LIMIT_EXCEEDED) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            //nop
                        }
                    }
                    bot.pickNickname(true, nickname);
                } else {
                    getLogger().error("ChangeNicknameRequest: unexpected error: {}", response);
                    bot.stop();
                }
                break;
            default:
                getLogger().error("ChangeNicknameRequest: unexpected message: {}", response);
                break;
        }
    }
}
