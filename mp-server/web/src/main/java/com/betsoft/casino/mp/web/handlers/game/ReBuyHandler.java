package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.common.AbstractBattlegroundGameRoom;
import com.betsoft.casino.mp.data.persister.WeaponsPersister;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.ReBuy;
import com.betsoft.casino.mp.transport.ReBuyResponse;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import static com.betsoft.casino.utils.TObject.SERVER_RID;

@Component
public class ReBuyHandler extends AbstractRoomHandler<ReBuy, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(ReBuyHandler.class);

    private final ForkJoinPool fjPool = new ForkJoinPool();
    private final LobbySessionService lobbySessionService;
    private final SocketService socketService;
    private final IWeaponService weaponService;

    public ReBuyHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                        MultiNodeRoomInfoService multiNodeRoomInfoService,
                        RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                        ServerConfigService serverConfigService, LobbySessionService lobbySessionService,
                        SocketService socketService, CassandraPersistenceManager cpm) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
        this.socketService = socketService;
        this.weaponService = cpm.getPersister(WeaponsPersister.class);
    }

    @Override
    public void handle(WebSocketSession session, ReBuy message, IGameSocketClient client) {
        fjPool.execute(() -> {
            _handle(session, message, client);
        });
    }

    @SuppressWarnings("rawtypes")
    private void _handle(WebSocketSession session, ReBuy message, IGameSocketClient client) {

        Long accountId = client.getAccountId();

        if (client.getRoomId() == null || client.getSeatNumber() < 0 || accountId == null) {
            LOG.error("rid={}, Not seat, roomId={}, SeatNumber={}, accountId={}",
                    message.getRid(), client.getRoomId(), client.getSeatNumber(), accountId);
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
            return;
        }

        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());

        if (lobbySession == null) {
            LOG.error("rid={}, Session not found, lobbySession is null", message.getRid());
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }

        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());

            if (room != null) {

                if (hasPendingOperations(accountId, client, message)) {

                    IRoomPlayerInfo playerInfo = playerInfoService.get(accountId);
                    if (playerInfo == null) {
                        LOG.error("rid={}, Not seat, playerInfo is null", message.getRid());
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                    }
                    return;
                }

                boolean isBattlegroundMode = room.isBattlegroundMode();
                TournamentSession tournamentSession = lobbySession.getTournamentSession();

                if (!isBattlegroundMode) {

                    if (tournamentSession == null) {
                        LOG.error("rid={}, ReBuy is available only in Tournament mode", message.getRid());
                        sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "ReBuy is available only in Tournament mode", message.getRid());
                        return;
                    }

                    if (!isReBuyAvailable(tournamentSession)) {
                        LOG.error("rid={}, ReBuy limit exceeded", message.getRid());
                        sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "ReBuy limit exceeded", message.getRid());
                        return;
                    }

                } else {

                    boolean hasPlayersWithPendingOperation = room.isNotAllowPlayWithAnyPendingPlayers() && hasPlayersWithPendingOperation(room.getId());

                    if (hasPlayersWithPendingOperation) {
                        LOG.error("rid={}, Cannot rebuy, room has players with pending operations", message.getRid());
                        sendErrorMessage(client, ErrorCodes.FOUND_TEMPORARY_PENDING_OPERATION, "Found room with " +
                                "pending operation", message.getRid());
                        return;
                    }
                }

                playerInfoService.lock(accountId);
                getLog().debug("_handle HS lock: {}", accountId);
                IActionGameSeat seat = null;
                boolean needFireReBuyAccepted = false;
                try {

                    seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());
                    if (seat == null || seat.getAccountId() != accountId) {

                        LOG.error("rid={}, Not seat, accountId={}, seat={}", message.getRid(), accountId, seat);
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);

                    } else if (!isBattlegroundMode && !room.isBuyInAllowed(seat)) {

                        LOG.error("rid={}, ReBuy not allowed at current game state", message.getRid());
                        sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "ReBuy not allowed at current game state", message.getRid());

                    } else {
                        Money seatBalance = Money.fromCents(lobbySession.getBalance());
                        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                        long playerStake = seat.getStake().toCents();

                        if (!isBattlegroundMode) {

                            seatBalance = Money.fromCents(playerInfo.getTournamentSession().getBalance());

                            if (playerStake <= seatBalance.toCents() || seat.getAmmoAmount() > 0) {
                                LOG.error("rid={}, Player has enough money for shooting, reBuy refused: seatBalance={}, seat={}",
                                        message.getRid(), seatBalance, seat);
                                sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Player has enough money", message.getRid());
                                return;
                            }

                        } else {//noinspection unchecked

                            if (!room.isBuyInAllowed(seat)) {
                                LOG.error("rid={}, ReBuy not allowed", message.getRid());
                                sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "ReBuy not allowed", message.getRid());
                                return;
                            }
                        }

                        //noinspection rawtypes
                        if (seat instanceof IActionGameSeat && !isBattlegroundMode &&
                                playerHasSpecialWeapons((IActionGameSeat) seat, tournamentSession, room.getGameType().getGameId())) {

                            LOG.error("rid={}, Player has special weapons, re-buy refused: seat={}", message.getRid(), seat);
                            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Player has special weapons", message.getRid());
                            return;
                        }

                        long amount = isBattlegroundMode ?
                                room.getRoomInfo().getBattlegroundBuyIn() :
                                tournamentSession.getReBuyPrice();

                        long balance = getRealBalance(client, MoneyType.REAL);

                        LOG.info("rid={}, Balance before ReBuy: {}, isBattlegroundMode: {}, amount: {}",
                                message.getRid(), balance, isBattlegroundMode, amount);

                        if (amount > balance) {
                            LOG.error("rid={}, Not enough money for ReBuy", message.getRid());
                            sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, "Not enough money", message.getRid());
                            return;
                        }

                        if(room.isBattlegroundMode() && !room.getGameType().isCrashGame()){

                            if(lobbySession.isConfirmBattlegroundBuyIn() && playerInfo.getRoundBuyInAmount() > 0){

                                LOG.info("rid={}, Player {} has buyIn, not need make new buyIn amount: {}",
                                        message.getRid(), accountId, playerInfo.getRoundBuyInAmount());

                                ReBuyResponse reBuyResponse = new ReBuyResponse(
                                        System.currentTimeMillis(),
                                        message.getRid(),
                                        playerInfo.getRoundBuyInAmount(),
                                        isBattlegroundMode ? 0 : lobbySession.getBalance(),
                                        isBattlegroundMode ? 0 : tournamentSession.getReBuyCount()
                                );

                                seat.sendMessage(reBuyResponse, message);
                                return;
                            }
                        }

                        try {
                            playerInfo.setPendingOperation(true, "ReBuy, amount=" + amount + ", rid=" + message.getRid());

                            playerInfoService.put(playerInfo);

                            long roundWin = seat.getRoundWin().toCents();

                            long currentBalance = isBattlegroundMode ?
                                    0 :
                                    tournamentSession.getBalance() + (playerStake * seat.getAmmoAmount()) + roundWin;
                            seat.getCurrentPlayerRoundInfo().setRoundStartBalance(seatBalance);
                            BuyInResult buyInResult = socketService.buyIn(
                                    client.getServerId(),
                                    playerInfo.getId(),
                                    client.getSessionId(),
                                    Money.fromCents(amount),
                                    playerInfo.getGameSessionId(),
                                    playerInfo.getRoomId(),
                                    playerInfo.getBuyInCount(),
                                    isBattlegroundMode ? null : tournamentSession.getTournamentId(),
                                    currentBalance,
                                    room
                            );

                            if (!isBattlegroundMode) {

                                boolean toResetBalance =
                                        buyInResult.getAmount() == (tournamentSession.getReBuyAmount() - currentBalance);

                                if (toResetBalance) {
                                    seat.setRoundWin(new Money(0));
                                    roundWin = 0;
                                }
                            }

                            long reBuyAmount = buyInResult.getAmount() + currentBalance - roundWin;

                            int ammoAmount = (int) (reBuyAmount / playerStake);

                            if (isBattlegroundMode) {
                                reBuyAmount = buyInResult.getAmount();
                                ammoAmount = room.getRoomInfo().getBattlegroundAmmoAmount();
                                lobbySession.setConfirmBattlegroundBuyIn(true);
                                LOG.info("rid={}, ReBuyHandler for battle reBuyAmount: {}, ammoAmount: {}, " +
                                                "lobbySession.setConfirmBattlegroundBuyIn set to true ",
                                        message.getRid(), reBuyAmount, ammoAmount);
                            }

                            seat.incrementAmmoAmount(ammoAmount);
                            seat.incrementTotalAmmoAmount(ammoAmount);

                            playerInfo.makeBuyIn(buyInResult.getPlayerRoundId(), buyInResult.getAmount());
                            playerInfo.setPendingOperation(false);

                            if (reBuyAmount > 0) {
                                playerInfo.incrementBuyInCount();
                                if (!isBattlegroundMode) {
                                    int reBuyCount = playerInfo.getTournamentSession().getReBuyCount();
                                    playerInfo.getTournamentSession().setReBuyCount(reBuyCount + 1);
                                }
                            }

                            seat.updatePlayerRoundInfo(buyInResult.getPlayerRoundId());

                            if (!isBattlegroundMode)
                            {
                                long ammoAmountPrice = ammoAmount * playerStake;
                                long newBalance = reBuyAmount - ammoAmountPrice;

                                tournamentSession.setBalance(newBalance);
                                tournamentSession.setReBuyCount(tournamentSession.getReBuyCount() + 1);

                                playerInfo.getTournamentSession().setBalance(newBalance);
                            }

                            playerInfoService.put(playerInfo);
                            seat.setPlayerInfo(playerInfo);

                            lobbySession.setBalance(
                                    isBattlegroundMode ?
                                            buyInResult.getBalance() :
                                            tournamentSession.getBalance()
                            );

                            lobbySessionService.add(lobbySession);

                            ReBuyResponse reBuyResponse = new ReBuyResponse(
                                    System.currentTimeMillis(),
                                    message.getRid(),
                                    ammoAmount,
                                    isBattlegroundMode ? 0 : lobbySession.getBalance(),
                                    isBattlegroundMode ? 0 : tournamentSession.getReBuyCount()
                            );

                            seat.sendMessage(reBuyResponse, message);

                            if (room.getRoomInfo().isPrivateRoom() && lobbySession.isConfirmBattlegroundBuyIn()) {

                                IBuyInConfirmedSeats buyInConfirmedSeats = room.getTOFactoryService().createBuyInConfirmedSeats(
                                        System.currentTimeMillis(), SERVER_RID, room.getRealConfirmedSeatsId());

                                room.sendChanges(buyInConfirmedSeats);
                            }

                            if(room instanceof AbstractBattlegroundGameRoom) {
                                if(room.getRoomInfo().isPrivateRoom()) {
                                    ((AbstractBattlegroundGameRoom)room)
                                            .updatePlayersStatusAndSendToOwner(
                                                    Arrays.asList(seat.getSocketClient()), Status.READY);
                                }
                            }

                            needFireReBuyAccepted = true;

                        } catch (BuyInFailedException bfExc) {

                            LOG.error("rid=" + message.getRid() + ", Failed to perform ReBuy", bfExc);

                            playerInfo.setPendingOperation(false);
                            playerInfoService.put(playerInfo);

                            if (bfExc.getErrorCode() > 0) {

                                sendErrorMessage(client,
                                        ErrorCodes.translateGameServerErrorCode(bfExc.getErrorCode()),
                                        "ReBuy failed, reason: " + bfExc.getMessage(), message.getRid());
                            } else {

                                sendErrorMessage(client,
                                        bfExc.isFatal() ? ErrorCodes.BAD_BUYIN : ErrorCodes.NOT_FATAL_BAD_BUYIN,
                                        "ReBuy failed, reason: " + bfExc.getMessage(), message.getRid());
                            }
                        } catch (Exception e) {

                            LOG.error("rid=" + message.getRid() + ", Failed to perform ReBuy", e);

                            playerInfo.setPendingOperation(false);
                            playerInfoService.put(playerInfo);

                            sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR,
                                    "ReBuy failed, reason: " + e.getMessage(), message.getRid());
                        }
                    }
                } finally {

                    playerInfoService.unlock(accountId);
                    getLog().debug("_handle HS unlock: {}", accountId);
                    //need release lock by account before fireReBuyAccepted with internal lock by room
                    if (needFireReBuyAccepted) {
                        //noinspection unchecked
                        room.fireReBuyAccepted(seat);
                    }
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private long getRealBalance(IGameSocketClient client, MoneyType moneyType) throws Exception {
        return socketService.getBalanceSync(client.getServerId(), client.getSessionId(), moneyType.name());
    }

    private boolean isReBuyAvailable(TournamentSession session) {
        return session.isReBuyAllowed() && (session.getReBuyLimit() == -1 || session.getReBuyCount() < session.getReBuyLimit());
    }

    @SuppressWarnings("unchecked")
    private boolean playerHasSpecialWeapons(IActionGameSeat seat, TournamentSession session, long gameId) {
        for (IWeapon weapon : (Collection<IWeapon>) seat.getWeapons().values()) {
            if (weapon.getShots() > 0) {
                return true;
            }
        }
        Map<Money, Map<Integer, Integer>> weaponsByStake = weaponService.getAllSpecialModeWeapons(
                session.getTournamentId(), session.getAccountId(), MoneyType.TOURNAMENT.ordinal(), gameId);
        for (Map.Entry<Money, Map<Integer, Integer>> entry : weaponsByStake.entrySet()) {
            if (!seat.getStake().equals(entry.getKey())) {
                Map<Integer, Integer> weapons = entry.getValue();
                for (int count : weapons.values()) {
                    if (count > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
