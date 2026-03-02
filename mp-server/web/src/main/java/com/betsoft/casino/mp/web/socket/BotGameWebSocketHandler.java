package com.betsoft.casino.mp.web.socket;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.OpenRoom;
import com.betsoft.casino.mp.transport.PurchaseWeaponLootBox;
import com.betsoft.casino.mp.transport.Shot;
import com.betsoft.casino.mp.transport.SitIn;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.game.ShotHandler;
import com.betsoft.casino.mp.web.handlers.game.bots.BotOpenRoomHandler;
import com.betsoft.casino.mp.web.handlers.game.bots.BotPurchaseWeaponLootBoxHandler;
import com.betsoft.casino.mp.web.handlers.game.bots.BotSitInHandler;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.betsoft.casino.utils.TObject;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

public class BotGameWebSocketHandler extends AbstractWebSocketHandler<BotGameClient> {
    private static final Logger LOG = LogManager.getLogger(BotGameWebSocketHandler.class);
    protected final IMessageSerializer serializer;

    public BotGameWebSocketHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                   MultiNodeRoomInfoService multiNodeRoomInfoService,
                                   RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                   LobbySessionService lobbySessionService, NicknameGenerator nicknameGenerator,
                                   ServerConfigService serverConfigService, CassandraPersistenceManager cpm,
                                   SocketService socketService, CurrencyRateService currencyRateService,
                                   CrashGameSettingsService crashGameSettingsService, BGPrivateRoomInfoService bgPrivateRoomInfoService,
                                   MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService,
                                   BotConfigInfoService botConfigInfoService, PendingOperationService pendingOperationService) {
        this.serializer = serializer;

        BotOpenRoomHandler botOpenRoomHandler = new BotOpenRoomHandler(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService,
                playerInfoService, roomServiceFactory, lobbySessionService, nicknameGenerator, serverConfigService);
        register(OpenRoom.class, botOpenRoomHandler);

        BotSitInHandler botSitInHandler = new BotSitInHandler(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService,
                roomServiceFactory, lobbySessionService, serverConfigService, cpm, currencyRateService, crashGameSettingsService,
                bgPrivateRoomInfoService, multiNodePrivateRoomInfoService, botConfigInfoService, pendingOperationService);
        register(SitIn.class, botSitInHandler);

        ShotHandler shotHandler = new ShotHandler(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService,
                roomServiceFactory, serverConfigService);
        register(Shot.class, shotHandler);

        BotPurchaseWeaponLootBoxHandler botPurchaseWeaponLootBoxHandler = new BotPurchaseWeaponLootBoxHandler(serializer, singleNodeRoomInfoService,
                multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService, socketService, lobbySessionService);
        register(PurchaseWeaponLootBox.class, botPurchaseWeaponLootBoxHandler);

        setLocalDevOriginsAllowed(serverConfigService.getConfig().isLocalDevAllowed());
    }

    @Override
    IMessageSerializer getSerializer() {
        return serializer;
    }

    @Override
    Logger getLog() {
        return LOG;
    }

    @Override
    void onSuccess(TObject message, BotGameClient client) {
        client.setLastRequestId(message.getRid());
    }

    /**
     * In-game bots will not use web socket connections, so we don't need these methods for them
     */
    @Override
    void createConnection(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        throw new UnsupportedOperationException();
    }

    @Override
    void closeConnection(WebSocketSession session) {
        throw new UnsupportedOperationException();
    }

    @Override
    BotGameClient getClient(WebSocketSession session) {
        throw new UnsupportedOperationException();
    }
}
