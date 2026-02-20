package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.AbstractBattlegroundWaitingPlayersGameState;
import com.betsoft.casino.mp.common.AbstractBattlegroundGameRoom;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.Ok;
import com.betsoft.casino.mp.transport.StartBattlegroundPrivateRoom;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.StartBGPrivateRoomRoundTask;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class StartBattlegroundPrivateRoomHandler extends AbstractRoomHandler<StartBattlegroundPrivateRoom, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(StartBattlegroundPrivateRoomHandler.class);

    private final BGPrivateRoomInfoService bgPrivateRoomInfoService;
    private final MultiNodePrivateRoomInfoService  multiNodePrivateRoomInfoService;
    private final LobbySessionService lobbySessionService;

    public StartBattlegroundPrivateRoomHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                               MultiNodeRoomInfoService multiNodeRoomInfoService, RoomPlayerInfoService playerInfoService,
                                               RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                                               BGPrivateRoomInfoService bgPrivateRoomInfoService,
                                               MultiNodePrivateRoomInfoService  multiNodePrivateRoomInfoService,
                                               LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.bgPrivateRoomInfoService = bgPrivateRoomInfoService;
        this.multiNodePrivateRoomInfoService = multiNodePrivateRoomInfoService;
        this.lobbySessionService = lobbySessionService;
    }

    @Override
    public void handle(WebSocketSession session, StartBattlegroundPrivateRoom message, IGameSocketClient client) {

        if (client.getRoomId() == null || client.getAccountId() == null) {
            getLog().error("handle: Room not open: {}", message);
            sendErrorMessage(client, ErrorCodes.ROOM_NOT_OPEN, "Room not open", message.getRid(), message);
            return;
        }

        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            getLog().error("handle: Session not found: {}", message);
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }


        boolean isBattlegroundAllowed = lobbySession.isBattlegroundAllowed();
        if (!isBattlegroundAllowed) {
            getLog().error("handle: Battleground mode not allowed: {}", message);
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Battleground mode not allowed", message.getRid());
            return;
        }

        try {
            boolean hasPendingOperations = hasPendingOperations(client.getAccountId(), client, message);
            if (hasPendingOperations) {
                getLog().error("handle: Has pending operations: {}", message);
                sendErrorMessage(client, ErrorCodes.FOUND_PENDING_OPERATION, "Has pending operations", message.getRid());
                return;
            }

            playerInfoService.lock(client.getAccountId());
            getLog().debug("handle: handle HS lock: {}", client.getAccountId());

            try {
                IRoom room = roomServiceFactory.getRoomWithoutCreationById(client.getRoomId());

                if (room == null) {
                    getLog().error("handle: Room not found: {}", message);
                    sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_START_ROUND, "Room not found", message.getRid());
                    return;
                }

                boolean isConfirmBattlegroundBuyIn = lobbySession.isConfirmBattlegroundBuyIn();

                if(room instanceof BattleAbstractCrashGameRoom) {
                    com.betsoft.casino.mp.maxblastchampions.model.Seat seat =
                            (com.betsoft.casino.mp.maxblastchampions.model.Seat)
                                    room.getSeatByAccountId(client.getAccountId());

                    if (seat == null) {
                        getLog().error("handle: Not seat: {}", message);
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                        return;
                    }

                    Map<String, ICrashBetInfo> crashBetInfoMap = seat.getCrashBets();
                    isConfirmBattlegroundBuyIn = (crashBetInfoMap != null && crashBetInfoMap.size() > 0);
                }

                if (!isConfirmBattlegroundBuyIn) {
                    getLog().error("handle: Battleground BuyIn not confirmed: {}", message);
                    sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_START_ROUND, "Battleground BuyIn not confirmed", message.getRid());
                    return;
                }

                boolean isOwner = lobbySession.isOwner();
                if (!isOwner) {
                    getLog().error("handle: Battleground could be started only by host: {}", message);
                    sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_START_ROUND, "Battleground could be started only by host", message.getRid());
                    return;
                }

                short realConfirmedSeatsCount = room.getRealConfirmedSeatsCount();
                if (realConfirmedSeatsCount < room.getMinSeats()) {
                    getLog().error("handle: Not enough seaters to start the round: {}, realConfirmedSeatsCount={}, " +
                            "room.getMinSeats()={}", message, realConfirmedSeatsCount, room.getMinSeats());
                    sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_START_ROUND, "Not enough seaters to start the round", message.getRid());
                    return;
                }

                boolean hasNotReadyNotKickedSeat = room.hasNotReadyNotKickedSeat();

                IGameState gameState = room.getGameState();

                getLog().debug("handle: message={}; realConfirmedSeatsCount={}, room.getMinSeats()={}, hasNotReadyNotKickedSeat={}; gameState={}",
                        message, realConfirmedSeatsCount, room.getMinSeats(), hasNotReadyNotKickedSeat, gameState);

                if (gameState instanceof AbstractBattlegroundWaitingPlayersGameState) {

                    if (hasNotReadyNotKickedSeat) {
                        getLog().error("handle: here are not ready players in a room: {}", message);
                        sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_START_ROUND, "There are not ready players in a room", message.getRid());
                        return;
                    }

                    AbstractBattlegroundWaitingPlayersGameState castedGameState =
                            (AbstractBattlegroundWaitingPlayersGameState) gameState;
                    castedGameState.setTimeToStart(System.currentTimeMillis());

                } else if (gameState instanceof com.betsoft.casino.mp.maxblastchampions.model.WaitingPlayersGameState
                            && room instanceof BattleAbstractCrashGameRoom) {

                    StartBGPrivateRoomRoundTask startBGPrivateRoomRoundTask =
                            new StartBGPrivateRoomRoundTask(room.getId());

                    playerInfoService
                            .getNotifyService()
                            .executeOnAllMembers(startBGPrivateRoomRoundTask);

                } else {
                    getLog().error("handle: Battleground could be started only in waiting state: {}, gameState={}", message, gameState);
                    sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_START_ROUND, "Battleground could be started only in waiting state", message.getRid());
                    return;
                }

                client.sendMessage(new Ok(System.currentTimeMillis(), message.getRid()), message);

                if(room.getRoomInfo() != null && room.getRoomInfo().isPrivateRoom() && (room instanceof ICAFRoom)) {

                    List<String> playingNicknames = new ArrayList<>();

                    List<ISeat> seats = null;

                    if (room instanceof AbstractBattlegroundGameRoom) {
                        seats = room.getSeats();
                    } else if (room instanceof BattleAbstractCrashGameRoom) {
                        seats = room.getRealSeats();
                    }

                    if(seats != null) {
                        for (ISeat seat : seats) {
                            if (seat != null && !StringUtils.isTrimmedEmpty(seat.getNickname())) {
                                playingNicknames.add(seat.getNickname());
                            }
                        }
                    }

                    if (!playingNicknames.isEmpty()) {
                        try {
                            ((ICAFRoom) room)
                                    .updatePlayersStatusNicknamesOnly(playingNicknames, Status.PLAYING, false, true);
                        } catch (Exception e) {
                            getLog().error("handle: Exception to " +
                                    "updatePlayersStatusAndSentToOwner, {}", e.getMessage(), e);
                        }
                    }
                }

            } finally {
                playerInfoService.unlock(client.getAccountId());
                getLog().debug("handle: handle HS unlock: {}", client.getAccountId());
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
