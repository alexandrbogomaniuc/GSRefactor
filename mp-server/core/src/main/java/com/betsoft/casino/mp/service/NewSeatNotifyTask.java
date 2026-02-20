package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IMultiNodeRoom;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 24.08.18.
 */
@SpringAware
public class NewSeatNotifyTask implements Runnable, Serializable, ApplicationContextAware {
    private long roomId;
    private long serverId;
    private boolean singleNodeRoom;
    private ISeat seat;
    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(NewSeatNotifyTask.class);

    public NewSeatNotifyTask(long roomId, long serverId, boolean singleNodeRoom, ISeat seat) {
        this.roomId = roomId;
        this.serverId = serverId;
        this.singleNodeRoom = singleNodeRoom;
        this.seat = seat;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void run() {
        IRoomInfoService roomInfoService = singleNodeRoom ?
                context.getBean("singleNodeRoomInfoService", SingleNodeRoomInfoService.class) :
                context.getBean("multiNodeRoomInfoService", MultiNodeRoomInfoService.class);
        LOG.debug("run: roomId={}, serverId={}, singleNodeRoom={}, seat={}", roomId, serverId, singleNodeRoom, seat);
        if (context == null) {
            LOG.error("ApplicationContext not found, roomId={}", roomId);
            return;
        }
        roomInfoService.seatAdded(roomId);
        if (!singleNodeRoom) {
            IServerConfigService serverConfigService = (IServerConfigService) context.getBean("serverConfigService");
            if (serverConfigService.getServerId() != serverId) {
                IRoomServiceFactory roomServiceFactory = (IRoomServiceFactory) context.getBean("roomServiceFactory");
                try {
                    IMultiNodeRoom room = (IMultiNodeRoom) roomServiceFactory.getRoomWithoutCreationById(roomId);
                    if (room != null) {
                        //noinspection unchecked
                        room.addSeatFromOtherServer(seat);
                    } else {
                        LOG.error("Room not found, id={}", roomId);
                    }
                } catch (Exception e) {
                    LOG.error("Cannot notify about seat", e);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NewSeatNotifyTask [");
        sb.append("roomId=").append(roomId);
        sb.append(", serverId=").append(serverId);
        sb.append(", singleNodeRoom=").append(singleNodeRoom);
        sb.append(", seat=").append(seat);
        sb.append(']');
        return sb.toString();
    }
}
