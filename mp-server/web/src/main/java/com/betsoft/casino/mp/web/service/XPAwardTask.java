package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.common.AbstractGameRoom;
import com.betsoft.casino.mp.common.AchievementHelper;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IPlayerStats;
import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SpringAware
public class XPAwardTask implements Callable<CollectResult>, Serializable, ApplicationContextAware {
    private static final Logger LOG = LogManager.getLogger(XPAwardTask.class);

    private transient ApplicationContext context;

    private long accountId;
    private long gameId;
    private long xp;

    public static int SUCCESS = -1;
    public static int SEATER_NOT_FOUND_IN_ROOM = -3;

    public XPAwardTask(long accountId, long xp, long gameId) {
        this.accountId = accountId;
        this.xp = xp;
        this.gameId = gameId;
    }

    @Override
    public CollectResult call() throws Exception {
        CollectResult collectResult = new CollectResult();
        if (context != null) {
            LOG.debug("call: {}", toString());
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
                if (room == null){
                    return null; // room is not found on this server
                }

                AbstractGameRoom gameRoom = (AbstractGameRoom) room;
                ISeat seatByAccountId = gameRoom.getSeatByAccountId(accountId);
                LOG.debug("seatByAccountId: {}", seatByAccountId);
                if (seatByAccountId != null) {
                    IPlayerStats stats = seatByAccountId.getPlayerInfo().getStats();
                    LOG.debug("old stats: {}", stats);
                    int oldLevel = AchievementHelper.getPlayerLevel(stats.getScore());
                    int newLevel = AchievementHelper.getPlayerLevel(stats.getScore().getAmount() + xp);
                    boolean isNewLevelUP = newLevel > oldLevel;
                    collectResult.setNewLevel(isNewLevelUP);
                    stats.addScore(xp);
                    gameRoom.updateXPStats(xp, seatByAccountId);
                    if (isNewLevelUP) {
                        collectResult.setNewLevel(newLevel);
                        collectResult.setXp(stats.getScore().getLongAmount());
                        collectResult.setXpPrev(AchievementHelper.getXP(newLevel));
                        collectResult.setXpNext(AchievementHelper.getXP(newLevel + 1));
                    }
                    LOG.debug("oldLevel: " + oldLevel + " newLevel: " + newLevel);
                    LOG.debug("new stats: {}", stats);
                    collectResult.setErrorCode(SUCCESS);
                    collectResult.setPlayerStats(stats);
                } else {
                    collectResult.setErrorCode(SEATER_NOT_FOUND_IN_ROOM);
                }
            } catch (Exception e) {
                LOG.debug("error: ", e);
                collectResult.setErrorCode(SEATER_NOT_FOUND_IN_ROOM);
            }
        } else {
            LOG.error("Application context not found, accountId: {}", accountId);
        }

        LOG.debug("XP collect result: " + collectResult);
        return collectResult;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("XPAwardTask [");
        sb.append("accountId=").append(accountId);
        sb.append(", gameId=").append(gameId);
        sb.append(", xp=").append(xp);
        sb.append(']');
        return sb.toString();
    }
}
