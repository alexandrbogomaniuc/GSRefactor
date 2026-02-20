package com.betsoft.casino.bots.handlers;

import com.betsoft.casino.bots.RoomBot;
import com.betsoft.casino.bots.requests.BulletRequest;
import com.betsoft.casino.bots.requests.IBotRequest;
import com.betsoft.casino.mp.transport.BulletClearResponse;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BulletClearResponseHandler implements IServerMessageHandler<BulletClearResponse> {

    private final RoomBot bot;

    public BulletClearResponseHandler(RoomBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(BulletClearResponse response) {
        Set<Integer> botRequestsKeys = new HashSet<>();
        Map<Integer, IBotRequest> botRequests = bot.getRequests();

        for (Map.Entry<Integer, IBotRequest> requestEntry : botRequests.entrySet()) {
            if(requestEntry.getValue() instanceof BulletRequest){
                botRequestsKeys.add(requestEntry.getKey());
            }
        }
        botRequests.keySet().removeAll(botRequestsKeys);
    }
}
