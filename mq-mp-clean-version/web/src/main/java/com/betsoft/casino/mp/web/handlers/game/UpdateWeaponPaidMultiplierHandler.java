package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.UpdateWeaponPaidMultiplier;
import com.betsoft.casino.mp.transport.UpdateWeaponPaidMultiplierResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

@Component
public class UpdateWeaponPaidMultiplierHandler extends AbstractRoomHandler<UpdateWeaponPaidMultiplier, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(UpdateWeaponPaidMultiplierHandler.class);

    public UpdateWeaponPaidMultiplierHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                             MultiNodeRoomInfoService multiNodeRoomInfoService,
                                             RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                             ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
    }

    @Override
    public void handle(WebSocketSession session, UpdateWeaponPaidMultiplier message, IGameSocketClient client) {

        try {
            if (client.getAccountId() == null) {
                sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Room not open", message.getRid());
            } else {
                IRoom room = getRoomWithCheck(message.getRid(), message.getRoomId(), client, client.getGameType());
                if (room != null) {
                    IGameConfig config = room.getGame().getGameConfig(room.getId());
                    if (config != null) {
                        client.sendMessage(new UpdateWeaponPaidMultiplierResponse(System.currentTimeMillis(), message.getRid(),
                                config.getWeaponPrices()));
                    }
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
