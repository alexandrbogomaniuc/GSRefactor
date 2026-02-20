package com.betsoft.casino.mp.service;

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
public class SeatRemovedNotifyTask implements Runnable, Serializable, ApplicationContextAware {
    private long roomId;
    private boolean singleNodeRoom;
    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(SeatRemovedNotifyTask.class);

    public SeatRemovedNotifyTask(long roomId, boolean singleNodeRoom) {
        this.roomId = roomId;
        this.singleNodeRoom = singleNodeRoom;
    }

    @Override
    public void run() {
        if (context != null) {
            @SuppressWarnings("rawtypes")
            IRoomInfoService roomInfoService = singleNodeRoom ?
                    context.getBean("singleNodeRoomInfoService", SingleNodeRoomInfoService.class) :
                    context.getBean("multiNodeRoomInfoService", MultiNodeRoomInfoService.class);
            roomInfoService.seatRemoved(roomId);
        } else {
            LOG.error("ApplicationContext not found, roomId={}", roomId);
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
        sb.append(']');
        return sb.toString();
    }

}
