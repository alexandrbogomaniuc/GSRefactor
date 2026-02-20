package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.*;
import com.betsoft.casino.bots.mqb.ManagedBattleGroundRoomBot;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.FullGameInfo;
import com.betsoft.casino.mp.transport.GetFullGameInfo;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class GetFullGameInfoRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;

    public GetFullGameInfoRequest(IRoomBot bot, ISocketClient client) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new GetFullGameInfo(System.currentTimeMillis(), rid));
    }

    @Override
    public void handle(ITransportObject response) {
        switch (response.getClassName()) {
            case "FullGameInfo":
                FullGameInfo fullGameInfo = (FullGameInfo) response;
                bot.getLogger().debug("GetFullGameInfoRequest::handle: botId={}, nickname={}, fullGameInfo={}",
                        bot.getId(), bot.getNickname(), fullGameInfo);

                if(fullGameInfo.getState() == RoomState.WAIT) {
                    bot.setRoomEnemies(fullGameInfo.getRoomEnemies());
                    if (bot.isBattleBot()) {
                        ((BattleGroundRoomBot) bot).updateRoundStartTime(fullGameInfo.getDate(), fullGameInfo.getTimeToStart());
                        ((BattleGroundRoomBot) bot).setLastTimeFullGameInfo(System.currentTimeMillis());
                    }
                    if (bot.isMqbBattleBot()) {
                        ((ManagedBattleGroundRoomBot) bot).setObservers(fullGameInfo.getObservers());
                        ((ManagedBattleGroundRoomBot) bot).generateConfirmBuyInTime();
                    }
                    if (bot instanceof RoomBot) {
                        int mapId = ((FullGameInfo) response).getMapId();
                        ((RoomBot) bot).setCurrentMapId(mapId);
                        bot.getLogger().debug("update mapId from full game info: {} ", mapId);
                    }
                } else if(fullGameInfo.getState() == RoomState.PLAY) {
                    if (bot.isBattleBot()) {
                        ((BattleGroundRoomBot) bot).updateRoundEndTime(fullGameInfo.getDate(), fullGameInfo.getEndTime());
                    }
                }
                break;
            case "Error":
                getLogger().error("GetFullGameInfoRequest: " + response);
                bot.count(Stats.ERRORS);
                int code = ((Error) response).getCode();
                if (code == ErrorCodes.REQUEST_FREQ_LIMIT_EXCEEDED || code == ErrorCodes.ROOM_NOT_FOUND) {
                    if(bot instanceof ManagedBattleGroundRoomBot) {
                        ((ManagedBattleGroundRoomBot)bot).markExpiredAndStop();
                    } else {
                        bot.stop();
                    }
                }
                break;
            default:
                getLogger().error("Unexpected response type");
                break;
        }
    }

    @Override
    public String toString() {
        return "GetFullGameInfoRequest{" +
                "botId=" + (bot != null ? bot.getId() : null) +
                ", client.sessionId=" + (client != null ? client.getSessionId() : null) +
                '}';
    }
}
