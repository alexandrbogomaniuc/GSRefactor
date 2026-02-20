package com.betsoft.casino.mp.web.handlers.game.bots;

import com.betsoft.casino.mp.common.AchievementHelper;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.playerinfo.DefaultRoomPlayerInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.game.SitInHandler;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

public class BotSitInHandler extends SitInHandler {
    private static final Logger LOG = LogManager.getLogger(BotSitInHandler.class);

    public BotSitInHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                           MultiNodeRoomInfoService multiNodeRoomInfoService,
                           RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                           LobbySessionService lobbySessionService, ServerConfigService serverConfigService,
                           CassandraPersistenceManager cpm, CurrencyRateService currencyRateService, CrashGameSettingsService crashGameSettingsService,
                           BGPrivateRoomInfoService bgPrivateRoomInfoService, MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService,
                           BotConfigInfoService botConfigInfoService, IPendingOperationService pendingOperationService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, lobbySessionService, null,
                serverConfigService, cpm, currencyRateService, crashGameSettingsService, bgPrivateRoomInfoService, multiNodePrivateRoomInfoService,
                botConfigInfoService, pendingOperationService);
    }

    @Override
    public void handle(WebSocketSession session, SitIn message, IGameSocketClient client) {
        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
                if (lobbySession != null) {
                    sitIn(message, client, room, getRoomPlayerInfo(lobbySession, client.getRoomId()));
                } else {
                    sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    protected IRoomPlayerInfo getRoomPlayerInfo(LobbySession lobbySession, long roomId) {
        long playerStake = lobbySession.getStakes().get(0);
        return new DefaultRoomPlayerInfo(
                lobbySession.getAccountId(),
                lobbySession.getBankId(),
                roomId,
                -1,
                lobbySession.getSessionId(),
                0,
                lobbySession.getNickname(),
                lobbySession.getAvatar(),
                lobbySession.getEnterDate(),
                new Currency("USD", "$"),
                new PlayerStats(),
                false,
                null,
                null,
                playerStake,
                lobbySession.getStakesReserve(),
                lobbySession.getWeaponMode(), false);
    }

    @SuppressWarnings("unchecked")
    private void sitIn(SitIn message, IGameSocketClient client, IRoom room, IRoomPlayerInfo thisRoomPlayer) {
        try {
            IActionGameSeat seat = (IActionGameSeat) room.createSeat(thisRoomPlayer, client, 1);
            if (room.processSitIn(seat, message) == ErrorCodes.OK) {
                processBuyIn(message, room, seat, thisRoomPlayer);
            } else {
                seat.sendMessage(new Error(ErrorCodes.TOO_MANY_PLAYER, "Too many players",
                        getCurrentTime(), message.getRid()));
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void processBuyIn(SitIn message, IRoom room, IActionGameSeat<? extends IWeapon, ?, ?, ?, ?> seat, IRoomPlayerInfo thisRoomPlayer) {
        //seat.incrementAmmoAmount(message.getAmmoAmount());
        List<Weapon> weapons = new ArrayList<>();
        seat.getWeapons().forEach((type, weapon) -> weapons.add(new Weapon(type.getId(), weapon.getShots())));
        int level = AchievementHelper.getPlayerLevel(thisRoomPlayer.getStats().getScore());
        LobbySession lobbySession = lobbySessionService.get(thisRoomPlayer.getSessionId());
        room.sendChanges(
                new SitInResponse(getCurrentTime(), TObject.SERVER_RID, getSeatNumber(seat), seat.getNickname(),
                        getCurrentTime(), seat.getAmmoAmount(), lobbySession.getBalance(), (Avatar) seat.getAvatar(), weapons,
                        getConvertedLootboxPrices(room, seat), false, level, false, 0, MoneyType.REAL.name(),
                        lobbySession.getBattlegroundRakePercent()),
                new SitInResponse(getCurrentTime(), message.getRid(), getSeatNumber(seat), seat.getNickname(),
                        getCurrentTime(), seat.getAmmoAmount(), lobbySession.getBalance(), (Avatar) seat.getAvatar(), weapons,
                        getConvertedLootboxPrices(room, seat), false, level, false, 0, MoneyType.REAL.name(),
                        lobbySession.getBattlegroundRakePercent()),
                seat.getAccountId(), message
        );
        seat.getPlayerInfo().setSeatNumber(getSeatNumber(seat));
        playerInfoService.put(seat.getPlayerInfo());
        LOG.debug("Bot performed sit in, seat: {}", seat);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
