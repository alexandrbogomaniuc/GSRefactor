package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.AbstractCrashGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.Seat;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.CrashBet;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Handler for getting crash bet
 */
@Component
public class CrashBetHandler extends AbstractCrashBetHandler<CrashBet> {
    private static final Logger LOG = LogManager.getLogger(CrashBetHandler.class);

    public CrashBetHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                           MultiNodeRoomInfoService multiNodeRoomInfoService,
                           RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                           SocketService socketService,
                           ServerConfigService serverConfigService,
                           LobbySessionService lobbySessionService,
                           CrashGameSettingsService crashGameSettingsService,
                           RoomPlayersMonitorService roomPlayersMonitorService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService,
                socketService, lobbySessionService, crashGameSettingsService, roomPlayersMonitorService);
    }

    /**
     * Handle CrashBet message from client for a usual game.
     * @param seat         Seat of player
     * @param message      CrashBet message from client
     * @param client       game socket client
     * @param room         room where a player is seat in
     * @param lobbySession lobby session of player
     * @param accountId    account id of player
     */
    @Override
    protected void placeRealBets(Seat seat, CrashBet message, IGameSocketClient client, AbstractCrashGameRoom room, LobbySession lobbySession, Long accountId) {
        if (seat == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
        } else if (!room.isBuyInAllowed(seat)) {
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "BuyIn not allowed at current game state",
                    message.getRid());
        } else {
            Money seatBalance =  Money.fromCents(lobbySession.getBalance() + seat.getCanceledBetAmount());
            placeRealBet(seat, message, lobbySession, client, room, accountId, seatBalance);
        }
    }

    /**
     * Handle CrashBet message from client for a battle game.
     * @param seat         Seat of player
     * @param message      CrashBet message from client
     * @param client       game socket client
     * @param room         room where a player is seat in
     * @param lobbySession lobby session of player
     * @param accountId    account id of player
     */
    @Override
    protected void placeBattleBets(com.betsoft.casino.mp.maxblastchampions.model.Seat seat, CrashBet message, IGameSocketClient client,
                                   BattleAbstractCrashGameRoom room, LobbySession lobbySession, Long accountId) {
        if (seat == null) {

            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);

        } else if (!room.isBuyInAllowed(seat)) {

            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "BuyIn not allowed at current game state",
                    message.getRid());
        } else {

            placeBattleBet(seat, message, lobbySession, client, room, accountId);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
