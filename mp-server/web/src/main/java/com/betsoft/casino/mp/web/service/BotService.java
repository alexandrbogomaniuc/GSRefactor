package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.bots.LocalRoomBot;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.room.IMultiNodeRoomInfo;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.socket.BotGameClient;
import com.betsoft.casino.mp.web.socket.BotGameWebSocketHandler;
import com.dgphoenix.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

public class BotService {
    private static final Logger LOG = LogManager.getLogger(BotService.class);

    private final SingleNodeRoomInfoService singleNodeRoomInfoService;
    private final ServerConfigService serverConfigService;
    private final IMessageSerializer serializer;
    private final BotGameWebSocketHandler handler;

    public BotService(SingleNodeRoomInfoService singleNodeRoomInfoService, ServerConfigService serverConfigService,
                      IMessageSerializer serializer, BotGameWebSocketHandler handler) {
        this.singleNodeRoomInfoService = singleNodeRoomInfoService;
        this.serverConfigService = serverConfigService;
        this.serializer = serializer;
        this.handler = handler;
    }

    private Map<String, LocalRoomBot> bots = new HashMap<>();

    @PostConstruct
    private void init() {
        long accountId = RNG.nextInt(1000) * 1000;
        int serverId = serverConfigService.getServerId();
        for (IRoomInfo roomInfo : this.singleNodeRoomInfoService.getAllRooms()) {
            if (roomInfo instanceof IMultiNodeRoomInfo || ((ISingleNodeRoomInfo) roomInfo).getGameServerId() == serverId) {
                String botSessionId = "Bot-" + roomInfo.getId() + "-" + accountId++;

                LocalRoomBot bot = new LocalRoomBot(null, botSessionId, serverId, botSessionId,
                        roomInfo.getId(), roomInfo.getStake().getValue(), serializer);
                bot.setClient(new BotGameClient(handler, bot));
                bots.put(botSessionId, bot);
                bot.start();
                LOG.info("{} started", botSessionId);
            }
        }
    }

}
