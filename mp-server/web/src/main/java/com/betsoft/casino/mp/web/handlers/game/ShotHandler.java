package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.ServiceNotStartedException;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.Shot;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * User: flsh
 * Date: 03.11.17.
 */
public class ShotHandler extends AbstractRoomHandler<Shot, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(ShotHandler.class);

    public ShotHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                       MultiNodeRoomInfoService multiNodeRoomInfoService,
                       RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                       ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, Shot message, IGameSocketClient client) {
        if (client.getRoomId() == null || client.getSeatNumber() < 0) {
            getLog().warn("Not seat, client={}", client);
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
            return;
        }
        try {
            AbstractActionGameRoom room = getActionRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                //playerInfoService.lock(client.getRoomId(), client.getSeatNumber());
                try {
                    IActionGameSeat seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());
                    if (seat == null || client.getAccountId() == null || seat.getAccountId() != client.getAccountId()) {
                        getLog().warn("Not seat, seat check failed: client={}, seat={}", client, seat);
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
                    } else {
                        try {
                            long now = System.currentTimeMillis();
                            //noinspection unchecked
                            room.processShot(seat, message, false);
                            StatisticsManager.getInstance().updateRequestStatistics("ShotHandler: processShot",
                                    System.currentTimeMillis() - now, client.getSessionId() + ":" + message.getRid());
                        } catch (UnsupportedOperationException e) {
                            sendErrorMessage(client, ErrorCodes.ROUND_NOT_STARTED, "Round not started", message.getRid());
                        }
                    }
                } finally {
                    //playerInfoService.unlock(client.getRoomId(), client.getSeatNumber());
                }
            }
        } catch (ServiceNotStartedException e) {
            processRebootError(client, message, e);
        }
        catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
