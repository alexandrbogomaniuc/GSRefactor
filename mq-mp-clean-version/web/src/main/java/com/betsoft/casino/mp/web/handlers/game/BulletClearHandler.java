package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.BulletClear;
import com.betsoft.casino.mp.transport.BulletClearResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Set;

import static com.betsoft.casino.utils.TObject.SERVER_RID;

@Component
public class BulletClearHandler extends AbstractRoomHandler<BulletClear, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(BulletClearHandler.class);
    private final SocketService socketService;
    protected final LobbySessionService lobbySessionService;

    public BulletClearHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                              MultiNodeRoomInfoService multiNodeRoomInfoService,
                              RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                              SocketService socketService,
                              ServerConfigService serverConfigService,
                              LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, BulletClear message, IGameSocketClient client) {
        Long accountId = client.getAccountId();
        if (client.getRoomId() == null || client.getSeatNumber() < 0 || accountId == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }

        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                if (hasPendingOperations(accountId, client, message)) {
                    return;
                }
                playerInfoService.lock(accountId);
                getLog().debug("handle HS lock: {}", accountId);
                try {
                    IActionGameSeat seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());
                    if (seat == null || seat.getAccountId() != accountId) {
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                    } else {
                        short maxBulletsOnMap = room.getRoomInfo().getGameType().getMaxBulletsOnMap();

                        if (maxBulletsOnMap == 0) {
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_PLACE_BULLET,
                                    "Not allowed any action with bullets on map", message.getRid(), message);
                        }

                        Set bulletsOnMap = seat.getBulletsOnMap();
                        LOG.debug("bullets  {} will be cleared for accountId: {}",
                                bulletsOnMap, seat.getAccountId());
                        bulletsOnMap.clear();
                        room.sendChanges(new BulletClearResponse(System.currentTimeMillis(),
                                        SERVER_RID, seat.getNumber()),
                                new BulletClearResponse(System.currentTimeMillis(),
                                        message.getRid(), seat.getNumber()), accountId, message);
                    }
                } finally {
                    playerInfoService.unlock(accountId);
                    getLog().debug("handle HS unlock: {}", accountId);
                }
            }

        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @SuppressWarnings("rawtypes")
    public void clearBullets(IGameSocketClient client) {
        Long accountId = client.getAccountId();
        if (client.getRoomId() == null || client.getSeatNumber() < 0 || accountId == null) {
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            return;
        }

        try {
            IRoom room = getRoomWithCheck(SERVER_RID, client.getRoomId(), client, client.getGameType());
            if (room != null && room.getRoomInfo().getGameType().getMaxBulletsOnMap() > 0) {
                playerInfoService.lock(accountId);
                getLog().debug("clearBullets HS lock: {}", accountId);
                try {
                    IActionGameSeat seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());
                    if (seat != null && seat.getAccountId() == accountId) {
                        Set bulletsOnMap = seat.getBulletsOnMap();
                        LOG.debug("bullets {} will be cleared for accountId: {}",
                                bulletsOnMap, seat.getAccountId());
                        bulletsOnMap.clear();
                        room.sendChanges(new BulletClearResponse(System.currentTimeMillis(), SERVER_RID, seat.getNumber()));
                    }
                } finally {
                    playerInfoService.unlock(accountId);
                    getLog().debug("clearBullets HS unlock: {}", accountId);
                }
            }

        } catch (Exception e) {
            getLog().error("Unexpected error, client={}", client, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}

