package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.MineCoordinates;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

@Component
public class MinesHandler extends AbstractRoomHandler<MineCoordinates, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(MinesHandler.class);

    public MinesHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                        MultiNodeRoomInfoService multiNodeRoomInfoService,
                        RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                        ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, MineCoordinates message, IGameSocketClient client) {
        if (client.getRoomId() == null || client.getSeatNumber() < 0) {
            getLog().warn("Not seat, client={}", client);
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
            return;
        }
        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                //playerInfoService.lock(client.getRoomId(), client.getSeatNumber());
                try {
                    ISeat seat = room.getSeat(client.getSeatNumber());
                    if (seat == null || client.getAccountId() == null || seat.getAccountId() != client.getAccountId()) {
                        getLog().warn("Not seat, seat check failed: client={}, seat={}", client, seat);
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
                    } else {
                        try {
                            long now = System.currentTimeMillis();
                            if (room instanceof AbstractActionGameRoom) {
                                //noinspection unchecked
                                ((AbstractActionGameRoom) room).placeMineToMap((IActionGameSeat) seat, message);
                            } else {
                                sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Room not support this call", message.getRid());
                            }
                            StatisticsManager.getInstance().updateRequestStatistics("MinesHandler: processShot",
                                    System.currentTimeMillis() - now, client.getSessionId() + ":" + client.getRid());
                        } catch (UnsupportedOperationException e) {
                            sendErrorMessage(client, ErrorCodes.ROUND_NOT_STARTED, "Round not started", message.getRid());
                        }
                    }
                } finally {
                    //playerInfoService.unlock(client.getRoomId(), client.getSeatNumber());
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

