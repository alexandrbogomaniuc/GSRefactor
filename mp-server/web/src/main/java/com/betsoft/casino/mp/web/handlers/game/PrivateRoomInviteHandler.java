package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.common.AbstractBattlegroundGameRoom;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.PrivateRoomInvite;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.List;

/**
 * Handler for PrivateRoomInvite request from private rooms.
 */
@Component
public class PrivateRoomInviteHandler extends AbstractRoomHandler<PrivateRoomInvite, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(PrivateRoomInviteHandler.class);
    private final LobbySessionService lobbySessionService;
    private final SocketService socketService;

    public PrivateRoomInviteHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                    MultiNodeRoomInfoService multiNodeRoomInfoService, RoomPlayerInfoService playerInfoService,
                                    RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                                    LobbySessionService lobbySessionService, SocketService socketService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
        this.socketService = socketService;
    }

    /**
     * Handles PrivateRoomInviteRequest message from private room owner. Checks if player is seater or not, do invite and send response to owner
     *
     * @param session     web socket session of owner
     * @param message     PrivateRoomInviteRequest message
     * @param clientOwner game socket clientOwner of owner
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, PrivateRoomInvite message, IGameSocketClient clientOwner) {

        getLog().debug("handle: message:{}, clientOwner:{}, session:{}", message, clientOwner, session);

        if (roomServiceFactory == null) {
            sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "No room factory service identified", message.getRid());
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(clientOwner.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(clientOwner, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }

        if (!clientOwner.isPrivateRoom()) {
            sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "For private rooms only", message.getRid());
            return;
        }

        if (!clientOwner.isOwner()) {
            sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Only owner (host) can do private room invite", message.getRid());
            return;
        }

        List<String> nicknames = message.getNicknames();

        if (nicknames != null && nicknames.isEmpty()) {
            sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Wrong nickname", message.getRid());
            return;
        }

        try {

            IRoom room = getRoomWithCheck(message.getRid(), clientOwner.getRoomId(), clientOwner, clientOwner.getGameType());
            if (room == null) {
                sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "No room identified", message.getRid());
                return;
            }

            IRoomInfo roomInfo = room.getRoomInfo();
            if (roomInfo == null) {
                sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "No room info identified", message.getRid());
                return;
            }

            boolean isPrivateRoom = roomInfo.isPrivateRoom();

            if (!isPrivateRoom) {
                sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Room info wrong type", message.getRid());
                return;
            }

            boolean successful = false;

            if(room instanceof AbstractBattlegroundGameRoom) {

                IRoomInfoService bgPrivateRoomInfoService
                        = roomServiceFactory.getRoomInfoService(roomInfo.getGameType(), isPrivateRoom);

                if (!(bgPrivateRoomInfoService instanceof BGPrivateRoomInfoService)) {
                    sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Wrong room info service identified", message.getRid());
                    return;
                }

                successful = ((AbstractBattlegroundGameRoom)room).invitePlayersToPrivateRoomAtCanex(nicknames);

            } else if(room instanceof BattleAbstractCrashGameRoom) {

                IRoomInfoService multiNodePrivateRoomInfoService
                        = roomServiceFactory.getRoomInfoService(roomInfo.getGameType(), isPrivateRoom);

                if (!(multiNodePrivateRoomInfoService instanceof MultiNodePrivateRoomInfoService)) {
                    sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Wrong room info service identified", message.getRid());
                    return;
                }

                successful = ((BattleAbstractCrashGameRoom)room).invitePlayersToPrivateRoomAtCanex(nicknames);

            }

            clientOwner.sendMessage(room.getTOFactoryService()
                    .createPrivateRoomInviteResponse(System.currentTimeMillis(), message.getRid(), successful));

        } catch (Exception e) {
            processUnexpectedError(clientOwner, message, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
