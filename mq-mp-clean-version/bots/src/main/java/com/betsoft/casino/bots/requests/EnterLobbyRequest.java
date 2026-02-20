package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.ILobbyBot;
import com.betsoft.casino.bots.mqb.ManagedLobbyBot;
import com.betsoft.casino.mp.transport.EnterLobby;
import com.betsoft.casino.mp.transport.EnterLobbyBattlegroundInfo;
import com.betsoft.casino.mp.transport.EnterLobbyResponse;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

import java.net.URI;

public class EnterLobbyRequest extends AbstractBotRequest {
    protected final ILobbyBot bot;
    protected final ISocketClient client;
    protected final int gameId;
    protected final int serverId;
    protected final String sessionId;

    public EnterLobbyRequest(ILobbyBot bot, ISocketClient client, int gameId, int serverId, String sessionId) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.gameId = gameId;
        this.serverId = serverId;
        this.sessionId = sessionId;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new EnterLobby(System.currentTimeMillis(), sessionId, "en", rid, serverId, "real",
                false, gameId, 0, false, false));
    }

    @Override
    public void handle(ITransportObject response) {
        switch (response.getClassName()) {
            case "EnterLobbyResponse":
                EnterLobbyResponse message = (EnterLobbyResponse) response;
                bot.setBalance(message.getBalance());
                bot.setStakesLimit(message.getStakesLimit());
                bot.setStakes(message.getStakes());

                if (bot instanceof ManagedLobbyBot) {
                    String openRoomWSUrl = ((ManagedLobbyBot)bot).getOpenRoomWSUrl();
                    String host = null;
                    try {
                        URI openRoomWSUri = new URI(openRoomWSUrl);
                        host = openRoomWSUri.getHost();
                    } catch ( Exception exception) {
                        getLogger().error("EnterLobbyRequest: cant get host from openRoomWSUrl: {}, {}", openRoomWSUrl, exception.getMessage());
                    }

                    boolean skipNickNameCheck = false;

                    if (host.endsWith("mp.local") || host.endsWith("mp.local.com") || host.endsWith(".mydomain")//hack for local/dev deploy
                            || host.endsWith(".maxquest.com")) { //hack for test env. deploy
                        skipNickNameCheck = true;
                    }
                    if(!skipNickNameCheck) {
                        String botNickname = ((ManagedLobbyBot) bot).getNickname();
                        String messageNickname = message.getNickname();
                        if (!botNickname.equals(messageNickname)) {
                            getLogger().error("EnterLobbyRequest: wrong nickname botNickname: {}, messageNickname: {}",
                                    botNickname, messageNickname);
                            bot.stop();
                            break;
                        }
                    }
                }

                if(message.isNicknameEditable()) {
                    bot.pickNickname(false, message.getNickname());
                }
                bot.setMinStake(message.getMinStake());
                bot.setMaxStake(message.getMaxStake());
                EnterLobbyBattlegroundInfo enterLobbyBattlegroundInfo = message.getBattleground();
                if(enterLobbyBattlegroundInfo != null){
                    bot.setBuyIns(enterLobbyBattlegroundInfo.getBuyIns());
                } else{
                    bot.setWeaponPrices(message.getPaytable() != null ? message.getPaytable().getWeaponPaidMultiplier() : null);
                }
                bot.sleep(500).subscribe(t ->
                        bot.sendGetStartGameUrlRequest()
                );
                break;
            case "Error":
                getLogger().error("EnterLobbyRequest: enterLobby failed: {}", response);
                handleError((Error) response);
                break;
            default:
                getLogger().error("EnterLobbyRequest: Unexpected response type={}", response.getClassName());
                break;
        }
    }

    void handleError(Error error) {
        switch (error.getCode()) {
            case ErrorCodes.INVALID_SESSION:
            case ErrorCodes.ILLEGAL_NICKNAME:
                bot.stop();
                break;
        }
    }

    @Override
    public String toString() {
        return "EnterLobbyRequest{" +
                "bot=" + bot +
                ", client=" + client +
                ", gameId=" + gameId +
                ", serverId=" + serverId +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
