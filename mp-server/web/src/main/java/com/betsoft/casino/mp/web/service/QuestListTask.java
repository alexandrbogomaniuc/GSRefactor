package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

@SpringAware
public class QuestListTask implements Callable<Set<IQuest>>, Serializable, ApplicationContextAware {
    private long accountId;
    private long gameId;
    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(QuestListTask.class);

    public QuestListTask(long accountId, long gameId) {
        this.accountId = accountId;
        this.gameId = gameId;
    }

    @SuppressWarnings("rawtypes")
    public Set<IQuest> call() {
        Set<IQuest> quests = new HashSet<>();
        if (context != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("call:  {}", toString());
            }
            try {
                RoomServiceFactory roomServiceFactory = context.getBean("roomServiceFactory", RoomServiceFactory.class);
                RoomPlayerInfoService playerInfoService = context.getBean("playerInfoService", RoomPlayerInfoService.class);

                long roomId = -1;
                IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
                if (roomPlayerInfo != null) {
                    roomId = roomPlayerInfo.getRoomId();
                }
                GameType gameType = GameType.getByGameId((int) gameId);
                IRoom room = roomServiceFactory.getRoomWithoutCreation(gameType, roomId);
                if (room != null) {
                    ISeat seatByAccountId = room.getSeatByAccountId(accountId);
                    LOG.debug("seatByAccountId: {}", seatByAccountId);
                    if (seatByAccountId != null)
                        quests = seatByAccountId.getPlayerInfo().getPlayerQuests().getQuests();
                }
            } catch (Exception e) {
                LOG.debug("call error:", e);
            }
        } else {
            LOG.error("ApplicationContext not found accountId: {}", accountId);
        }

        LOG.debug("quests from gs, size : {},  quests: {}: ", quests.size(), quests);
        return quests;
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuestListTask [");
        sb.append("accountId=").append(accountId);
        sb.append(", gameId=").append(gameId);
        sb.append(']');
        return sb.toString();
    }
}
