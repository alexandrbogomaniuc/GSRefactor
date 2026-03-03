package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.model.ICAFRoom;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.abs.casino.common.util.string.StringUtils;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.Arrays;

@SpringAware
public class SitOutTask implements Runnable, Serializable, ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(SitOutTask.class);
    private final long roomId;
    private final long accountId;
    private final int seatNumber;

    private transient ApplicationContext context;

    public SitOutTask(long roomId, long accountId, int seatNumber) {
        this.roomId = roomId;
        this.accountId = accountId;
        this.seatNumber = seatNumber;
    }

    @Override
    public void run() {
        if (context != null) {
            LOG.debug("run: {}", this);

            try {
                RoomServiceFactory roomServiceFactory =
                        context.getBean("roomServiceFactory", RoomServiceFactory.class);

                IRoomInfo roomInfo = roomServiceFactory.getRoomInfo(roomId);

                if (roomInfo != null) {
                    @SuppressWarnings("rawtypes")

                    IRoom room = roomServiceFactory.getRoomWithoutCreation(roomInfo.getGameType(), roomId);

                    if (room != null) {

                        String candidateNickname = null;
                        ISeat seat = room.getSeatByAccountId(accountId);
                        if(seat != null) {
                            LOG.debug("SitOutTask: for accountId: {}, seat identified {}", accountId, seat);
                            candidateNickname = seat.getNickname();
                        } else {
                            LOG.debug("SitOutTask: for accountId: {}, seat is null", accountId);
                        }

                        room.processSitOut(null, null, seatNumber, accountId, true);
                        room.processCloseRoom(accountId);
                        LOG.debug("SitOutTask remove roomId: {}, accountId: {}, seatNumber: {}",
                                roomId, accountId, seatNumber);

                        if(room.getRoomInfo() != null && room.getRoomInfo().isPrivateRoom()) {

                            if(StringUtils.isTrimmedEmpty(candidateNickname)) {
                                LOG.debug("SitOutTask cant identify nickname for accountId: {}, roomId: {}",
                                         accountId, roomId);
                            } else {
                                LOG.debug("SitOutTask: updatePlayersStatusToWaiting for candidateNickname: {}", candidateNickname);
                                this.updatePlayersStatusToWaiting(room, candidateNickname);

                                LOG.debug("SitOutTask: sendWaitingPlayerStatusToCanex for candidateNickname: {}", candidateNickname);
                                this.sendWaitingPlayerStatusToCanex(room, roomInfo, candidateNickname);
                            }
                        }

                    } else {
                        LOG.debug("SitOutTask not found roomId: {}, just exit", roomId);
                    }

                } else {
                    LOG.debug("SitOutTask not found roomInfo: {}, just exit", roomId);
                }
            } catch (com.abs.casino.common.exception.CommonException e) {
                LOG.debug("Cannot sitOut player: {}, roomId: {}", accountId, roomId, e);
            } catch (Exception e) {
                LOG.debug("Cannot sitOut player: {}, roomId: {}", accountId, roomId, e);
            }
        } else {
            LOG.error("ApplicationContext not found, {}", this);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    private void updatePlayersStatusToWaiting(IRoom room, String candidateNickname) {

        if(!(room instanceof ICAFRoom)) {
            LOG.error("updatePlayersStatusToWaiting: the room is not ICAFRoom {}", room);
            return;
        }

        Status status = Status.WAITING;
        try {
            LOG.debug("updatePlayersStatusToWaiting: room.updatePlayersStatusNicknamesOnly to {} for {} ",
                    status , candidateNickname);

            UpdatePrivateRoomResponse response = ((ICAFRoom)room)
                    .updatePlayersStatusNicknamesOnly(Arrays.asList(candidateNickname), status, false, true);

            LOG.debug("updatePlayersStatusToWaiting: response {}", response);

            if (response == null) {
                LOG.error("updatePlayersStatusToWaiting: response is null for {}", candidateNickname);
                return;
            }

            if (response.getPrivateRoom() == null) {
                LOG.error("updatePlayersStatusToWaiting: response.getPrivateRoom() is null " +
                        "for {}", candidateNickname);
                return;
            }

            if (StringUtils.isTrimmedEmpty(response.getPrivateRoom().getPrivateRoomId())) {
                LOG.error("updatePlayersStatusToWaiting: " +
                        "response.getPrivateRoom().getPrivateRoomId() is empty for {}", candidateNickname);
                return;
            }

            if (response.getPrivateRoom().getPlayers() == null || response.getPrivateRoom().getPlayers().isEmpty()) {
                LOG.error("updatePlayersStatusToWaiting: response.getPrivateRoom().getPlayers() " +
                        "is empty for {}", candidateNickname);
                return;
            }

        } catch (Exception e) {
            LOG.error("updatePlayersStatusToWaiting: Exception to " +
                    "updatePlayersStatusNicknamesOnly, {}", e.getMessage(), e);
        }
    }

    private void sendWaitingPlayerStatusToCanex(IRoom room, IRoomInfo roomInfo, String candidateNickname) {

        if(room == null) {
            LOG.error("sendWaitingPlayerStatusToCanex: room is null for candidateNickname={}", candidateNickname);
            return;
        }

        if(!(room instanceof ICAFRoom)) {
            LOG.error("sendWaitingPlayerStatusToCanex: the room is not ICAFRoom {}", room);
            return;
        }

        if(roomInfo == null) {
            LOG.error("sendWaitingPlayerStatusToCanex: roomInfo is null for candidateNickname={}", candidateNickname);
            return;
        }

        try {

            Status tbgStatus = Status.WAITING;

            String privateRoomId = roomInfo.getPrivateRoomId();

            LOG.debug("sendWaitingPlayerStatusToCanex: privateRoomId:{}, TBGStatus:{}, " +
                    "candidateNickname:{}", privateRoomId, tbgStatus, candidateNickname);

            if(StringUtils.isTrimmedEmpty(privateRoomId)) {
                LOG.error("sendWaitingPlayerStatusToCanex: privateRoomId is empty, skip sendWaitingPlayerStatusToCanex");
                return;
            }

            int bankId = (int)roomInfo.getBankId();

            ((ICAFRoom)room).sendPlayerStatusInPrivateRoomToCanex(privateRoomId, 0, bankId,
                    candidateNickname, null, null, tbgStatus);

        } catch (Exception e) {
            LOG.error("sendWaitingPlayerStatusToCanex: Exception {}", e.getMessage(), e);
        }
    }
    @Override
    public String toString() {
        return "SitOutTask [" + "roomId=" + roomId +
                ", accountId=" + accountId +
                ", seatNumber=" + seatNumber +
                ']';
    }
}
