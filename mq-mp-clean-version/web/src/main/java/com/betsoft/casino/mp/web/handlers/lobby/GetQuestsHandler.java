package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.persister.PlayerQuestsPersister;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.Quest;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.transport.GetQuests;
import com.betsoft.casino.mp.transport.Quests;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.QuestListTask;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GetQuestsHandler extends MessageHandler<GetQuests, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetQuestsHandler.class);
    private final PlayerQuestsPersister playerQuestsPersister;
    private final RoomPlayerInfoService playerInfoService;


    public GetQuestsHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                            RoomPlayerInfoService playerInfoService,
                            CassandraPersistenceManager cpm) {
        super(gson, lobbySessionService, lobbyManager);
        this.playerQuestsPersister = cpm.getPersister(PlayerQuestsPersister.class);
        this.playerInfoService = playerInfoService;
    }

    @Override
    public void handle(WebSocketSession session, GetQuests message, ILobbySocketClient client) {
        if (checkLogin(message, client)) {

            PlayerQuests allPlayerQuests = new PlayerQuests(new HashSet<>());
            final Long bankId = client.getBankId();
            GameType gameType = client.getGameType();
            final long gameId = gameType.getGameId();
            final Long accountId = client.getAccountId();
            MoneyType moneyType = client.getMoneyType();
            int mode = moneyType.ordinal();

            LOG.debug("GetQuestsHandler bankId={}, accountId={}, gameId={}, mode={}", bankId, accountId, gameId, mode);

            LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
            List<Long> stakes = lobbySession.getStakes();
            LOG.debug("client.getSessionId(): {} stakes: {}", client.getSessionId(), stakes);


            for (Long stake_ : stakes) {
                Money stake = Money.fromCents(stake_);
                PlayerQuests playerQuests = loadQuests(lobbySession, bankId, gameId, accountId, stake, mode);

                allPlayerQuests.getQuests().addAll(playerQuests.getQuests());
                LOG.debug("get data from cassandra for stake: playerQuests for stake={}, allPlayerQuests size={}, " +
                                "playerQuests.getQuests().size()={}",
                        stake, allPlayerQuests.getQuests().size(), playerQuests.getQuests().size());

                playerQuests.getQuests().forEach(LOG::debug);
            }


            IExecutorService notifyService = playerInfoService.getNotifyService();

            notifyService.submitToAllMembers(new QuestListTask(client.getAccountId(),
                    client.getGameType().getGameId()), new MultiExecutionCallback() {
                @Override
                public void onResponse(Member member, Object value) {
                }

                @Override
                public void onComplete(Map<Member, Object> values) {
                    PlayerQuests playerQuests = null;
                    for (Map.Entry<Member, Object> memberObjectEntry : values.entrySet()) {
                        Member member = memberObjectEntry.getKey();
                        Object value = memberObjectEntry.getValue();
                        if (value instanceof Set) {
                            Set quests = (Set) value;
                            LOG.debug("member: {} quests: {}", member, quests);
                            if (!quests.isEmpty()) {
                                Set<IQuest> questHashSet = new HashSet<>(quests.size());
                                for (Object quest : quests) {
                                    if (quest instanceof Quest) {
                                        questHashSet.add((Quest) quest);
                                    }
                                }
                                LOG.debug("questHashSet: {}", questHashSet);
                                playerQuests = new PlayerQuests(questHashSet);
                            }

                        }
                    }

                    if (playerQuests != null) {
                        LOG.debug("Quests from room should be updated={}", playerQuests);
                        LOG.debug("allPlayerQuests before: {}", allPlayerQuests);
                        allPlayerQuests.getQuests().removeAll(playerQuests.getQuests());
                        allPlayerQuests.getQuests().addAll(playerQuests.getQuests());
                        LOG.debug("allPlayerQuests after: {}", allPlayerQuests);
                    }

                    LOG.debug("allPlayerQuests.size after: {}", allPlayerQuests.getQuests().size());

                    client.sendMessage(new Quests(System.currentTimeMillis(), message.getRid(),
                            allPlayerQuests.getQuests()), message);
                }
            });

        }
    }

    private PlayerQuests loadQuests(LobbySession lobbySession, long bankId, long gameId, long accountId, Money stake, int mode) {
        Long bonusOrTournamentId = EnterLobbyHandler.getBonusOrTournamentId(lobbySession.getTournamentSession(),
                lobbySession.getActiveFrbSession(), lobbySession.getActiveCashBonusSession());

        if (bonusOrTournamentId == null) {
            return playerQuestsPersister.load(bankId, gameId, accountId, stake, mode);
        } else {
            return playerQuestsPersister.loadSpecialModeQuests(bonusOrTournamentId,
                    bankId, gameId, accountId, stake, mode);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
