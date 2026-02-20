package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.ServiceNotStartedException;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.BalanceUpdated;
import com.betsoft.casino.mp.transport.RefreshBalance;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.betsoft.casino.mp.web.socket.UnifiedSocketClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * User: flsh
 * Date: 03.08.18.
 */
@Component
public class RefreshBalanceHandler extends AbstractRoomHandler<RefreshBalance, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(RefreshBalanceHandler.class);
    private SocketService socketService;
    protected LobbySessionService lobbySessionService;

    public RefreshBalanceHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                 MultiNodeRoomInfoService multiNodeRoomInfoService,
                                 RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                 ServerConfigService serverConfigService, SocketService socketService,
                                 LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, RefreshBalance message, IGameSocketClient client) {
        long now = System.currentTimeMillis();
        if (client.getAccountId() == null) {
            sendErrorMessage(client, ErrorCodes.NOT_LOGGED_IN, "RefreshBalance failed: account not found",
                    message.getRid());
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(client.getAccountId());
        //dirty optimization, just return balance, see UnifiedSocketClient.startBalanceUpdater
        if (client.getGameType().isCrashGame()) {
            long balance = lobbySession != null ? lobbySession.getBalance() : ((UnifiedSocketClient) client).getLastUpdatedBalance();
            client.sendMessage(new BalanceUpdated(getCurrentTime(), message.getRid(), balance, 0), message);
            StatisticsManager.getInstance().updateRequestStatistics("RefreshBalanceHandler: fast for crash",
                    System.currentTimeMillis() - now, client.getSessionId() + ":" + client.getRid());
            return;
        }
        ISeat seat;
        IRoom room;
        try {
            room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room == null) {
                return;
            }
            seat = room.getSeat(client.getSeatNumber());
        } catch (ServiceNotStartedException e) {
            processRebootError(client, message, e);
            return;
        } catch (CommonException e) {
            LOG.error("Cannot load room {}", client.getRoomId(), e);
            return;
        }

        int seatAmmoAmount = seat instanceof IActionGameSeat ? ((IActionGameSeat) seat).getAmmoAmount() : 0 ;
        Long balance = getBalance(lobbySession, seat, room);
        if(balance == null) {
            LOG.warn("Cannot load balance form lobbySession, try load from GS");
            getExternalBalance(client, room.getRoomInfo().getMoneyType().name())
                    .doOnSuccess(extBalance -> {
                        getLog().debug("RefreshBalanceHandler, aid: {}, balance: {}, seatAmmoAmount: {}, seat={}",
                                client.getAccountId(), extBalance, seatAmmoAmount, seat);
                        client.sendMessage(new BalanceUpdated(getCurrentTime(), message.getRid(), extBalance,
                                seatAmmoAmount), message);
                    })
                    .doOnError((Throwable error) -> {
                        LOG.error("Failed to RefreshBalance, client{}", client, error);
                        sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "RefreshBalance failed: " + error.getMessage(),
                                message.getRid());
                    })
                    .subscribeOn(Schedulers.elastic())
                    .subscribe();
        } else {
            getLog().debug("RefreshBalanceHandler, aid: {}, balance: {}, seatAmmoAmount: {}, seat={}",
                    client.getAccountId(), balance, seatAmmoAmount, seat);
            client.sendMessage(new BalanceUpdated(getCurrentTime(), message.getRid(), balance, seatAmmoAmount), message);
        }
        getLog().debug("RefreshBalanceHandler clientAmmo: {},  seatAmmoAmount: {}", message.getClientAmmo(),
                seatAmmoAmount);
    }

    private Long getBalance(LobbySession lobbySession, ISeat seat, IRoom room) {
        if (room.isBattlegroundMode()) {
            //see MQBG-63
            return 0L;
        }
        if (seat != null) {
            //playerInfo contains actual balance for PlayGameState
            if (seat.getPlayerInfo().getTournamentSession() != null) {
                return seat.getPlayerInfo().getTournamentSession().getBalance();
            } else if (seat.getPlayerInfo().getActiveCashBonusSession() != null) {
                return seat.getPlayerInfo().getActiveCashBonusSession().getBalance();
            }
        }
        return lobbySession == null ? null : lobbySession.getBalance();
    }

    protected Mono<Long> getExternalBalance(IGameSocketClient client, String mode) {
        return socketService.getBalance(client.getServerId(), client.getSessionId(), mode);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
