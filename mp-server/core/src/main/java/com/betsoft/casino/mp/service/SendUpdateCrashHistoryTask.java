package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.ICrashRoundInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.utils.ITransportObject;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.StringJoiner;

@SpringAware
public class SendUpdateCrashHistoryTask implements Runnable, Serializable, ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(SendUpdateCrashHistoryTask.class);
    private final long roomId;
    private final GameType gameType;
    private final long senderServerId;
    private final ICrashRoundInfo crashRoundInfo;
    private transient ApplicationContext context;

    public SendUpdateCrashHistoryTask(Long roomId, GameType gameType, long senderServerId, ICrashRoundInfo crashRoundInfo) {
        this.roomId = roomId;
        this.gameType = gameType;
        this.senderServerId = senderServerId;
        this.crashRoundInfo = crashRoundInfo;
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
            if (senderServerId != serverConfigService.getServerId()) {
                appendCrashHistory(room.getMap(), crashRoundInfo);
                LOG.debug("save crashRoundInfo from other server, {}", crashRoundInfo);
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
        return new StringJoiner(", ", SendUpdateCrashHistoryTask.class.getSimpleName() + "[", "]")
                .add("roomId=" + roomId)
                .add("gameType=" + gameType)
                .add("senderServerId=" + senderServerId)
                .add("crashRoundInfo=" + crashRoundInfo)
                .add("context=" + context)
                .toString();
    }

    private static void appendCrashHistory(Object map, ICrashRoundInfo info) throws Exception {
        for (Method method : map.getClass().getMethods()) {
            if (!"addCrashHistory".equals(method.getName()) || method.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType.isAssignableFrom(info.getClass())) {
                method.invoke(map, info);
                return;
            }
        }
        throw new NoSuchMethodException("addCrashHistory(<ICrashRoundInfo>) not found for map " + map.getClass().getName());
    }
}

