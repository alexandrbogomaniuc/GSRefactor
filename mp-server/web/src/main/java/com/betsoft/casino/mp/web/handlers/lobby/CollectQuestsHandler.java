package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.persister.PlayerQuestsPersister;
import com.betsoft.casino.mp.data.persister.PlayerStatsPersister;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.transport.CollectQuest;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.LevelUpLobby;
import com.betsoft.casino.mp.transport.Ok;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.CollectResult;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.QuestCollectTask;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.google.gson.Gson;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Map;

@Component
public class CollectQuestsHandler extends MessageHandler<CollectQuest, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(CollectQuestsHandler.class);
    private final PlayerQuestsPersister playerQuestsPersister;
    private final PlayerStatsPersister playerStatsPersister;
    private RoomPlayerInfoService playerInfoService;

    public CollectQuestsHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                                RoomPlayerInfoService playerInfoService, CassandraPersistenceManager cpm) {
        super(gson, lobbySessionService, lobbyManager);
        this.playerQuestsPersister = cpm.getPersister(PlayerQuestsPersister.class);
        this.playerStatsPersister = cpm.getPersister(PlayerStatsPersister.class);
        this.playerInfoService = playerInfoService;
    }

    @Override
    public void handle(WebSocketSession session, CollectQuest message, ILobbySocketClient client) {
        if (checkLogin(message, client)) {
            Long bankId = client.getBankId();
            long gameId = client.getGameType().getGameId();
            Long accountId = client.getAccountId();

            IExecutorService notifyService = playerInfoService.getNotifyService();

            notifyService.submitToAllMembers(new QuestCollectTask(client.getAccountId(), message.getId(),
                            client.getGameType().getGameId()),
                    new MultiExecutionCallback() {
                        @Override
                        public void onResponse(Member member, Object value) {
                        }

                        @Override
                        public void onComplete(Map<Member, Object> values) {
                            boolean resultFromGsSent = false;
                            for (Map.Entry<Member, Object> memberObjectEntry : values.entrySet()) {
                                Member member = memberObjectEntry.getKey();
                                Object value = memberObjectEntry.getValue();
                                if (value != null) {
                                    CollectResult collectResult = (CollectResult) value;
                                    LOG.debug("member: " + member + " collectResult: " + collectResult);
                                    if (collectResult.getErrorCode() != QuestCollectTask.SEATER_NOT_FOUND_IN_ROOM) {
                                        resultFromGsSent = true;
                                        if (collectResult.getErrorCode() == QuestCollectTask.QUEST_NOT_EXIST_IN_LIST
                                                || collectResult.getErrorCode() == ErrorCodes.QUEST_IS_NOT_COMPLETED) {
                                            client.sendMessage(new Error(ErrorCodes.QUEST_IS_NOT_COMPLETED,
                                                    "Quest is not completed on server side.", message.getRid()));
                                        } else if (collectResult.getErrorCode() == ErrorCodes.QUEST_ALREADY_COLLECTED) {
                                            client.sendMessage(new Error(ErrorCodes.QUEST_ALREADY_COLLECTED,
                                                    "Quest already collected on server side.", message.getRid()));
                                        } else {
                                            if (collectResult.isNewLevel()) {
                                                client.sendMessage(
                                                        new LevelUpLobby(
                                                                System.currentTimeMillis(), message.getRid(),
                                                                -1,
                                                                collectResult.getNewLevel(),
                                                                collectResult.getXp(),
                                                                collectResult.getXpPrev(),
                                                                collectResult.getXpNext()));
                                            }
                                            client.sendMessage(new Ok(System.currentTimeMillis(), message.getRid()));
                                        }
                                    }
                                }
                            }

                            LOG.debug("resultFromGsSent: " + resultFromGsSent);
                            if (!resultFromGsSent) {
                                processFromCassandra(message, client, bankId, gameId, accountId);
                            }
                        }
                    });


        }
    }

    private void processFromCassandra(CollectQuest message, ILobbySocketClient client, Long bankId, long gameId, Long accountId) {
//        PlayerQuests playerQuests = playerQuestsPersister.load(bankId, gameId, accountId);
//        Set<Quest> quests = playerQuests.getQuests();
//        getLog().debug("quests from persister: " + quests);
//        Optional<Quest> quest = quests.stream().filter(q -> q.getId() == message.getId()).findFirst();
//        getLog().debug("quest: " + quest);
//        if (quest.isPresent() && quest.get().isComplete()) {
//            if (quest.get().isCollected()) {
//                client.sendMessage(new Error(ErrorCodes.QUEST_ALREADY_COLLECTED,
//                        "Quest already collected on server side.", message.getRid()));
//            } else {
//                Quest quest_ = quest.get();
//                quest_.setCollected(true);
//                quests.add(quest_);
//                playerQuestsPersister.updateQuests(bankId, gameId, accountId, quests);
//                getLog().debug("new quests: " + quests);
//                PlayerStats stats = playerStatsPersister.load(bankId, gameId, accountId);
//                long xp = quest.get().getXP();
//                int oldLevel = AchievementHelper.getPlayerLevel(stats.getScore());
//                int newLevel = AchievementHelper.getPlayerLevel(stats.getScore().getAmount() + xp);
//                if (newLevel > oldLevel) {
//                    client.sendMessage(new LevelUpLobby(
//                                    System.currentTimeMillis(), message.getRid(),
//                                    -1,
//                                    newLevel,
//                                    stats.getScore().getLongAmount() + xp,
//                                    getXP(newLevel),
//                                    getXP(newLevel + 1)
//                            )
//                    );
//                }
//                PlayerStats diff = new PlayerStats();
//                diff.addScore(xp);
//                getLog().debug("Adding stats for {}: {}", accountId, diff);
//                playerStatsPersister.addStats(bankId, gameId, accountId, diff);
//
//                getLog().debug("oldLevel: " + oldLevel + " newLevel: " + newLevel);
//                client.sendMessage(new Ok(System.currentTimeMillis(), message.getRid()));
//            }
//        } else {
//            client.sendMessage(new Error(ErrorCodes.QUEST_IS_NOT_COMPLETED,
//                    "Quest is not completed on server side.", message.getRid()));
//        }
    }

    public static long getXP(long level) {
        return 500 * (level * level - level);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
