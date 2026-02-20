package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.common.AbstractGameRoom;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

@SpringAware
public class QuestCollectTask implements Callable<CollectResult>, Serializable, ApplicationContextAware {
    private long accountId;
    private long questId;
    private long gameId;

    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(QuestCollectTask.class);
    public static int QUEST_UPDATED_CORRECT = -1;
    public static int QUEST_NOT_EXIST_IN_LIST = -2;
    public static int SEATER_NOT_FOUND_IN_ROOM = -3;

    public QuestCollectTask(long accountId, long questId, long gameId) {
        this.accountId = accountId;
        this.questId = questId;
        this.gameId = gameId;
    }

    public CollectResult call() {
        CollectResult collectResult = new CollectResult();
        if (context != null) {
            LOG.debug("call: {} ", toString());
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
                if (room == null) {
                    return null; // room is not found on this server
                }

                AbstractGameRoom gameRoom = (AbstractGameRoom) room;
                ISeat seatByAccountId = gameRoom.getSeatByAccountId(accountId);
                LOG.debug("seatByAccountId: " + seatByAccountId);
                if (seatByAccountId != null) {
                    Set<IQuest> quests = seatByAccountId.getPlayerInfo().getPlayerQuests().getQuests();
                    Optional<IQuest> first = quests.stream().filter(quest -> quest.getId() == questId).findFirst();
                    if (first.isPresent()) {
                        IQuest quest = first.get();

//                        if (!quest.isComplete()) {
//                            return new CollectResult(null, false,0,  ErrorCodes.QUEST_IS_NOT_COMPLETED);
//                        } else if(quest.isCollected()){
//                            return new CollectResult(null, false,0,  ErrorCodes.QUEST_ALREADY_COLLECTED);
//                        }else {
//                            quest.setCollected(true);
//                            quests.add(quest);
//                            PlayerStats stats = seatByAccountId.getPlayerInfo().getStats();
//                            LOG.debug("old stats: " + stats);
//                            long xp = quest.getXP();
//                            int oldLevel = AchievementHelper.getPlayerLevel(stats.getScore());
//                            int newLevel = AchievementHelper.getPlayerLevel(stats.getScore().getAmount() + xp);
//                            boolean isNewLevelUP = newLevel > oldLevel;
//                            collectResult.setNewLevel(isNewLevelUP);
//                            stats.addScore(xp);
//                            gameRoom.updateStatsFromCollectQuests(xp, seatByAccountId, quests);
//                            if (isNewLevelUP) {
//                                collectResult.setNewLevel(newLevel);
//                                collectResult.setXp(stats.getScore().getLongAmount());
//                                collectResult.setXpPrev(AchievementHelper.getXP(newLevel));
//                                collectResult.setXpNext(AchievementHelper.getXP(newLevel + 1));
//                            }
//                            LOG.debug("oldLevel: " + oldLevel + " newLevel: " + newLevel);
//                            LOG.debug("new stats: " + stats);
//                            collectResult.setErrorCode(QUEST_UPDATED_CORRECT);
//                            collectResult.setPlayerStats(stats);
//                        }
                    } else {
                        collectResult.setErrorCode(QUEST_NOT_EXIST_IN_LIST); //
                    }
                } else {
                    collectResult.setErrorCode(SEATER_NOT_FOUND_IN_ROOM);
                }
            } catch (Exception e) {
                LOG.debug("error: " + e.getMessage());
                collectResult.setErrorCode(SEATER_NOT_FOUND_IN_ROOM);
            }
        } else {
            LOG.error("ApplicationContext not found accountId: " + accountId);
        }

        LOG.debug("quest collectResult from gs: " + collectResult);
        return collectResult;
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuestCollectTask [");
        sb.append("accountId=").append(accountId);
        sb.append(", questId=").append(questId);
        sb.append(", gameId=").append(gameId);
        sb.append(']');
        return sb.toString();
    }
}
