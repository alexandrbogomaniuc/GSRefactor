package com.betsoft.casino.bots.requests;

import com.betsoft.casino.bots.LobbyBot;
import com.betsoft.casino.bots.Stats;
import com.betsoft.casino.mp.transport.GetStartGameUrl;
import com.betsoft.casino.mp.transport.GetStartGameUrlResponse;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.utils.ITransportObject;

import java.util.HashMap;
import java.util.Map;

public class GetStartGameUrlRequest extends AbstractBotRequest {
    private final LobbyBot bot;
    private final ISocketClient client;
    private final long stake;
    private final Long roomId;

    public GetStartGameUrlRequest(LobbyBot bot, ISocketClient client, long stake, Long roomId) {
        super(bot.getLogger());
        this.bot = bot;
        this.client = client;
        this.stake = stake;
        this.roomId = roomId;
    }

    @Override
    public void send(int rid) {
        client.sendMessage(new GetStartGameUrl(System.currentTimeMillis(), roomId, rid, stake));
    }

    @Override
    public void handle(ITransportObject response) {
        switch (response.getClassName()) {
            case "GetStartGameUrlResponse":
                Map<String, String> params = extractParams(((GetStartGameUrlResponse) response).getStartGameUrl());
                String socketUrl = params.get("WEB_SOCKET_URL");
                if (bot.isWssUrl() && socketUrl.startsWith("ws:")) {
                    getLogger().error("GetStartGameUrlRequest: lobby bot and room bot protocol mismath, " +
                                    "lobbyUrl={} roomURL={}", bot.getUrl(), socketUrl);
                    socketUrl = socketUrl.replaceFirst("ws:", "wss:");
                }
                bot.connectToRoom(socketUrl, Long.parseLong(params.get("roomId")),
                        Integer.parseInt(params.get("serverId")), params.get("SID"), false);
                break;
            case "Error":
                bot.count(Stats.ERRORS);
                getLogger().error("GetStartGameUrlRequest: error, try again");
                bot.sendGetStartGameUrlRequest();
                // TODO: handle error
                break;
            default:
                getLogger().error("GetStartGameUrlRequest: Unexpected response type");
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
}
