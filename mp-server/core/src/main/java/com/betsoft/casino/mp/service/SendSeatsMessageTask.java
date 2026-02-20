package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.ITransportObject;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * User: flsh
 * Date: 03.03.2022.
 */
@SpringAware
public class SendSeatsMessageTask implements Runnable, Serializable, ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(SendSeatsMessageTask.class);
    private long roomId;
    private GameType gameType;
    private long senderServerId;
    private Long relatedAccountId;
    private boolean notSendToRelatedAccountId;
    private long relatedRequestId;
    private ITransportObject message;
    boolean sendToAllObservers;
    private transient ApplicationContext context;

    public SendSeatsMessageTask(long roomId, GameType gameType, long senderServerId, Long relatedAccountId, boolean notSendToRelatedAccountId,
                                long relatedRequestId, ITransportObject message, boolean sendToAllObservers) {
        this.roomId = roomId;
        this.gameType = gameType;
        this.senderServerId = senderServerId;
        this.relatedAccountId = relatedAccountId;
        this.notSendToRelatedAccountId = notSendToRelatedAccountId;
        this.relatedRequestId = relatedRequestId;
        this.message = message;
        this.sendToAllObservers = sendToAllObservers;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void run() {
        LOG.debug("run: Run task={}", toString());
        if (context == null) {
            LOG.error("run: ApplicationContext not found, roomId={}", roomId);
            return;
        }
        IServerConfigService serverConfigService = (IServerConfigService) context.getBean("serverConfigService");
        //don't send message for server created this task
        if (serverConfigService.getServerId() != senderServerId) {
            try {
                IRoomServiceFactory roomServiceFactory = (IRoomServiceFactory) context.getBean("roomServiceFactory");
                IRoom room = roomServiceFactory.getRoomWithoutCreation(gameType, roomId);
                if (room == null) {
                    LOG.info("run: Room not found, message={}", toString());
                    return;
                }
                LOG.debug("run: Room found {}", room);
                sendMessageToSeats(room);
            } catch (Exception e) {
                LOG.error("run: Cannot send message={}", toString(), e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void sendMessageToSeats(IRoom room) {
        if (sendToAllObservers) {
            LOG.debug("sendMessageToSeats: send message to observers {}", room);
            Collection<IGameSocketClient> observers = room.getObservers();
            for (IGameSocketClient observer : observers) {
                if (relatedAccountId != null && observer.getAccountId() != null && notSendToRelatedAccountId && observer.getAccountId().
                        equals(relatedAccountId)) {
                    LOG.debug("sendMessageToSeats: skip observer: {}", observer);
                    continue;
                }
                if (!observer.isDisconnected()) {
                    LOG.debug("sendMessageToSeats: send message {} to observer: {}", message, observer);
                    observer.sendMessage(message);
                } else {
                    LOG.debug("sendMessageToSeats: skip observer (is disconnected): {}", observer);
                }
            }

        } else {
            LOG.debug("sendMessageToSeats: send message to seats {}", room);
            @SuppressWarnings("unchecked")
            List<ISeat> seats = room.getAllSeats();
            for (ISeat seat : seats) {
                if (seat != null) {
                    if (relatedAccountId != null && notSendToRelatedAccountId && seat.getAccountId() == relatedAccountId) {
                        LOG.debug("sendMessageToSeats: skip seat: {}", seat);
                        continue;
                    }
                    try {
                        LOG.debug("sendMessageToSeats: send message {} to seat: {}", message, seat);
                        seat.sendMessage(message);
                    } catch (Exception e) {
                        LOG.debug("Cannot send message to seat, accountId={}", seat.getAccountId(), e);
                    }
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
        return new StringJoiner(", ", SendSeatsMessageTask.class.getSimpleName() + "[", "]")
                .add("roomId=" + roomId)
                .add("gameType=" + gameType)
                .add("serverId=" + senderServerId)
                .add("relatedAccountId=" + relatedAccountId)
                .add("notSendToRelatedAccountId=" + notSendToRelatedAccountId)
                .add("relatedRequestId=" + relatedRequestId)
                .add("message=" + message)
                .add("sendToAllObservers=" + sendToAllObservers)
                .toString();
    }
}
