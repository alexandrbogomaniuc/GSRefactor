package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.BuyIn;
import com.betsoft.casino.mp.transport.BuyInResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.concurrent.ForkJoinPool;

/**
 * User: flsh
 * Date: 03.11.17.
 */
@Component
public class BuyInHandler extends AbstractRoomHandler<BuyIn, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(BuyInHandler.class);
    private final SocketService socketService;
    protected final LobbySessionService lobbySessionService;
    private final ForkJoinPool fjPool = new ForkJoinPool();

    public BuyInHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                        MultiNodeRoomInfoService multiNodeRoomInfoService,
                        RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                        SocketService socketService,
                        ServerConfigService serverConfigService,
                        LobbySessionService lobbySessionService,
                        CassandraPersistenceManager cpm) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
    }

    @Override
    public void handle(WebSocketSession session, BuyIn message, IGameSocketClient client) {
        fjPool.execute(() -> {
            _handle(session, message, client);
        });
    }

    private void _handle(WebSocketSession session, BuyIn message, IGameSocketClient client) {
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
        if (lobbySession.getActiveFrbSession() != null || lobbySession.getActiveCashBonusSession() != null
                || lobbySession.getTournamentSession() != null) {
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Not allowed for bonus mode", message.getRid());
            return;
        }
        try {
            @SuppressWarnings("rawtypes")
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                if (hasPendingOperations(accountId, client, message)) {
                    return;
                }
                if (room.isBattlegroundMode()) {
                    sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Not allowed for battleground mode",
                            message.getRid());
                    return;
                }
                playerInfoService.lock(accountId);
                getLog().debug("_handle HS lock: {}", accountId);
                try {
                    @SuppressWarnings("rawtypes")
                    IActionGameSeat seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());
                    if (seat == null || seat.getAccountId() != accountId) {
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                    } else if (!room.isBuyInAllowed(seat)) {
                        sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "BuyIn not allowed at current game state",
                                message.getRid());
                    } else {

                        int betLevel = seat.getBetLevel();

                        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();

                        long stakesReserve = playerInfo.getStakesReserve();
                        if (stakesReserve <= 0) { //temp fix for old code compatibility, remove after deploy to All systems
                            stakesReserve = IRoom.DEFAULT_STAKES_RESERVE;
                        }

                        long stakesLimit = lobbySession.getStakesLimit();
                        if (stakesLimit <= 0) {
                            stakesLimit = IRoom.DEFAULT_STAKES_LIMIT;
                        }

                        stakesReserve *= betLevel;
                        stakesLimit *= betLevel;

                        getLog().debug("betLevel: {}, stakesReserve: {}, stakesLimit: {}",
                                betLevel, stakesReserve, stakesLimit);

                        if (seat.getAmmoAmount() > stakesLimit + 10) {
                            LOG.warn("ammoAmount>stakesLimit+10, seat.getAmmoAmount()={}, stakesLimit={}",
                                    seat.getAmmoAmount(), stakesLimit);
                            seat.sendMessage(new BuyInResponse(System.currentTimeMillis(), message.getRid(),
                                    0, lobbySession.getBalance()), message);
                            return;
                        }

                        Money amount = seat.getStake().multiply(stakesReserve);

                        Money lobbyBalance = Money.fromCents(lobbySession.getBalance());

                        if (seat.getStake().toCents() > lobbyBalance.toCents()) {
                            LOG.error("Not enough money, amount={}, lobbyBalance={}, roundWin={}, stakesReserve={}, " +
                                            "seat={}", amount, lobbyBalance.toCents(),
                                    seat.getRoundWin().toCents(), stakesReserve, seat);
                            //hmmm, may be call to update balance ???
                            sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, "Not enough money",
                                    message.getRid(), message);
                            return;
                        }

                        if (amount.greaterThan(lobbyBalance)) {

                            long correctedStakesReserve = lobbyBalance.divideBy(seat.getStake());
                            Money correctedAmount = seat.getStake().multiply(correctedStakesReserve);

                            LOG.warn("Low balance reduce ammo for buy, amount={}, lobbyBalance={}, seat roundWin={}, " +
                                            "stakesReserve={},  correctedStakesReserve={}, correctedAmount={}, seat={}",
                                    amount.toCents(),
                                    lobbyBalance.toCents(),
                                    seat.getRoundWin().toCents(),
                                    stakesReserve,
                                    correctedStakesReserve,
                                    correctedAmount.toCents(),
                                    seat);

                            stakesReserve = correctedStakesReserve;
                            amount = correctedAmount;

                        }

                        final int ammoAmount = (int) stakesReserve;

                        if (amount.greaterThan(Money.ZERO)) {

                            if (hasPendingOperations(accountId, client, message)) {
                                return;
                            }

                            try {
                                playerInfo.setPendingOperation(true, "BuyIn, amount=" + amount.toCents() +
                                        ", rId=" + message.getRid());
                                playerInfoService.put(playerInfo);

                                IPlayerRoundInfo playerRoundInfo = seat.getCurrentPlayerRoundInfo();

                                //set Round start balance at the begging of the round only when it is 0. It could be
                                //that during betLevel change lobbyBalance has value after the previous BuyInHandler::handle call.
                                if(playerRoundInfo != null && playerRoundInfo.getRoundStartBalance().toCents() == 0) {
                                    playerRoundInfo.setRoundStartBalance(lobbyBalance);
                                }

                                BuyInResult buyInResult = socketService.buyIn(
                                        client.getServerId(),
                                        playerInfo.getId(),
                                        client.getSessionId(),
                                        amount,
                                        playerInfo.getGameSessionId(),
                                        playerInfo.getRoomId(),
                                        playerInfo.getBuyInCount(),
                                        null,
                                        room
                                );

                                seat.incrementAmmoAmount(ammoAmount);
                                seat.incrementTotalAmmoAmount(ammoAmount);

                                playerInfo.makeBuyIn(buyInResult.getPlayerRoundId(), buyInResult.getAmount());
                                playerInfo.setPendingOperation(false);

                                if (buyInResult.getAmount() > 0) {
                                    playerInfo.incrementBuyInCount();
                                }

                                playerInfoService.put(playerInfo);
                                seat.updatePlayerRoundInfo(buyInResult.getPlayerRoundId());
                                seat.setPlayerInfo(playerInfo);

                                seat.sendMessage(new BuyInResponse(System.currentTimeMillis(), message.getRid(),
                                        ammoAmount, buyInResult.getBalance()), message);

                                lobbySession.setBalance(buyInResult.getBalance());
                                lobbySessionService.add(lobbySession);

                            } catch (BuyInFailedException bfExc) {
                                LOG.error("Failed to perform buy in", bfExc);
                                playerInfo.setPendingOperation(false);
                                IRoomPlayerInfo actualPlayerInfo = playerInfoService.get(accountId);
                                if (actualPlayerInfo != null) {
                                    playerInfoService.put(playerInfo);
                                }
                                if (bfExc.getErrorCode() > 0) {
                                    sendErrorMessage(client,
                                            ErrorCodes.translateGameServerErrorCode(bfExc.getErrorCode()),
                                            "BuyIn failed, reason: " + bfExc.getMessage(), message.getRid());
                                } else {
                                    sendErrorMessage(client, bfExc.isFatal() ?
                                                    ErrorCodes.BAD_BUYIN : ErrorCodes.NOT_FATAL_BAD_BUYIN,
                                            "Buy in failed, reason: " + bfExc.getMessage(), message.getRid());
                                }
                            } catch (Exception e) {
                                LOG.error("Failed to perform buy in", e);
                                playerInfo.setPendingOperation(false);
                                IRoomPlayerInfo actualPlayerInfo = playerInfoService.get(accountId);
                                if (actualPlayerInfo != null) {
                                    playerInfoService.put(playerInfo);
                                }
                                sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR,
                                        "Buy in failed, reason: " + e.getMessage(), message.getRid());
                            }
                        } else {
                            LOG.error("Impossible error: bad amount={}", amount.toCents());
                            sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR,
                                    "Buy in failed, bad amount value", message.getRid());
                        }

                        IRoomPlayerInfo actualPlayerInfo = playerInfoService.get(accountId);
                        if (actualPlayerInfo != null) {
                            playerInfoService.put(playerInfo);
                        }
                    }
                } finally {
                    playerInfoService.unlock(accountId);
                    getLog().debug("_handle HS unlock: {}", accountId);
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
