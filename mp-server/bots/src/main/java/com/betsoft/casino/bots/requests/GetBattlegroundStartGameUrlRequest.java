package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.LobbyBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.bots.mqb.ManagedLobbyBot;
import com.betsoft.casino.mp.transport.GetBattlegroundStartGameUrl;
import com.betsoft.casino.mp.transport.GetStartGameUrlResponse;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class GetBattlegroundStartGameUrlRequest extends AbstractBotRequest {
    private final LobbyBot bot;
    private final ISocketClient client;
    private final long buyIn;
    private final Long roomId;

    public GetBattlegroundStartGameUrlRequest(LobbyBot bot, ISocketClient client, long buyIn, Long roomId) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.buyIn = buyIn;
        this.roomId = roomId;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new GetBattlegroundStartGameUrl(System.currentTimeMillis(), rid, buyIn, roomId));
    }

    @Override
    public void handle(ITransportObject response) {
        switch (response.getClassName()) {
            case "GetStartGameUrlResponse":
                Map<String, String> params = extractParams(((GetStartGameUrlResponse) response).getStartGameUrl());

                String socketUrl = params.get("WEB_SOCKET_URL");
                int serverId = Integer.parseInt(params.get("serverId"));
                long responseRoomId = Long.parseLong(params.get("roomId"));
                String sessionId = params.get("SID");

                getLogger().debug("GetBattlegroundStartGameUrlRequest: socketUrl={}, serverId={}, responseRoomId={}, sessionId={} ",
                        socketUrl, serverId, responseRoomId, sessionId);

                if (bot.isWssUrl() && socketUrl.startsWith("ws:")) {
                    getLogger().error("GetBattlegroundStartGameUrlRequest: lobby bot and room bot protocol mismath, " +
                            "lobbyUrl={} roomURL={}", bot.getUrl(), socketUrl);
                    socketUrl = socketUrl.replaceFirst("ws:", "wss:");
                }

                boolean isMqbBot = bot instanceof ManagedLobbyBot;
                if (isMqbBot && roomId != responseRoomId) {
                    getLogger().debug("Need stop mqb bot, mismatch roomIds, requested roomId: {}, real responseRoomId: {} ",
                            roomId, responseRoomId);
                    bot.stop();

                } else {
                    bot.connectToRoom(socketUrl, responseRoomId, serverId, sessionId, true);
                }

                break;
            case "Error":
                bot.count(Stats.ERRORS);
                getLogger().error("GetBattlegroundStartGameUrlRequest: error, try again");
                bot.sendGetStartGameUrlRequest();
                // TODO: handle error
                break;
            default:
                getLogger().error("GetBattlegroundStartGameUrlRequest: Unexpected response type");
                break;
        }
    }

    private Map<String, String> extractParams(String url) {
        Map<String, String> result = new HashMap<>();
        String[] params = url.substring(url.indexOf("?") + 1).split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GetBattlegroundStartGameUrlRequest.class.getSimpleName() + "[", "]")
                .add("bot=" + bot)
                .add("client=" + client)
                .add("buyIn=" + buyIn)
                .add("roomId=" + roomId)
                .toString();
    }
}

