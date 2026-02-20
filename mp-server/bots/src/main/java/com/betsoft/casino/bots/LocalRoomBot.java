package com.betsoft.casino.bots;

import com.betsoft.casino.bots.strategies.BurstShootingStrategy;
import com.betsoft.casino.mp.web.IMessageSerializer;

public class LocalRoomBot extends RoomBot {

    public LocalRoomBot(LobbyBot lobbyBot, String id, int serverId, String sessionId, long roomId, float stake,
                        IMessageSerializer serializer) {
        super(lobbyBot, id, null, serverId, sessionId, serializer, roomId, 0, stake, id, new BurstShootingStrategy(100), null, null);

        stats = new Stats();
    }

    public void setClient(ISender sender) {
        this.client = new ProxyClient(sender);
    }

    @Override
    public void start() {
        doAction("from start");
    }

    @Override
    public void stop() {
        LOG.info("Bot " + id + " is shutting down, current stats: " + stats);
    }

    @Override
    public String toString() {
        return "LocalRoomBot{id=" + id + '}';
    }

    @Override
    public double getRoomStake() {
        return 0;
    }
}
