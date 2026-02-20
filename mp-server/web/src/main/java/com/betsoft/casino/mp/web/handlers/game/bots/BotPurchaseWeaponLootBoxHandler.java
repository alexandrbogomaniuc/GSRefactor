package com.betsoft.casino.mp.web.handlers.game.bots;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.IWeaponLootBox;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.PurchaseWeaponLootBox;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.game.PurchaseWeaponLootBoxHandler;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketSession;

public class BotPurchaseWeaponLootBoxHandler extends PurchaseWeaponLootBoxHandler {
    private static final Logger LOG = LogManager.getLogger(BotPurchaseWeaponLootBoxHandler.class);

    public BotPurchaseWeaponLootBoxHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                           MultiNodeRoomInfoService multiNodeRoomInfoService,
                                           RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                           ServerConfigService serverConfigService, SocketService socketService,
                                           LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService,
                socketService, lobbySessionService);
    }

    @SuppressWarnings("unchecked,rawtypes")
    @Override
    public void handle(WebSocketSession session, PurchaseWeaponLootBox message, IGameSocketClient client) {
        try {
            AbstractActionGameRoom room = getActionRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                IActionGameSeat seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());
                if (seat == null || client.getAccountId() == null || seat.getAccountId() != client.getAccountId()) {
                    sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
                } else {
                    IWeaponLootBox lootBox = room.generateWeaponLootBox(seat, message.getRid(), message.getBox(), 0,
                            Money.ZERO);
                    if (lootBox != null) {
                        seat.sendMessage(lootBox);
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
