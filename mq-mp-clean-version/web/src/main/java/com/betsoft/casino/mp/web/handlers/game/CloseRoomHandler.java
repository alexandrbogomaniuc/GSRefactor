package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.CloseRoom;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.socket.ConnectionClosedNotifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * User: flsh
 * Date: 03.11.17.
 */
public class CloseRoomHandler extends AbstractRoomHandler<CloseRoom, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(CloseRoomHandler.class);
    private final ConnectionClosedNotifier connectionClosedNotifier;

    public CloseRoomHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                            MultiNodeRoomInfoService multiNodeRoomInfoService,
                            RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                            ServerConfigService serverConfigService, ConnectionClosedNotifier connectionClosedNotifier) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.connectionClosedNotifier = connectionClosedNotifier;
    }

    @Override
    public void handle(WebSocketSession session, CloseRoom message, IGameSocketClient client) {
        try {
            if (client.getAccountId() == null) {
                sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Room not open", message.getRid());
            } else if (client.getSeatNumber() != -1) {
                sendErrorMessage(client, ErrorCodes.NEED_SITOUT, "Player can't close room without sitOut", message.getRid());
            } else {
                IRoom<?, ?, ?, ?, ?, ?, ?, ?> room = getRoomWithCheck(message.getRid(), message.getRoomId(), client, client.getGameType());
                if (room != null) {
                    room.processCloseRoom(client, message);
                    client.setRoomId(null);
                    //for singleSocket client websocket connection may be reused, any MQ instance can process requests
                    if (!client.isSingleConnectionClient()) {
                        closeSocketSession(session, client);
                    }
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private void closeSocketSession(WebSocketSession session, IGameSocketClient client) {
        try {
            //noinspection ReactiveStreamsUnusedPublisher, close() always return Mono.empty()
            session.close();
            connectionClosedNotifier.notify(session);
        } catch (Exception e) {
            LOG.debug("Close error, client={}", client, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
