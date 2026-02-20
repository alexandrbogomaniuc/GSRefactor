package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.Seat;
import com.betsoft.casino.mp.model.ICrashBetInfo;
import com.betsoft.casino.mp.model.ICrashGameRoomPlayerInfo;
import com.betsoft.casino.mp.model.IGameState;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.CrashBet;
import com.betsoft.casino.mp.transport.CrashCancelBet;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.RequestStatistic;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.betsoft.casino.utils.ITransportObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
/**
 * Handler for cancel bet. If room is in Play state it equals to eject of bet.
 */
@Component
public class CrashCancelBetHandler extends AbstractRoomHandler<CrashCancelBet, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(CrashCancelBetHandler.class);
    private final SocketService socketService;
    protected final LobbySessionService lobbySessionService;
    private final ForkJoinPool fjPool = new ForkJoinPool();
    private final MultiNodeSeatService multiNodeSeatService;

    public CrashCancelBetHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                 MultiNodeRoomInfoService multiNodeRoomInfoService,
                                 RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                                 SocketService socketService,
                                 ServerConfigService serverConfigService,
                                 LobbySessionService lobbySessionService,
                                 MultiNodeSeatService multiNodeSeatService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
        this.multiNodeSeatService = multiNodeSeatService;
    }

    @Override
    public void handle(WebSocketSession session, CrashCancelBet message, IGameSocketClient client) {
        fjPool.execute(() -> {
            _handle(session, message, client);
        });
    }

    private void _handle(WebSocketSession session, CrashCancelBet message, IGameSocketClient client) {
        Long accountId = client.getAccountId();
        if (client.getRoomId() == null || accountId == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
            return;
        }

        getLog().debug("handle: CrashCancelBet message: {}, accountId: {} ", message, accountId);

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
            RequestStatistic requestStatistic = client.getRequestStatistic(CrashBet.class);
            int cancelBetLimit = message.getCancelBetLimit();
            boolean isLimitApproved = isLimitApproved(message, requestStatistic, cancelBetLimit);

            getLog().debug("handle: CrashCancelBet cancelBetLimit={}, isLimitApproved={}, requestStatistic: {}",
                    cancelBetLimit, isLimitApproved, requestStatistic);

            if(message.isPlaceNewBet() && requestStatistic != null && !isLimitApproved) {
                sendErrorMessage(client, ErrorCodes.REQUEST_FREQ_LIMIT_EXCEEDED, "Too many requests",
                        message.getRid());
                return;
            }

            _handleBattle(message, client, accountId, lobbySession);

        } else {

            _handleReal(message, client, accountId, lobbySession);
        }
    }

    private void _handleReal(CrashCancelBet message, IGameSocketClient client, Long accountId, LobbySession lobbySession) {
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
                try {
                    Seat seat = (Seat) room.getSeatByAccountId(client.getAccountId());
                    if (seat == null) {
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                    } else {
                        ICrashGameRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                        getLog().debug("seat.getPlayerInfo()={}, playerInfoService.playerInfo={}", playerInfo,
                                playerInfoService.get(client.getAccountId()));

                        String crashBetId = message.getCrashBetId();
                        Map<String, ICrashBetInfo> crashBets = seat.getCrashBets();

                        getLog().debug("crashBetInfoMap before: {}, accountId: {} ", crashBets, accountId);

                        //not fatal error, this may be normal if round in Qualify state
                        if (!crashBets.containsKey(crashBetId)) {
                            sendErrorMessage(client, ErrorCodes.BET_NOT_FOUND, "Crash bet is not found in room", message.getRid());
                            return;
                        }

                        @SuppressWarnings("rawtypes")
                        IGameState gameState = room.getGameState();

                        int resultCode = gameState.processCancelCrashMultiplier(
                                seat.getAccountId(),
                                crashBetId,
                                message.getRid(),
                                message.isPlaceNewBet(),
                                message
                        );

                        if (resultCode != ErrorCodes.OK) {
                            //noinspection unchecked
                            if ((message.isPlaceNewBet() && !gameState.isBuyInAllowed(seat)) || resultCode == ErrorCodes.CANCEL_BET_NOT_ALLOWED) {
                                sendErrorMessage(client, ErrorCodes.CANCEL_BET_NOT_ALLOWED, "Not allowed in play and buyIn states", message.getRid());
                            } else if (resultCode == ErrorCodes.BET_NOT_FOUND) {
                              sendErrorMessage(client, ErrorCodes.BET_NOT_FOUND, "Not allowed, round result already processing", message.getRid());
                            } else {
                                sendErrorMessage(client, ErrorCodes.BAD_STAKE, "Crash bet process with error", message.getRid());
                            }
                        } else {
                            getLog().debug("crashBets after: {}, accountId: {} ", crashBets, accountId);
                        }
                        playerInfoService.put(playerInfo);
                    }
                } finally {
                    playerInfoService.unlock(accountId);
                }
            }

        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private void _handleBattle(CrashCancelBet message, IGameSocketClient client, Long accountId, LobbySession lobbySession) {
        try {
            @SuppressWarnings("rawtypes")
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());

            if (room != null) {

                IRoomInfo roomInfo = room.getRoomInfo();

                boolean isPrivateRoom = roomInfo != null ? roomInfo.isPrivateRoom() : false;

                if(isPrivateRoom) {
                    if(roomInfo.isPlayerKicked(accountId)) {
                        getLog().error("_handleBattle: The player {} is kicked, skip cancel battle bet {}", accountId, message);
                        sendErrorMessage(client, ErrorCodes.CANCEL_BET_NOT_ALLOWED, "Player is kicked", message.getRid(), message);
                        return;
                    }
                }

                if (hasPendingOperations(accountId, client, message)) {
                    return;
                }

                playerInfoService.lock(accountId);

                try {
                    com.betsoft.casino.mp.maxblastchampions.model.Seat seat =
                            (com.betsoft.casino.mp.maxblastchampions.model.Seat)
                            room.getSeatByAccountId(client.getAccountId());

                    if (seat == null) {

                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);

                    } else {

                        ICrashGameRoomPlayerInfo playerInfo = seat.getPlayerInfo();
                        getLog().debug("_handleBattle: seat.getPlayerInfo()={}, playerInfoService.playerInfo={}",
                                playerInfo, playerInfoService.get(client.getAccountId()));

                        String crashBetId = message.getCrashBetId();
                        Map<String, ICrashBetInfo> crashBets = seat.getCrashBets();

                        getLog().debug("_handleBattle: crashBetInfoMap before: {}, accountId: {} ", crashBets, accountId);

                        //not fatal error, this may be normal if round in Qualify state
                        if (!crashBets.containsKey(crashBetId)) {
                            sendErrorMessage(client, ErrorCodes.BET_NOT_FOUND, "Crash bet is not found in room", message.getRid());
                            return;
                        }

                        @SuppressWarnings("rawtypes")
                        IGameState gameState = room.getGameState();

                        int resultCode = gameState.processCancelCrashMultiplier(
                                seat.getAccountId(),
                                crashBetId,
                                message.getRid(),
                                message.isPlaceNewBet(),
                                message
                        );

                        if (resultCode != ErrorCodes.OK) {
                            //noinspection unchecked
                            if (message.isPlaceNewBet() && !gameState.isBuyInAllowed(seat)) {
                                sendErrorMessage(client, ErrorCodes.CANCEL_BET_NOT_ALLOWED, "Not allowed in play", message.getRid());
                            } else if (resultCode == ErrorCodes.BET_NOT_FOUND) {
                                sendErrorMessage(client, ErrorCodes.BET_NOT_FOUND, "Not allowed, round result already processing", message.getRid());
                            } else {
                                sendErrorMessage(client, ErrorCodes.BAD_STAKE, "Crash bet process with error", message.getRid());
                            }
                        } else {

                            getLog().debug("_handleBattle: crashBets after: {}, accountId: {} ", crashBets, accountId);

                            if(isPrivateRoom && room instanceof BattleAbstractCrashGameRoom) {
                                try {
                                    ((BattleAbstractCrashGameRoom)room).updatePlayersStatusNicknamesOnly
                                            (Arrays.asList(seat.getNickname()), Status.WAITING, false, true);

                                } catch (Exception e) {
                                    getLog().error("placeBattleBet: Exception to updatePlayersStatusAndSentToOwner, " +
                                            "{}", e.getMessage(), e);
                                }
                            }
                        }

                        playerInfoService.put(playerInfo);
                    }
                } finally {
                    playerInfoService.unlock(accountId);
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

