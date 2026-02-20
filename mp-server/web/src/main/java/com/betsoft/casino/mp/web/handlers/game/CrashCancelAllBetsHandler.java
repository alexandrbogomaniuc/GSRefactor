package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.Seat;
import com.betsoft.casino.mp.model.ICrashBetInfo;
import com.betsoft.casino.mp.model.ICrashGameRoomPlayerInfo;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.CrashBet;
import com.betsoft.casino.mp.transport.CrashCancelAllBets;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.RequestStatistic;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Arrays;
import java.util.Map;

/**
 * Handler for cancel all bets
 */
@Component
public class CrashCancelAllBetsHandler extends AbstractRoomHandler<CrashCancelAllBets, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(CrashCancelAllBetsHandler.class);

    private final LobbySessionService lobbySessionService;
    private final MultiNodeSeatService multiNodeSeatService;

    public CrashCancelAllBetsHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                     MultiNodeRoomInfoService multiNodeRoomInfoService,
                                     RoomPlayerInfoService playerInfoService,
                                     RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                                     LobbySessionService lobbySessionService,
                                     MultiNodeSeatService multiNodeSeatService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService,
                playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
        this.multiNodeSeatService = multiNodeSeatService;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, CrashCancelAllBets message, IGameSocketClient client) {
        Long accountId = client.getAccountId();
        if (client.getRoomId() == null || client.getSeatNumber() < 0 || accountId == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
            return;
        }
        getLog().debug("handle CrashCancelAllBets message: {}, accountId: {} ", message, accountId);

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
            if(requestStatistic != null && !isLimitApproved(message, requestStatistic, message.getCancelBetLimit())) {
                sendErrorMessage(client, ErrorCodes.REQUEST_FREQ_LIMIT_EXCEEDED, "Too many requests",
                        message.getRid());
                return;
            }
            _handleBattle(message, client, accountId);
        } else {
            _handleReal(message, client, accountId);
        }
    }

    public void _handleReal(CrashCancelAllBets message, IGameSocketClient client, Long accountId) {
        try {
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
                        ICrashGameRoomPlayerInfo playerInfo = (ICrashGameRoomPlayerInfo) seat.getPlayerInfo();
                        getLog().debug("seat.getPlayerInfo()={}, playerInfoService.playerInfo={}", playerInfo,
                                playerInfoService.get(client.getAccountId()));

                        Map<String, ICrashBetInfo> crashBets = seat.getCrashBets();
                        getLog().debug("crashBetInfoMap before: {}, accountId: {} ", crashBets, accountId);

                        for (Map.Entry<String, ICrashBetInfo> entry : crashBets.entrySet()) {
                            if (!entry.getValue().isEjected()) {
                                if (room.getGameState().processCancelCrashMultiplier(seat.getAccountId(), entry.getKey(), message.getRid(),
                                        false, message) == ErrorCodes.OK) {
                                    getLog().debug("crashBetInfoMap after cancel: {}, accountId: {} ", crashBets, accountId);
                                } else {
                                    sendErrorMessage(client, ErrorCodes.BAD_STAKE, "Crash bet process with error", message.getRid());
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

    public void _handleBattle(CrashCancelAllBets message, IGameSocketClient client, Long accountId) {
        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                if (hasPendingOperations(accountId, client, message)) {
                    return;
                }

                playerInfoService.lock(accountId);
                try {
                    com.betsoft.casino.mp.maxblastchampions.model.Seat seat = (com.betsoft.casino.mp.maxblastchampions.model.Seat)
                            room.getSeatByAccountId(client.getAccountId());
                    if (seat == null) {
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                    } else {
                        ICrashGameRoomPlayerInfo playerInfo = (ICrashGameRoomPlayerInfo) seat.getPlayerInfo();
                        getLog().debug("_handleBattle: seat.getPlayerInfo()={}, playerInfoService.playerInfo={}", playerInfo,
                                playerInfoService.get(client.getAccountId()));

                        Map<String, ICrashBetInfo> crashBets = seat.getCrashBets();
                        getLog().debug("_handleBattle: crashBetInfoMap before: {}, accountId: {} ", crashBets, accountId);

                        for (Map.Entry<String, ICrashBetInfo> entry : crashBets.entrySet()) {
                            if (!entry.getValue().isEjected()) {
                                if (room.getGameState().processCancelCrashMultiplier(seat.getAccountId(), entry.getKey(), message.getRid(),
                                        false, message) == ErrorCodes.OK) {
                                    getLog().debug("_handleBattle: crashBetInfoMap after cancel: {}, accountId: {} ", crashBets, accountId);

                                    IRoomInfo roomInfo = room.getRoomInfo();

                                    if(roomInfo != null && roomInfo.isPrivateRoom() && room instanceof BattleAbstractCrashGameRoom) {
                                        try {

                                            ((BattleAbstractCrashGameRoom)room)
                                                    .updatePlayersStatusNicknamesOnly(Arrays.asList(seat.getNickname()), Status.WAITING, false, true);

                                        } catch (Exception e) {
                                            getLog().error("_handleBattle: Exception to updatePlayersStatusAndSentToOwner, " +
                                                    "{}", e.getMessage(), e);
                                        }
                                    }

                                } else {
                                    sendErrorMessage(client, ErrorCodes.BAD_STAKE, "Crash bet process with error", message.getRid());
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
