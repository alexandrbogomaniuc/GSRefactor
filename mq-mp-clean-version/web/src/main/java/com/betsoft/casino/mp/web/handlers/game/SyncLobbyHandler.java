package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.Ok;
import com.betsoft.casino.mp.transport.SyncLobby;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

public class SyncLobbyHandler extends AbstractRoomHandler<SyncLobby, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(SyncLobbyHandler.class);

    public SyncLobbyHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                            MultiNodeRoomInfoService multiNodeRoomInfoService,
                            RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                            ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
    }

    @Override
    public void handle(WebSocketSession session, SyncLobby message, IGameSocketClient client) {
        if (client.getRoomId() == null || client.getSeatNumber() < 0) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
            return;
        }
        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                IActionGameSeat seat = (IActionGameSeat) room.getSeatByAccountId(client.getAccountId());
                if (seat == null) {
                    sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
                } else {
                    syncLobby(seat, room.getId());
                    seat.sendMessage(new Ok(System.currentTimeMillis(), message.getRid()), message);
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    public void syncLobby(IActionGameSeat seat, long roomId) {
        Map<Integer, Integer> weapons = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<SpecialWeaponType, IWeapon> seatWeapons = seat.getWeapons();
        if (seatWeapons != null) {
            seatWeapons.forEach((type, weapon) -> weapons.put(type.getId(), weapon.getShots()));
        }
        updatePlayerInfo(roomId, seat, weapons);
    }

    private void updatePlayerInfo(long roomId, ISeat seat, Map<Integer, Integer> weapons) {
        playerInfoService.lock(seat.getAccountId());
        getLog().debug("updatePlayerInfo HS lock: {}", seat.getAccountId());
        try {
            IActionRoomPlayerInfo roomPlayerInfo = (IActionRoomPlayerInfo) playerInfoService.get(seat.getAccountId());
            if (roomPlayerInfo != null) {
                roomPlayerInfo.setWeapons(weapons);
                playerInfoService.put(roomPlayerInfo);
            } else {
                LOG.warn("updatePlayerInfo: roomPlayerInfo not found for roomId={}, seat={}", roomId, seat);
            }
        } finally {
            playerInfoService.unlock(seat.getAccountId());
            getLog().debug("updatePlayerInfo HS unlock: {}", seat.getAccountId());
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
