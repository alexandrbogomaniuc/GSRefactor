package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

import java.net.URI;
import java.util.List;

public class SitInRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private final int ammoCount;
    private final long stake;
    private final String lang;
    private int failedCount;

    public SitInRequest(IRoomBot bot, ISocketClient client, int ammoCount, long stake, String lang, int failedCount) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.ammoCount = ammoCount;
        this.stake = stake;
        this.lang = lang;
        this.failedCount = failedCount;
    }

    public SitInRequest(IRoomBot bot, ISocketClient client, String lang, int failedCount) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.lang = lang;
        this.failedCount = failedCount;
        this.ammoCount = 0;
        this.stake = 0;
    }

    @Override
    public boolean isSingleResponse() {
        return false;
    }

    @Override
    public void send(int rid) {
        SitIn sitIn = new SitIn(System.currentTimeMillis(), rid, lang);
        sitIn.setStake(stake);
        client.sendMessage(sitIn);
    }

    @Override
    public void handle(ITransportObject response) {
        boolean needSleep = true;
        switch (response.getClassName()) {
            case "SitInResponse":
                SitInResponse sitInResponse = (SitInResponse) response;

                boolean skipNickNameCheck = false;

                if(bot instanceof ManagedBattleGroundRoomBot){
                    String wsUrl =  bot.getUrl();
                    String host = null;
                    try {
                        URI wSUri = new URI(wsUrl);
                        host = wSUri.getHost();
                    } catch ( Exception exception) {
                        getLogger().error("SitInRequest: cant get host from wsUrl: {}, {}", wsUrl, exception.getMessage());
                    }

                    if (host.endsWith("mp.local") || host.endsWith("mp.local.com") || host.endsWith(".mydomain")//hack for local/dev deploy
                            || host.endsWith(".maxquest.com")) { //hack for test env. deploy
                        skipNickNameCheck = true;
                    }
                }

                if (!skipNickNameCheck && !bot.getNickname().equals(sitInResponse.getNickname())) {
                    getLogger().debug("SitInRequest: other bot sitInResponse.getNickname: '{}', bot.nickName='{}'", sitInResponse.getNickname(), bot.getNickname());
                } else {
                    bot.setBalance(sitInResponse.getBalance());
                    bot.setServerAmmo((int) sitInResponse.getAmmoAmount());
                    bot.setSeatId(sitInResponse.getId());
                    List<Weapon> weapons = sitInResponse.getWeapons();
                    bot.getStrategy().resetWeapons();
                    for (Weapon weapon : weapons) {
                        bot.addWeapon(weapon.getId(), weapon.getShots());
                    }

                    bot.count(Stats.SELF_SIT_IN);

                    if (bot.getState() == BotState.PLAYING ) {
                        needSleep = false;
                    } else {
                        if (bot.isBattleBot()) {
                            bot.setState(BotState.WAIT_BATTLE_PLAYERS, "SitInRequest: SitInResponse");
                        } else {
                            bot.setState(BotState.PLAYING, "SitInRequest: SitInResponse");
                        }
                    }
                }
                break;

            case "FullGameInfo":
                bot.setRoomEnemies(((FullGameInfo) response).getRoomEnemies());
                needSleep = false;
                break;

            case "Error":
                getLogger().error("SitInRequest sitInResponse failed: {}", response);
                bot.count(Stats.ERRORS);
                Error errorResponse = (Error) response;
                int code = errorResponse.getCode();
                if (code == ErrorCodes.INTERNAL_ERROR &&
                        (
                            errorResponse.getMsg().startsWith("Buy in failed, try again")
                            || errorResponse.getMsg().contains("QualifyGameState")
                        )
                ) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //nop
                    }
                    failedCount++;
                    if (failedCount < 5) {
                        bot.sendSitInRequest(failedCount);
                        break;
                    } else {
                        bot.stop();
                    }
                }

                if (code == ErrorCodes.TOO_MANY_PLAYER) {
                    bot.count(Stats.TOO_MANY_PLAYERS_ERROR);
                    bot.sendCloseRoomRequest();
                    if (bot.isUsualActionBot()) {
                        bot.restart();
                    }
                }

                if (code == ErrorCodes.INVALID_SESSION || code == ErrorCodes.NOT_ALLOWED_SIT_IN_FOR_BOT) {
                    if (bot instanceof ManagedBattleGroundRoomBot) {
                        ((ManagedBattleGroundRoomBot)bot).markExpiredAndStop();
                    } else {
                        bot.stop();
                    }
                    break;
                }

                handleError(errorResponse);
                needSleep = false;
                break;
            default:
                getLogger().error("SitInRequest: unexpected response type: {}", response);
                break;
        }

        // fix for usual bots. need start action on sit in.
        if (needSleep && bot.isUsualActionBot()) {
            bot.doActionWithSleep(1000, "SitInRequest[" + response.getClassName() + "]");
        }
    }

    private void handleError(Error error) {
        bot.setState(BotState.WAITING_FOR_RESPONSE, "SitInRequest: Error=" + error.getMsg());
        bot.sendCloseRoomRequest();
    }

    @Override
    public String toString() {
        return "SitInRequest{" +
                "bot=" + bot +
                ", client=" + client +
                ", ammoCount=" + ammoCount +
                ", lang='" + lang + '\'' +
                '}';
    }
}
