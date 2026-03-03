package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.ICAFRoom;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.ISingleNodeSeat;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.SitOut;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Arrays;

/**
 * User: flsh
 * Date: 03.11.17.
 */
@Component
public class SitOutHandler extends AbstractRoomHandler<SitOut, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(SitOutHandler.class);

    public SitOutHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                         MultiNodeRoomInfoService multiNodeRoomInfoService,
                         RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                         ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, SitOut message, IGameSocketClient client) {
        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                ISeat seat = room.getSeatByAccountId(client.getAccountId());
                if (seat != null && seat.getAccountId() == client.getAccountId()) {
                    if (!room.isBattlegroundMode() && hasPendingOperations(seat.getAccountId(), client, message)) {
                        getLog().debug("SitOutHandler, found pending operation for accountId: {} message: {}", seat, message);
                        return;
                    }
                    seat.setWantSitOut(true);
                    int seatNumber = seat instanceof ISingleNodeSeat ? ((ISingleNodeSeat) seat).getNumber() : 0;
                    try {
                        room.processSitOut(client, message, seatNumber, seat.getAccountId(), true);

                        String candidateNickname = seat.getNickname();
                        if(StringUtils.isTrimmedEmpty(candidateNickname)) {
                            getLog().error("handle: candidateNickname is empty, seat: {}", seat);
                            return;
                        }

                        IRoomInfo roomInfo = room.getRoomInfo();
                        if (roomInfo == null) {
                            getLog().error("handle: No room info identified, message: {}", message);
                            return;
                        }

                        boolean isPrivateRoom = roomInfo.isPrivateRoom();

                        if (!isPrivateRoom) {
                            getLog().debug("handle: Room info is not private, message: {}", message);
                            return;
                        }

                        IRoomInfoService roomInfoService
                                = roomServiceFactory.getRoomInfoService(roomInfo.getGameType(), isPrivateRoom);

                        if(!(roomInfoService instanceof IPrivateRoomInfoService)) {
                            getLog().error("handle: room info service is not IPrivateRoomInfoService, message: {}", message);
                            return;
                        }

                        getLog().debug("handle: updatePlayersStatusToWaiting for candidateNickname: {}", candidateNickname);
                        this.updatePlayersStatusToWaiting(room, candidateNickname);

                        getLog().debug("handle: sendWaitingPlayerStatusToCanex for candidateNickname: {}", candidateNickname);
                        this.sendWaitingPlayerStatusToCanex(room, roomInfo, candidateNickname);

                    } catch (com.abs.casino.common.exception.CommonException e) {
                        getLog().error("Cannot sitOut, seat={}, room={}", seat, room, e);
                    }
                } else {
                    client.sendMessage(new Error(ErrorCodes.NOT_SEATER, "Seat not found",
                            System.currentTimeMillis(), message.getRid()), message);
                }
            } else{
                getLog().debug("SitOutHandler, room not found message: {}", message);
            }
        } catch (CommonException e) {
            processUnexpectedError(client, message, e);
        }
    }

    private void updatePlayersStatusToWaiting(IRoom room, String candidateNickname) {

        if(!(room instanceof ICAFRoom)) {
            getLog().error("updatePlayersStatusToWaiting: the room is not ICAFRoom {}", room);
            return;
        }

        Status status = Status.WAITING;
        try {
            getLog().debug("updatePlayersStatusToWaiting: room.updatePlayersStatusNicknamesOnly to {} for {} ",
                    status , candidateNickname);

            UpdatePrivateRoomResponse response = ((ICAFRoom)room)
                    .updatePlayersStatusNicknamesOnly(Arrays.asList(candidateNickname), status, false, true);

            getLog().debug("updatePlayersStatusToWaiting: response {}", response);

            if (response == null) {
                getLog().error("updatePlayersStatusToWaiting: response is null for {}", candidateNickname);
                return;
            }

            if (response.getPrivateRoom() == null) {
                getLog().error("updatePlayersStatusToWaiting: response.getPrivateRoom() is null " +
                        "for {}", candidateNickname);
                return;
            }

            if (StringUtils.isTrimmedEmpty(response.getPrivateRoom().getPrivateRoomId())) {
                getLog().error("updatePlayersStatusToWaiting: " +
                        "response.getPrivateRoom().getPrivateRoomId() is empty for {}", candidateNickname);
                return;
            }

            if (response.getPrivateRoom().getPlayers() == null || response.getPrivateRoom().getPlayers().isEmpty()) {
                getLog().error("updatePlayersStatusToWaiting: response.getPrivateRoom().getPlayers() " +
                        "is empty for {}", candidateNickname);
                return;
            }

        } catch (Exception e) {
            getLog().error("updatePlayersStatusToWaiting: Exception to " +
                    "updatePlayersStatusNicknamesOnly, {}", e.getMessage(), e);
        }
    }

    private void sendWaitingPlayerStatusToCanex(IRoom room, IRoomInfo roomInfo, String candidateNickname) {

        if(room == null) {
            getLog().error("sendWaitingPlayerStatusToCanex: room is null for candidateNickname={}", candidateNickname);
            return;
        }

        if(!(room instanceof ICAFRoom)) {
            getLog().error("sendWaitingPlayerStatusToCanex: the room is not ICAFRoom {}", room);
            return;
        }

        if(roomInfo == null) {
            getLog().error("sendWaitingPlayerStatusToCanex: roomInfo is null for candidateNickname={}", candidateNickname);
            return;
        }

        try {

            Status tbgStatus = Status.WAITING;

            String privateRoomId = roomInfo.getPrivateRoomId();

            getLog().debug("sendWaitingPlayerStatusToCanex: privateRoomId:{}, TBGStatus:{}, " +
                    "candidateNickname:{}", privateRoomId, tbgStatus, candidateNickname);

            if(StringUtils.isTrimmedEmpty(privateRoomId)) {
                getLog().error("sendWaitingPlayerStatusToCanex: privateRoomId is empty, skip sendWaitingPlayerStatusToCanex");
                return;
            }

            int bankId = (int)roomInfo.getBankId();

            ((ICAFRoom)room).sendPlayerStatusInPrivateRoomToCanex(privateRoomId, 0, bankId,
                    candidateNickname, null, null, tbgStatus);

        } catch (Exception e) {
            getLog().error("sendWaitingPlayerStatusToCanex: Exception {}", e.getMessage(), e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
