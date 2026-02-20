package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.BattleGroundRoomBot;
import com.betsoft.casino.bots.BotState;
import com.betsoft.casino.bots.IRoomBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedMaxBlastChampionsRoomBot;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

public class OpenRoomRequest extends AbstractBotRequest {
    private final IRoomBot bot;
    private final ISocketClient client;
    private final String sessionId;
    private final int serverId;
    private final MoneyType mode;
    private final String lang;
    private final long roomId;

    public OpenRoomRequest(IRoomBot bot, ISocketClient client, long roomId, String sessionId, int serverId,
                           MoneyType mode, String lang) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.sessionId = sessionId;
        this.serverId = serverId;
        this.mode = mode;
        this.lang = lang;
        this.roomId = roomId;
    }

    @Override
    public boolean isSingleResponse() {
        return false;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new OpenRoom(System.currentTimeMillis(),
                roomId, rid, sessionId, serverId, mode.toString().toLowerCase(), lang));
    }

    @Override
    public void handle(ITransportObject response) {
        boolean doNextAction = true;
        switch (response.getClassName()) {
            case "GetBalanceResponse":
                bot.setBalance(((GetBalanceResponse) response).getBalance());
                doNextAction = false;
                break;
            case "GetRoomInfoResponse":
                GetRoomInfoResponse roomInfoResponse = (GetRoomInfoResponse) response;
                RoomState state = roomInfoResponse.getState();
                if (state == RoomState.CLOSED) {
                    bot.setState(BotState.IDLE, "OpenRoomRequest: Error");
                    bot.restart();
                } else {
                    bot.setRoomInfo(roomInfoResponse);
                    bot.setState(BotState.OBSERVING, "OpenRoomRequest: GetRoomInfoResponse");
                    if(bot.isBattleBot()){
                        getLogger().debug("bot: {}, set openRoomAppeared true", bot.getId());
                        BattleGroundRoomBot battleGroundRoomBot = (BattleGroundRoomBot) bot;
                        battleGroundRoomBot.setBattlegroundBuyInConfirmed(false);
                        battleGroundRoomBot.setOpenRoomAppeared(true);
                    }
                }
                break;
            case "CrashGameInfo":
                CrashGameInfo crashGameInfo = (CrashGameInfo) response;
                getLogger().debug("handle: CrashGameInfo={}", crashGameInfo);
                RoomState roomState = crashGameInfo.getState();
                if (roomState == RoomState.CLOSED) {
                    bot.setState(BotState.IDLE, "OpenRoomRequest: Error");
                    bot.restart();
                    doNextAction = false;
                } else {
                    //bot.sitIn(0);

                    if(bot instanceof ManagedMaxBlastChampionsRoomBot) {
                        ManagedMaxBlastChampionsRoomBot maxBlastBot = (ManagedMaxBlastChampionsRoomBot) bot;

                        maxBlastBot.addPlayer(maxBlastBot.getNickname());
                        //getLogger().debug("OpenRoomRequest: botId={}, nickname={} added to players list:{}",
                        //        maxBlastBot.getId(), maxBlastBot.getNickname(), maxBlastBot.getPlayers().keySet().toArray());

                        //add existing players to the list
                        for (Seat seat : crashGameInfo.getSeats()) {
                            maxBlastBot.addPlayer(seat.getNickname());
                            //getLogger().debug("OpenRoomRequest: botId={}, nickname={} added player {} to players list",
                            //        maxBlastBot.getId(), maxBlastBot.getNickname(), seat.getNickname());
                        }

                        getLogger().debug("OpenRoomRequest: botId={}, nickname={} has players: {}",
                                maxBlastBot.getId(), maxBlastBot.getNickname(), maxBlastBot.getPlayers().keySet().toArray());

                        long msgRoundStartTime = crashGameInfo.getDate() + crashGameInfo.getTtnx();
                        maxBlastBot.calcCrashBetRequestTime(crashGameInfo.getDate(), msgRoundStartTime);
                    }

                    bot.setRoomInfo(crashGameInfo);
                    bot.setState(BotState.OBSERVING, "OpenRoomRequest: CrashGameInfo");
                }
                break;
            case "Error":
                getLogger().error("OpenRoomRequest: failed to open room: {}", response);
                bot.count(Stats.ERRORS);
                doNextAction = false;
                com.betsoft.casino.mp.transport.Error errorResponse = (Error) response;
                int code = errorResponse.getCode();
                if (bot.isMqbBattleBot()) {
                    getLogger().debug("OpenRoomRequest error stop mqb bot, {} ", bot.getId());
                    bot.stop();
                } else {
                    if (code == ErrorCodes.TOO_MANY_OBSERVERS) {
                        bot.openNewRoom();
                    } else {
                        bot.setState(BotState.IDLE, "OpenRoomRequest: Error");
                        bot.restart();
                    }
                }
                break;
            default:
                getLogger().error("OpenRoomRequest: Unexpected response type");
                break;
        }
        if(doNextAction) {
            long weightTime = bot.isBattleBot() ? 0 : 1000;
            bot.doActionWithSleep(weightTime, "OpenRoomRequest[" + response.getClassName() + "]");
        }
    }

    @Override
    public String toString() {
        return "OpenRoomRequest{" +
                "bot=" + bot +
                ", client=" + client +
                ", sessionId='" + sessionId + '\'' +
                ", serverId=" + serverId +
                ", mode=" + mode +
                ", lang='" + lang + '\'' +
                ", roomId=" + roomId +
                '}';
    }
}
