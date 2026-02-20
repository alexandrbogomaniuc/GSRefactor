package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.CloseRoundResults;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

@Component
public class CloseRoundResultsHandler extends AbstractRoomHandler<CloseRoundResults, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(CloseRoundResultsHandler.class);

    public CloseRoundResultsHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                    MultiNodeRoomInfoService multiNodeRoomInfoService,
                                    RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                    ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
    }

    @Override
    public void handle(WebSocketSession session, CloseRoundResults message, IGameSocketClient client) {
        if (client.getRoomId() == null || client.getSeatNumber() < 0) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
            return;
        }
        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                ISeat seat = room.getSeatByAccountId(client.getAccountId());
                if (seat == null) {
                    sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
                } else {
                    room.closeResults(seat);
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
