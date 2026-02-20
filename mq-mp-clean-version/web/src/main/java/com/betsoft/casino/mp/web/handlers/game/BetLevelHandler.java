package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.ISingleNodeSeat;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.BetLevel;
import com.betsoft.casino.mp.transport.BetLevelResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Set;

import static com.betsoft.casino.utils.TObject.SERVER_RID;

@Component
public class BetLevelHandler extends AbstractRoomHandler<BetLevel, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(BetLevelHandler.class);
    private final SocketService socketService;
    protected final LobbySessionService lobbySessionService;

    public BetLevelHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                           MultiNodeRoomInfoService multiNodeRoomInfoService,
                           RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                           SocketService socketService,
                           ServerConfigService serverConfigService,
                           LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
    }

    @Override
    public void handle(WebSocketSession session, BetLevel message, IGameSocketClient client) {
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

        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                if (hasPendingOperations(accountId, client, message)) {
                    return;
                }
                playerInfoService.lock(accountId);
                getLog().debug("handle HS lock: {}", accountId);
                try {
                    ISingleNodeSeat seat = (ISingleNodeSeat) room.getSeat(client.getSeatNumber());
                    if (seat == null || seat.getAccountId() != accountId) {
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                    } else {
                        MoneyType moneyType = lobbySession.getMoneyType();
                        if (moneyType.equals(MoneyType.FRB)) {
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                    "1 Not allowed change bet level", message.getRid(), message);
                            return;
                        }

                        if (room.getRoomInfo().getGameType().isBattleGroundGame()) {
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                    " Not allowed change bet level for BG games", message.getRid(), message);
                            return;
                        }

                        if (GameType.REVENGE_OF_RA.equals(room.getRoomInfo().getGameType())) {
                            Set<Integer> possibleBetLevels = com.betsoft.casino.mp.revengeofra.model.math
                                    .MathData.getPossibleBetLevels();
                            if (!possibleBetLevels.contains(message.getBetLevel())) {
                                sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                        "2 Not allowed change bet level 1", message.getRid(), message);
                                return;
                            }
                        } else if (GameType.DRAGONSTONE.equals(room.getGameType())) {
                            Set<Integer> possibleBetLevels = com.betsoft.casino.mp.dragonstone.model.math
                                    .MathData.getPossibleBetLevels();
                            if (!possibleBetLevels.contains(message.getBetLevel())) {
                                sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                        "2 Not allowed change bet level 1", message.getRid(), message);
                                return;
                            }
                        } else if (GameType.CLASH_OF_THE_GODS.equals(room.getGameType())) {
                            Set<Integer> possibleBetLevels = com.betsoft.casino.mp.clashofthegods.model.math
                                    .MathData.getPossibleBetLevels();
                            if (!possibleBetLevels.contains(message.getBetLevel())) {
                                sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                        "2 Not allowed change bet level 1", message.getRid(), message);
                                return;
                            }
                        } else if (GameType.PIRATES_POV.equals(room.getGameType())) {
                            Set<Integer> possibleBetLevels = com.betsoft.casino.mp.piratescommon.model.math
                                    .MathData.getPossibleBetLevels();
                            if (!possibleBetLevels.contains(message.getBetLevel())) {
                                sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                        "2 Not allowed change bet level 1", message.getRid(), message);
                                return;
                            }
                        } else if (GameType.DMC_PIRATES.equals(room.getGameType())) {
                            Set<Integer> possibleBetLevels = com.betsoft.casino.mp.piratescommon.model.math.MathData.getPossibleBetLevels();
                            if (!possibleBetLevels.contains(message.getBetLevel())) {
                                sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                        "2 Not allowed change bet level 1", message.getRid(), message);
                                return;
                            }
                        } else if (GameType.MISSION_AMAZON.equals(room.getGameType())) {
                            Set<Integer> possibleBetLevels = com.betsoft.casino.mp.missionamazon.model.math
                                    .MathData.getPossibleBetLevels();
                            if (!possibleBetLevels.contains(message.getBetLevel())) {
                                sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                        "2 Not allowed change bet level 1", message.getRid(), message);
                                return;
                            }
                        } else if (GameType.SECTOR_X.equals(room.getGameType())) {
                            Set<Integer> possibleBetLevels = com.betsoft.casino.mp.sectorx.model.math
                                    .MathData.getPossibleBetLevels();
                            if (!possibleBetLevels.contains(message.getBetLevel())) {
                                sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                        "2 Not allowed change bet level 1", message.getRid(), message);
                                return;
                            }
                        } else {
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                    "3 Not allowed change bet level", message.getRid(), message);
                            return;
                        }

                        boolean isSuccess = room.tryChangeBetLevel(seat.getAccountId(), message.getBetLevel());
                        if (!isSuccess) {
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL,
                                    "Not allowed change bet level isSuccess is false", message.getRid(), message);
                        } else {
                            room.sendChanges(new BetLevelResponse(System.currentTimeMillis(), SERVER_RID,
                                            message.getBetLevel(), seat.getNumber()),
                                    new BetLevelResponse(System.currentTimeMillis(), message.getRid(),
                                            message.getBetLevel(), seat.getNumber()),
                                    accountId,
                                    message);
                        }
                    }
                } finally {
                    playerInfoService.unlock(accountId);
                    getLog().debug("handle HS unlock: {}", accountId);
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
