package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.AbstractCrashGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.Seat;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.onlineplayer.SocketClientInfo;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.CrashBet;
import com.betsoft.casino.mp.transport.CrashBetResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.betsoft.casino.utils.TInboundObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

/**
 * Handler for receiving crash bets
 */
public abstract class AbstractCrashBetHandler<MESSAGE extends TInboundObject> extends AbstractRoomHandler<MESSAGE, IGameSocketClient> {
    private final ForkJoinPool fjPool = new ForkJoinPool();
    private final SocketService socketService;
    private final LobbySessionService lobbySessionService;
    protected final CrashGameSettingsService crashGameSettingsService;
    protected final RoomPlayersMonitorService roomPlayersMonitorService;

    public AbstractCrashBetHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                   MultiNodeRoomInfoService multiNodeRoomInfoService, RoomPlayerInfoService playerInfoService,
                                   RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                                   SocketService socketService, LobbySessionService lobbySessionService,
                                   CrashGameSettingsService crashGameSettingsService,
                                   RoomPlayersMonitorService roomPlayersMonitorService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
        this.crashGameSettingsService = crashGameSettingsService;
        this.roomPlayersMonitorService = roomPlayersMonitorService;
    }

    @Override
    public void handle(WebSocketSession session, MESSAGE message, IGameSocketClient client) {
        fjPool.execute(() -> {
            _handle(session, message, client);
        });
    }

    protected void _handle(WebSocketSession session, MESSAGE message, IGameSocketClient client) {

        Long accountId = client.getAccountId();

        if (client.getRoomId() == null || client.getSeatNumber() < 0 || accountId == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Room not open", message.getRid(), message);
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

        if (client.getGameType().isBattleGroundGame()) {

            _handleBattle(session, message, client, accountId, lobbySession);

        } else {

            _handleReal(session, message, client, accountId, lobbySession);
        }
    }

    private boolean isValidRoomStateForBet(RoomState roomState) {
        return RoomState.WAIT.equals(roomState);
    }

    protected void _handleReal(WebSocketSession session, MESSAGE message, IGameSocketClient client, Long accountId, LobbySession lobbySession) {

        try {
            AbstractCrashGameRoom room = (AbstractCrashGameRoom) getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                if (!isValidRoomStateForBet(room.getGameState().getRoomState())) {
                    sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "BuyIn not allowed at current game state",
                            message.getRid());
                    return;
                }
                if (hasPendingOperations(accountId, client, message)) {
                    return;
                }
                if (room.isBattlegroundMode()) {
                    sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Not allowed for battleground mode",
                            message.getRid());
                    return;
                }
                getLog().debug("lockSeat, accountId={}, message={}", accountId, message);
                room.lockSeat(accountId);
                Seat seat = null;
                try {

                    seat = room.getSeatByAccountId(client.getAccountId());
                    placeRealBets(seat, message, client, room, lobbySession, accountId);

                    //save client info for BigQuery report. Save only in case
                    //if placeRealBets successful
                    this.saveSocketClientInfo(client, room);

                } finally {
                    room.unlockSeat(accountId);
                    getLog().debug("unlockSeat, accountId={}", accountId);
                    if (seat != null) {
                        //need release lock by account before fireReBuyAccepted with internal lock by room
                        room.fireReBuyAccepted(seat);
                    }
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    protected void _handleBattle(WebSocketSession session, MESSAGE message, IGameSocketClient client, Long accountId, LobbySession lobbySession) {
        try {

            BattleAbstractCrashGameRoom room = (BattleAbstractCrashGameRoom) getRoomWithCheck(message.getRid(),
                    client.getRoomId(), client, client.getGameType());

            if (room != null) {

                boolean hasPendingOperations = hasPendingOperations(accountId, client, message);
                if (hasPendingOperations) {
                    return;
                }

                getLog().debug("lockSeat, accountId={}, message={}", accountId, message);
                room.lockSeat(accountId);
                com.betsoft.casino.mp.maxblastchampions.model.Seat seat = null;

                try {

                    seat = room.getSeatByAccountId(client.getAccountId());
                    placeBattleBets(seat, message, client, room, lobbySession, accountId);

                    //save client info for BigQuery report. Save only in case
                    //if placeBattleBets successful
                    this.saveSocketClientInfo(client, room);

                } finally {

                    room.unlockSeat(accountId);
                    getLog().debug("unlockSeat, accountId={}", accountId);

                    if (seat != null) {
                        //need release lock by account before fireReBuyAccepted with internal lock by room
                        room.fireReBuyAccepted(seat);
                    }
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }


    protected abstract void placeRealBets(Seat seat, MESSAGE message, IGameSocketClient client, AbstractCrashGameRoom room, LobbySession lobbySession,
                                      Long accountId);

    /**
     * Gets bet, sends bet to gs, processes response, saves bet for seat.If bet is not correct sends error to client.
     * @param seat Seat of player
     * @param message CrashBet message from client
     * @param lobbySession lobby session of player
     * @param client game socket client
     * @param room room where a player is seat in
     * @param accountId account id of player
     */
    protected void placeRealBet(Seat seat, CrashBet message, LobbySession lobbySession, IGameSocketClient client, AbstractCrashGameRoom room, Long accountId, Money seatBalance) {
        ICrashGameRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        Money amount = Money.fromCents(message.getCrashBetAmount());
        long amountInCents = amount.toCents();
        if (amountInCents > seatBalance.toCents()) {
            getLog().error("Not enough money, amount={}, seatBalance={}, seat={}",
                    amount, seatBalance.toCents(), seat);
            sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, "Not enough money",
                    message.getRid(), message);
            return;
        }
        ICrashGameSetting settings = crashGameSettingsService.getSettings(lobbySession.getBankId(), room.getGameType().getGameId(),
                playerInfo.getCurrency().getCode());
        if (amountInCents < settings.getMinStake() || amountInCents > settings.getMaxStake()) {
            getLog().error("Bad stake value, amountInCents={}, settings={}, seat={}", amountInCents, settings, seat);
            sendErrorMessage(client, ErrorCodes.BAD_STAKE, "Bad stake value", message.getRid(), message);
            return;
        }
        int countOfBetsLimit = room.allowedBetsCount();
        if (seat.getCrashBetsCount() >= countOfBetsLimit) {
            getLog().error("Large crash bets in round , amount={}, seatBalance={}, seat={}, crashBets: {}",
                    amount, seatBalance.toCents(), seat, seat.getCrashBets());
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Only" + countOfBetsLimit + " bets allowed",
                    message.getRid(), message);
            return;
        }
        double messageMultiplier = message.getMultiplier();
        double roundedMultiplier = BigDecimal.valueOf(messageMultiplier).setScale(2, RoundingMode.HALF_UP).doubleValue();
        //after fix Bot, remove this check or sendErrorMessage - ErrorCodes.BAD_MULTIPLIER
        if (messageMultiplier - roundedMultiplier != 0.0) {
            getLog().error("Found suspicious message multiplier, messageMultiplier={}, roundedMultiplier={}",
                    messageMultiplier, roundedMultiplier);
        }

        if ((!message.isAutoPlay() && roundedMultiplier > 0) ||
                (message.isAutoPlay() && !room.isValidMultiplier(roundedMultiplier))) {
            getLog().error("Bad multiplier value , multiplier={}, seatBalance={}, seat={}, crashBets: {}",
                    roundedMultiplier, seatBalance.toCents(), seat, seat.getCrashBets());
            sendErrorMessage(client, ErrorCodes.BAD_MULTIPLIER, "Bad multiplier value",
                    message.getRid(), message);
            return;
        }
        if (amount.lessOrEqualsTo(Money.ZERO)) {
            getLog().error("Impossible error: bad amount={}", amountInCents);
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Buy in failed, bad amount value", message.getRid());
            return;
        }
        if (hasPendingOperations(accountId, client, message)) {
            return;
        }
        Money correctedBetAmount = amount;
        long canceledBetAmount = seat.getCanceledBetAmount();
        boolean isSendRealBetWin = lobbySession.isSendRealBetWin();
        if (isSendRealBetWin) {
            correctedBetAmount = seat.correctAmountWithCanceledBets(amount);
        }
        try {
            BuyInResult buyInResult = null;
            if (correctedBetAmount.greaterThan(Money.ZERO) && isSendRealBetWin) {
                playerInfo.setPendingOperation(true, "BuyIn, amount=" + amountInCents + ", rId=" + message.getRid());
                playerInfoService.put(playerInfo);
                if(shouldUpdateStartBalance(seat)) {
                    seat.getCurrentPlayerRoundInfo().setRoundStartBalance(seatBalance);
                }
                buyInResult = socketService.buyIn(client.getServerId(), playerInfo.getId(),
                        client.getSessionId(), correctedBetAmount, playerInfo.getGameSessionId(),
                        playerInfo.getRoomId(), playerInfo.getBuyInCount(), null, room);
                playerInfo.makeBuyIn(buyInResult.getPlayerRoundId(), buyInResult.getAmount());
                playerInfo.setPendingOperation(false);
                if (buyInResult.getAmount() > 0) {
                    playerInfo.incrementBuyInCount();
                }
                if (!room.getGameState().isBuyInAllowed(seat) && buyInResult.isSuccess()) {
                    seat.incrementCanceledBetAmount(amountInCents);
                    playerInfoService.put(playerInfo);
                    seat.setPlayerInfo(playerInfo);
                    room.saveSeat(0, seat);
                    lobbySession.setBalance(buyInResult.getBalance());
                    lobbySessionService.add(lobbySession);
                    sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED_ALREADY, "BuyIn not allowed at current game state",
                            message.getRid(), message);
                    ITransportObject seatResponse = room.getTOFactoryService().createCrashCancelBetResponse(
                            System.currentTimeMillis(), message.getRid(), 0, room.getSeatNumber(seat),
                            amountInCents, message.getBetId(), seat.getNickname(), lobbySession.getBalance());
                    seat.sendMessage(seatResponse);
                    return;
                }
            }
            String crashBetKey = System.currentTimeMillis() + "_" + seat.getNickname();
            if (message.getBetId() != null) {
                crashBetKey = message.getBetId();
            }
            CrashBetInfo crashBetInfo = new CrashBetInfo(amountInCents, roundedMultiplier, message.isAutoPlay());
            if (!isSendRealBetWin) {
                crashBetInfo.setReserved(true);
            }
            seat.addCrashBet(crashBetKey, crashBetInfo);
            playerInfoService.put(playerInfo);
            if (buyInResult != null) {
                seat.updatePlayerRoundInfo(buyInResult.getPlayerRoundId());
                lobbySession.setBalance(buyInResult.getBalance());
                lobbySessionService.add(lobbySession);
            }
            seat.setPlayerInfo(playerInfo);
            room.saveSeat(0, seat);
            seat.sendMessage(new CrashBetResponse(System.currentTimeMillis(), message.getRid(),
                    amountInCents, lobbySession.getBalance(), crashBetKey), message);
            //send to seats on this server
            CrashBetResponse otherSeatsMessage = new CrashBetResponse(System.currentTimeMillis(), TObject.SERVER_RID,
                    amountInCents, 0L, crashBetKey);
            Collection<IGameSocketClient> observers = room.getObservers();
            for (IGameSocketClient observer : observers) {
                if (!observer.isDisconnected() && observer.getAccountId() != playerInfo.getId()) {
                    sendMessageToOtherSeat(otherSeatsMessage, observer);
                }
            }
            SendSeatsMessageTask sendSeatsMessageTask = (SendSeatsMessageTask) room.createSendSeatsMessageTask(playerInfo.getId(),
                    true, message.getRid(), otherSeatsMessage, true);
            room.executeOnAllMembers(sendSeatsMessageTask);
            getLog().debug("Success processing, seat.isDisconnected()={}, seat={}", seat.isDisconnected(), seat);
        } catch (Exception e) {
            getLog().error("Failed to perform buy in", e);
            playerInfo.setPendingOperation(false);
            //rollback changes
            seat.setCanceledBetAmount(canceledBetAmount);
            playerInfoService.put(playerInfo);
            int errorCode = room.getBuyInFailedErrorCode(e);
            sendErrorMessage(client, errorCode,
                            "BuyIn failed, reason: " + e.getMessage(), message.getRid());
        }
        playerInfoService.put(playerInfo);
    }
    private boolean shouldUpdateStartBalance(Seat seat){
        return seat.getCrashBetsCount()==0;
    }

    protected abstract void placeBattleBets(com.betsoft.casino.mp.maxblastchampions.model.Seat seat, MESSAGE message, IGameSocketClient client, BattleAbstractCrashGameRoom room, LobbySession lobbySession,
                                          Long accountId);
    /**
     * Gets bet, sends bet to gs, processes response, saves bet for seat.If bet is not correct sends error to client.
     * @param seat Seat of player
     * @param message CrashBet message from client
     * @param lobbySession lobby session of player
     * @param client game socket client
     * @param room room where a player is seat in
     * @param accountId account id of player
     */
    protected void placeBattleBet(com.betsoft.casino.mp.maxblastchampions.model.Seat seat, CrashBet message,
                                  LobbySession lobbySession, IGameSocketClient client, BattleAbstractCrashGameRoom room, Long accountId) {

        ICrashGameRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        IRoomInfo roomInfo = room != null ? room.getRoomInfo() : null;

        boolean isPrivateRoom = roomInfo != null ? roomInfo.isPrivateRoom() : false;

        if(isPrivateRoom) {
            if(roomInfo.isPlayerKicked(accountId)) {
                getLog().error("placeBattleBet: The player {} is kicked, skip place battle bet {}", accountId, message);
                sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Player is kicked", message.getRid(), message);
                return;
            }
        }

        Money betAmount = Money.fromCents(message.getCrashBetAmount());
        long amountInCents = betAmount.toCents();

        Money seatBalance = Money.fromCents(lobbySession.getBalance() + seat.getCanceledBetAmount());
        long  seatBalanceInCents = seatBalance.toCents();

        if (amountInCents > seatBalanceInCents) {

            getLog().error("placeBattleBet: Not enough money, betAmount={}, seatBalance={}, seat={}",
                    betAmount, seatBalanceInCents, seat);
            sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, "Not enough money",
                    message.getRid(), message);
            return;
        }

        long roomBattleStake = room.getRoomInfo().getStake().toCents();

        if (amountInCents != roomBattleStake) {

            getLog().error("placeBattleBet: wrong bet  betAmount , betAmount={}, seatBalance={}, seat={}, crashBets: {}",
                    betAmount, seatBalanceInCents, seat, seat.getCrashBets());
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Only " + roomBattleStake + " bets allowed",
                    message.getRid(), message);
            return;
        }

        double messageMultiplier = message.getMultiplier();
        double roundedMultiplier = BigDecimal
                .valueOf(messageMultiplier).setScale(2, RoundingMode.HALF_UP).doubleValue();

        //after fix Bot, remove this check or sendErrorMessage - ErrorCodes.BAD_MULTIPLIER
        if (messageMultiplier - roundedMultiplier != 0.0) {

            getLog().error("placeBattleBet: Found suspicious message multiplier, messageMultiplier={}, roundedMultiplier={}",
                    messageMultiplier, roundedMultiplier);
        }

        if ((!message.isAutoPlay() && roundedMultiplier > 0) ||
                (message.isAutoPlay() && !room.isValidMultiplier(roundedMultiplier))) {

            getLog().error("placeBattleBet: Bad multiplier value , multiplier={}, seatBalance={}, seat={}, crashBets: {}",
                    roundedMultiplier, seatBalanceInCents, seat, seat.getCrashBets());
            sendErrorMessage(client, ErrorCodes.BAD_MULTIPLIER, "Bad multiplier value",
                    message.getRid(), message);
            return;
        }

        if (betAmount.lessOrEqualsTo(Money.ZERO)) {

            getLog().error("placeBattleBet: Impossible error: bad betAmount={}", amountInCents);
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Buy in failed, bad amount value", message.getRid());
            return;
        }

        if (hasPendingOperations(accountId, client, message)) {
            return;
        }

        if(!seat.getCrashBets().isEmpty()) {

            getLog().error("placeBattleBet: player has bets, new bet is not allowed {}", seat.getCrashBets());
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Buy in failed, bad number of bets", message.getRid());
            return;
        }

        Money correctedBetAmount = betAmount;
        long canceledBetAmount = seat.getCanceledBetAmount();

        if (canceledBetAmount > 0) {

            long betAmountInCents = betAmount.toCents();
            getLog().debug("placeBattleBet: not zero canceledBetAmount, before correction canceledBetAmount={}, betAmountInCents={}",
                    canceledBetAmount, betAmountInCents);

            if (canceledBetAmount >= betAmountInCents) {

                correctedBetAmount = Money.ZERO;
                seat.setCanceledBetAmount(canceledBetAmount - betAmountInCents);

            } else {

                correctedBetAmount = Money.fromCents(betAmountInCents - canceledBetAmount);
                seat.setCanceledBetAmount(0);

            }

            getLog().debug("placeBattleBet: not zero canceledBetAmount, after correction seat.getCanceledBetAmount()={}, correctedBetAmount={}",
                    seat.getCanceledBetAmount(), correctedBetAmount.toCents());
        }

        try {
            BuyInResult buyInResult = null;

            if (correctedBetAmount.greaterThan(Money.ZERO)) {

                playerInfo.setPendingOperation(true, "BuyIn, amount=" + amountInCents + ", rId=" + message.getRid());
                playerInfoService.put(playerInfo);

                seat.getCurrentPlayerRoundInfo().setRoundStartBalance(seatBalance);

                buyInResult = socketService.buyIn(client.getServerId(), playerInfo.getId(),
                        client.getSessionId(), correctedBetAmount, playerInfo.getGameSessionId(),
                        playerInfo.getRoomId(), playerInfo.getBuyInCount(), null, room);

                playerInfo.makeBuyIn(buyInResult.getPlayerRoundId(), buyInResult.getAmount());
                playerInfo.setPendingOperation(false);

                if (buyInResult.getAmount() > 0) {
                    playerInfo.incrementBuyInCount();
                }

            }

            String crashBetKey = System.currentTimeMillis() + "_" + seat.getNickname();
            if (message.getBetId() != null) {
                crashBetKey = message.getBetId();
            }

            CrashBetInfo crashBetInfo = new CrashBetInfo(amountInCents, roundedMultiplier, message.isAutoPlay());
            seat.getCurrentPlayerRoundInfo().setRoundStartBalance(seatBalance);
            seat.addCrashBet(crashBetKey, crashBetInfo);

            playerInfoService.put(playerInfo);

            if (buyInResult != null) {

                seat.updatePlayerRoundInfo(buyInResult.getPlayerRoundId());

                lobbySession.setBalance(buyInResult.getBalance());
                lobbySessionService.add(lobbySession);
            }

            seat.setPlayerInfo(playerInfo);

            room.saveSeat(0, seat);

            CrashBetResponse crashBetResponse = new CrashBetResponse(System.currentTimeMillis(), message.getRid(),
                    amountInCents, lobbySession.getBalance(), crashBetKey);
            seat.sendMessage(crashBetResponse, message);

            //send to seats on this server
            CrashBetResponse otherSeatsCrashBetResponse = new CrashBetResponse(System.currentTimeMillis(), TObject.SERVER_RID,
                    amountInCents, 0L, crashBetKey);

            Collection<IGameSocketClient> observers = room.getObservers();
            for (IGameSocketClient observer : observers) {
                if (!observer.isDisconnected() && observer.getAccountId() != playerInfo.getId()) {
                    sendMessageToOtherSeat(otherSeatsCrashBetResponse, observer);
                }
            }

            SendSeatsMessageTask sendSeatsMessageTask = (SendSeatsMessageTask) room.createSendSeatsMessageTask(
                    playerInfo.getId(),
                    true,
                    message.getRid(),
                    otherSeatsCrashBetResponse,
                    true);

            room.executeOnAllMembers(sendSeatsMessageTask);

            getLog().debug("placeBattleBet: Success processing, seat.isDisconnected()={}, seat={}", seat.isDisconnected(), seat);

            if(isPrivateRoom) {
                try {
                    room.updatePlayersStatusNicknamesOnly
                            (Arrays.asList(seat.getNickname()), Status.READY, false, true);

                } catch (Exception e) {
                    getLog().error("placeBattleBet: Exception to updatePlayersStatusAndSentToOwner, " +
                            "{}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {

            getLog().error("Failed to perform buy in", e);
            playerInfo.setPendingOperation(false);
            //rollback changes
            seat.setCanceledBetAmount(canceledBetAmount);

            playerInfoService.put(playerInfo);

            if (e instanceof BuyInFailedException) {
                BuyInFailedException bfExc = (BuyInFailedException) e;
                if (bfExc.getErrorCode() > 0) {
                    sendErrorMessage(client,
                            ErrorCodes.translateGameServerErrorCode(bfExc.getErrorCode()),
                            "BuyIn failed, reason: " + e.getMessage(), message.getRid());
                } else {
                    sendErrorMessage(client, bfExc.isFatal() ?
                                    ErrorCodes.BAD_BUYIN : ErrorCodes.NOT_FATAL_BAD_BUYIN,
                            "Buy in failed, reason: " + e.getMessage(), message.getRid());
                }
            } else {
                sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR,
                        "Buy in failed, reason: " + e.getMessage(), message.getRid());
            }
        }

        playerInfoService.put(playerInfo);
    }

    @SuppressWarnings("rawtypes")
    private void sendMessageToOtherSeat(CrashBetResponse otherSeatsMessage, IGameSocketClient observer) {
        try {
            observer.sendMessage(otherSeatsMessage);
        } catch (Exception e) {
            getLog().debug("Cannot send message to otherSeat, accountId={}", observer.getAccountId(), e);
        }
    }

    private void saveSocketClientInfo(IGameSocketClient client, IRoom room) {

        if(client == null) {
            getLog().error("saveSocketClientInfo: client is null");
            return;
        }

        if(StringUtils.isTrimmedEmpty(client.getWebSocketSessionId())) {
            getLog().error("saveSocketClientInfo: client.getWebSocketSessionId() is empty: {}", client);
            return;
        }

        if(room == null) {
            getLog().error("saveSocketClientInfo: room is null");
            return;
        }

        IRoomInfo roomInfo = room.getRoomInfo();

        if(roomInfo == null) {
            getLog().error("saveSocketClientInfo: roomInfo is null in room: {}", room);
            return;
        }

        GameType gameType = roomInfo.getGameType();

        if(gameType == null) {
            getLog().error("saveSocketClientInfo: gameType is null in roomInfo: {}", roomInfo);
            return;
        }

        try {
            String playerExternalId = null;
            if (client instanceof ILobbySocketClient) {
                IPlayerInfo playerInfo = ((ILobbySocketClient) client).getPlayerInfo();
                if (playerInfo != null) {
                    playerExternalId = playerInfo.getExternalId();
                }
            }

            SocketClientInfo socketClientInfo
                    = roomPlayersMonitorService.convert(client, room, playerExternalId);

            roomPlayersMonitorService.upsertSocketClientInfo(socketClientInfo);

        } catch (Exception exception) {
            getLog().error("saveSocketClientInfo: exception: {}", exception.getMessage(), exception);
        }
    }
}
