package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.GetFullGameInfo;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.utils.ITransportObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * User: flsh
 * Date: 03.11.17.
 */
@Component
public class GetFullGameInfoHandler extends AbstractRoomHandler<GetFullGameInfo, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetFullGameInfoHandler.class);

    public GetFullGameInfoHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                  MultiNodeRoomInfoService multiNodeRoomInfoService,
                                  RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                  ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
    }

    @Override
    public void handle(WebSocketSession session, GetFullGameInfo message, IGameSocketClient client) {

        IRoom room = null;

        try {
            room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
            return;
        }

        if (room != null) {

            String uuid = lockSendGameInfo(room);
            try {
                ITransportObject fullGameInfo = room.getFullGameInfo(message, client);
                client.sendMessage(fullGameInfo, message);
            } catch (Exception e) {
                processUnexpectedError(client, message, e);
            } finally {
                unlockSendGameInfo(room, uuid);
            }
        }
    }

    private String lockSendGameInfo(IRoom room) {
        if(room instanceof BattleAbstractCrashGameRoom) {
            return ((BattleAbstractCrashGameRoom)room).lockSendGameInfo();
        }
        return null;
    }

    private void unlockSendGameInfo(IRoom room, String uuid) {
        if (room instanceof BattleAbstractCrashGameRoom) {
            ((BattleAbstractCrashGameRoom) room).unlockSendGameInfo(uuid);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
