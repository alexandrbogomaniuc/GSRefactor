package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IRoundResult;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.utils.ITransportObject;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

@SpringAware
public class SendSeatOwnerMessageTask implements Runnable, Serializable, ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(SendSeatsMessageTask.class);
    private long roomId;
    private GameType gameType;
    private long senderServerId;
    private ITransportObject message;
    private transient ApplicationContext context;

    public SendSeatOwnerMessageTask(long roomId, GameType gameType, long senderServerId, ITransportObject message) {
        this.roomId = roomId;
        this.gameType = gameType;
        this.senderServerId = senderServerId;
        this.message = message;
    }

    @Override
    public void run() {
        LOG.debug("Run task={}", LOG.isDebugEnabled() ? toString() : "");
        if (context == null) {
            LOG.error("ApplicationContext not found, roomId={}", roomId);
            return;
        }
        @SuppressWarnings("rawtypes")
        IServerConfigService serverConfigService = (IServerConfigService) context.getBean("serverConfigService");
        //don't send message for server created this task
        if (serverConfigService.getServerId() == senderServerId) {
            return;
        }
        try {
            IRoomServiceFactory roomServiceFactory = (IRoomServiceFactory) context.getBean("roomServiceFactory");
            @SuppressWarnings("rawtypes")
            IRoom room = roomServiceFactory.getRoomWithoutCreation(gameType, roomId);
            if (room == null) {
                LOG.warn("Room not found, message={}", LOG.isWarnEnabled() ? toString() : "");
                return;
            }

            List<ISeat<?, ?, ?, ?, ?>> seats = room.getAllSeats();
            if(seats != null && seats.size() > 0) {
                for(ISeat<?, ?, ?, ?, ?> seat : seats) {
                    if (seat != null && seat.isOwner()) {
                        seat.sendMessage(message);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot send message={} to seat", LOG.isErrorEnabled() ? toString() : "", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SendSeatOwnerMessageTask.class.getSimpleName() + "[", "]")
                .add("roomId=" + roomId)
                .add("gameType=" + gameType)
                .add("senderServerId=" + senderServerId)
                .add("message=" + message)
                .add("context=" + context)
                .toString();
    }
}
