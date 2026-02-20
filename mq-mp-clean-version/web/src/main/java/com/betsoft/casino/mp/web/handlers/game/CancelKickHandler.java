package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.ICAFRoom;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.privateroom.Player;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.ICancelKickResponse;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.CancelKick;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for CancelKick request from private rooms.
 */
@Component
public class CancelKickHandler extends AbstractRoomHandler<CancelKick, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(CancelKickHandler.class);
    private final LobbySessionService lobbySessionService;
    private final SocketService socketService;
    public CancelKickHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                             MultiNodeRoomInfoService multiNodeRoomInfoService, RoomPlayerInfoService playerInfoService,
                             RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                             LobbySessionService lobbySessionService, SocketService socketService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
        this.socketService = socketService;
    }

    /**
     * Handles CancelKick message from private room owner. Checks if player is seater or not, cancel kick and send response to owner
     * @param session web socket session of owner
     * @param message CancelKick message
     * @param clientOwner game socket clientOwner of owner
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, CancelKick message, IGameSocketClient clientOwner) {

        getLog().debug("handle: message:{}, clientOwner:{}, session:{}", message, clientOwner, session);

        if (roomServiceFactory == null) {
            getLog().error("handle: roomServiceFactory is null, message: {}", message);
            sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "No room factory service identified", message.getRid());
            return;
        }

        LobbySession lobbySession = lobbySessionService.get(clientOwner.getSessionId());
        if (lobbySession == null) {
            getLog().error("handle: lobbySession is null, message: {}", message);
            sendErrorMessage(clientOwner, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }

        if (!clientOwner.isPrivateRoom()) {
            getLog().error("handle: it is not a privet room, message: {}", message);
            sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "For private rooms only", message.getRid());
            return;
        }

        if (!clientOwner.isOwner()) {
            getLog().error("handle: it is not a privet room owner, message: {}", message);
            sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Only owner (host) can CancelKick players", message.getRid());
            return;
        }

        String candidateNickname = message.getNickname();
        if(StringUtils.isTrimmedEmpty(candidateNickname)) {
            getLog().error("handle: Wrong candidateNickname, message: {}", message);
            sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Wrong nickname", message.getRid());
            return;
        }

        try {
            IRoom room = getRoomWithCheck(message.getRid(), clientOwner.getRoomId(), clientOwner, clientOwner.getGameType());

            if (room == null) {
                getLog().error("handle: No room identified, message: {}", message);
                sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "No room identified", message.getRid());
                return;
            }

            if (!room.getGameState().isAllowedKick()) {
                getLog().error("handle: Kick/CancelKick not allowed in this state, message: {}, gameState: {}", message, room.getGameState());
                sendErrorMessage(clientOwner, ErrorCodes.NOT_ALLOWED_KICK, "CancelKick not allowed in this state", message.getRid());
                return;
            }

            IRoomInfo roomInfo = room.getRoomInfo();
            if (roomInfo == null) {
                getLog().error("handle: No room info identified, message: {}", message);
                sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "No room info identified", message.getRid());
                return;
            }

            boolean isPrivateRoom = roomInfo.isPrivateRoom();

            if (!isPrivateRoom) {
                getLog().error("handle: Room info is not private, message: {}", message);
                sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Room info wrong type", message.getRid());
                return;
            }

            IRoomInfoService roomInfoService
                    = roomServiceFactory.getRoomInfoService(roomInfo.getGameType(), isPrivateRoom);

            if (!(roomInfoService instanceof IPrivateRoomInfoService)) {
                getLog().error("handle: Wrong room info service identified, message: {}", message);
                sendErrorMessage(clientOwner, ErrorCodes.BAD_REQUEST, "Wrong room info service identified", message.getRid());
                return;
            }

            getLog().debug("handle: updatePlayersStatusToInvited for candidateNickname: {}", candidateNickname);
            this.updatePlayersStatusToInvited(room, candidateNickname);

            getLog().debug("handle: sendCancelKickPlayerStatusToCanex for candidateNickname: {}", candidateNickname);
            this.sendCancelKickPlayerStatusToCanex(room, roomInfo, candidateNickname);

            Long accountId = null;

            IGameSocketClient candidateGameSocket = room.getObserver(candidateNickname);
            if (candidateGameSocket != null && candidateGameSocket.getAccountId() != null) {
                accountId = candidateGameSocket.getAccountId();
            }

            if (accountId == null) { //try to get it from PrivateRoomPlayerStatusService
                accountId = getAccountIdFromPrivateRoomPlayersStatus(room, candidateNickname);
            }

            if (accountId != null) {
                //accountId was found for candidateNickname, make a CancelKick process over RoomInfo Listener
                getLog().debug("handle: make a CancelKick process over RoomInfo Listener, accountId: {}", accountId);
                this.cancelKickPLayerOverRoomInfoListener(roomInfoService, roomInfo, accountId);
            }

            ICancelKickResponse response = room.getTOFactoryService()
                    .createCancelKickResponse(System.currentTimeMillis(), message.getRid());

            clientOwner.sendMessage(response);

        }
        catch (Exception e) {
            processUnexpectedError(clientOwner, message, e);
        }
    }

    private Long getAccountIdFromPrivateRoomPlayersStatus(IRoom room, String candidateNickname) {
        Long accountId = null;

        PrivateRoom privateRoom = null;

        if(room instanceof ICAFRoom) {
            privateRoom = ((ICAFRoom)room).getPrivateRoomPlayersStatus();
        } else {
            getLog().error("getAccountIdFromPrivateRoomPlayersStatus: the room is not ICAFRoom {}", room);
        }

        if(privateRoom == null) {
            getLog().debug("getAccountIdFromPrivateRoomPlayersStatus: privateRoom is null in room {}", room);
        } else {
            List<Player> players = privateRoom.getPlayers();
            if(players == null || players.isEmpty()) {
                getLog().debug("getAccountIdFromPrivateRoomPlayersStatus: privateRoom.getPlayers() is empty " +
                        "in privateRoom {}", privateRoom);
            } else {
                Player player = players.stream()
                        .filter(p -> p != null && !StringUtils.isTrimmedEmpty(p.getNickname())
                                && p.getNickname().equals(candidateNickname))
                        .findFirst()
                        .orElse(null);
                if(player == null) {
                    getLog().debug("getAccountIdFromPrivateRoomPlayersStatus: player is null for nickname {} " +
                                    "in privateRoom {}", candidateNickname, privateRoom);
                } else {
                    if(player.getAccountId() == 0) {
                        getLog().debug("getAccountIdFromPrivateRoomPlayersStatus: accountId is o for player {} " +
                                        "in privateRoom {}", player, privateRoom);
                    } else {
                        accountId =player.getAccountId();
                    }
                }
            }
        }

        return accountId;
    }

    private void sendCancelKickPlayerStatusToCanex(IRoom room, IRoomInfo roomInfo, String candidateNickname) {

        if(room == null) {
            getLog().error("sendCancelKickPlayerStatusToCanex: room is null for candidateNickname={}", candidateNickname);
            return;
        }

        if(!(room instanceof ICAFRoom)) {
            getLog().error("sendCancelKickPlayerStatusToCanex: the room is not ICAFRoom {}", room);
            return;
        }

        if(roomInfo == null) {
            getLog().error("sendCancelKickPlayerStatusToCanex: roomInfo is null for candidateNickname={}", candidateNickname);
            return;
        }

        try {

            Status tbgStatus = Status.INVITED;

            String privateRoomId = roomInfo.getPrivateRoomId();

            getLog().debug("sendCancelKickPlayerStatusToCanex: privateRoomId:{}, TBGStatus:{}, " +
                            "candidateNickname:{}", privateRoomId, tbgStatus, candidateNickname);

            if(StringUtils.isTrimmedEmpty(privateRoomId)) {
                getLog().error("sendCancelKickPlayerStatusToCanex: privateRoomId is empty, skip " +
                        "sendCancelKickPlayerStatusToCanex");
                return;
            }

            int bankId = (int)roomInfo.getBankId();

            ((ICAFRoom)room).sendPlayerStatusInPrivateRoomToCanex(privateRoomId, 0, bankId,
                    candidateNickname, null, null, tbgStatus);

        } catch (Exception e) {
            getLog().error("sendCancelKickPlayerStatusToCanex: Exception {}", e.getMessage(), e);
        }
    }

    private void cancelKickPLayerOverRoomInfoListener(IRoomInfoService roomInfoService, IRoomInfo roomInfo, long accountId) {
        getLog().debug("cancelKickPLayerOverRoomInfoListener: accountId: {}, roomInfo: {}", accountId, roomInfo);
        long roomId = roomInfo.getId();
        roomInfoService.lock(roomId);
        try {
            roomInfo.cancelKick(accountId);
            roomInfoService.update(roomInfo);
        } finally {
           roomInfoService.unlock(roomId);
        }
    }

    private void updatePlayersStatusToInvited(IRoom room, String candidateNickname) {

        if(!(room instanceof ICAFRoom)) {
            getLog().error("updatePlayersStatusToInvited: the room is not ICAFRoom {}", room);
            return;
        }

        Status status = Status.INVITED;
        try {
            getLog().debug("updatePlayersStatusToInvited: room.updatePlayersStatusAndSendToOwnerNicknamesOnly to {} for {} ",
                    status, candidateNickname);

            UpdatePrivateRoomResponse response = ((ICAFRoom)room)
                    .updatePlayersStatusNicknamesOnly(Arrays.asList(candidateNickname), status, false, true);

            getLog().debug("updatePlayersStatusToInvited: response {}", response);

            if (response == null) {
                getLog().error("updatePlayersStatusToInvited: response is null for {}", candidateNickname);
                return;
            }

            if (response.getPrivateRoom() == null) {
                getLog().error("updatePlayersStatusToInvited: response.getPrivateRoom() is null " +
                        "for {}", candidateNickname);
                return;
            }

            if (StringUtils.isTrimmedEmpty(response.getPrivateRoom().getPrivateRoomId())) {
                getLog().error("updatePlayersStatusToInvited: " +
                        "response.getPrivateRoom().getPrivateRoomId() is empty for {}", candidateNickname);
                return;
            }

            if (response.getPrivateRoom().getPlayers() == null
                    || response.getPrivateRoom().getPlayers().isEmpty()) {
                getLog().error("updatePlayersStatusToInvited: response.getPrivateRoom().getPlayers() " +
                        "is empty for {}", candidateNickname);
                return;
            }

        } catch (Exception e) {
            getLog().error("updatePlayersStatusToInvited: Exception to " +
                    "updatePlayersStatusAndSendToOwnerNicknamesOnly, {}", e.getMessage(), e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
