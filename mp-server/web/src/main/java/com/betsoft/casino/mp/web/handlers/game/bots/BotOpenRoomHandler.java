package com.betsoft.casino.mp.web.handlers.game.bots;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.MaxQuestWeaponMode;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.Avatar;
import com.betsoft.casino.mp.transport.Currency;
import com.betsoft.casino.mp.transport.GetBalanceResponse;
import com.betsoft.casino.mp.transport.OpenRoom;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.game.AbstractRoomHandler;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.socket.BotGameClient;
import com.betsoft.casino.utils.ITransportObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.RNG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Collections;

public class BotOpenRoomHandler extends AbstractRoomHandler<OpenRoom, BotGameClient> {
    private static final Logger LOG = LogManager.getLogger(BotOpenRoomHandler.class);

    private static final long BOT_BALANCE = 100000000L;
    private final LobbySessionService lobbySessionService;
    private final NicknameGenerator nicknameGenerator;

    public BotOpenRoomHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                              MultiNodeRoomInfoService multiNodeRoomInfoService,
                              RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                              LobbySessionService lobbySessionService, NicknameGenerator nicknameGenerator,
                              ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
        this.nicknameGenerator = nicknameGenerator;
    }

    @Override
    public void handle(WebSocketSession session, OpenRoom message, BotGameClient client) {
        try {
            IRoom room = getRoomWithCheck(message.getRid(), message.getRoomId(), client, client.getGameType());
            if (room != null) {
                client.setSessionId(message.getSid());
                client.setAccountId(Long.parseLong(message.getSid().split("-")[2]));
                createBotLobbySession(message, client);
                openRoom(client, room, message);
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private void createBotLobbySession(OpenRoom message, BotGameClient client) {
        lobbySessionService.add(new LobbySession(
                message.getSid(),
                Long.parseLong(message.getSid().split("-")[2]),
                271L,
                nicknameGenerator.generate(),
                getRandomAvatar(),
                System.currentTimeMillis(),
                BOT_BALANCE,
                message.getRoomId(),
                client,
                new Currency("USD", "$"),
                true,
                Collections.singletonList(1L),
                IRoom.DEFAULT_STAKES_RESERVE,
                IRoom.DEFAULT_STAKES_LIMIT,
                0.0,
                client.getGameType().getGameId(),
                MaxQuestWeaponMode.LOOT_BOX,
                false,
                MoneyType.REAL, null, null, null));
    }

    private Avatar getRandomAvatar() {
        return new Avatar(RNG.nextInt(2), RNG.nextInt(2), RNG.nextInt(2));
    }

    @SuppressWarnings("unchecked")
    private void openRoom(BotGameClient client, IRoom room, OpenRoom message) throws CommonException {
        client.sendMessage(new GetBalanceResponse(System.currentTimeMillis(), message.getRid(), BOT_BALANCE));
        String currency = getRoomInfoService(client).getRoom(message.getRoomId()).getCurrency();
        ITransportObject result;
        try {
            result = room.processOpenRoom(client, message, currency);
        } catch (com.abs.casino.common.exception.CommonException e) {
            throw new CommonException(e.getMessage(), e);
        }
        client.setEnterDate(System.currentTimeMillis());
        client.setRoomId(message.getRoomId());
        client.sendMessage(result, message);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
