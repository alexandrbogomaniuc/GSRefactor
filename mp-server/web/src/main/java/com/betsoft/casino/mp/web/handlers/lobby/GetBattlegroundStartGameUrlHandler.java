package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.GetBattlegroundStartGameUrl;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;
import com.google.gson.Gson;
import com.hazelcast.core.*;
import com.hazelcast.core.Member;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Start game handler fo battleground mode. Handle GetBattlegroundStartGameUrl messages from client.
 */
@PropertySource("classpath:core.properties")
@Component
public class GetBattlegroundStartGameUrlHandler extends AbstractStartGameUrlHandler<GetBattlegroundStartGameUrl, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetBattlegroundStartGameUrlHandler.class);
    private final BotConfigInfoService botConfigInfoService;
    private final HazelcastInstance hazelcast;

    public GetBattlegroundStartGameUrlHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                                              SingleNodeRoomInfoService singleNodeRoomInfoService, MultiNodeRoomInfoService multiNodeRoomInfoService,
                                              ServerConfigService serverConfigService,
                                              RoomServiceFactory roomServiceFactory, RoomTemplateService roomTemplateService,
                                              RoomPlayerInfoService playerInfoService, BGPrivateRoomInfoService bgPrivateRoomInfoService,
                                              MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService, BotConfigInfoService botConfigInfoService,
                                              HazelcastInstance hazelcast, RoomPlayersMonitorService roomPlayersMonitorService) {
        super(gson, lobbySessionService, lobbyManager, singleNodeRoomInfoService, multiNodeRoomInfoService, serverConfigService, roomServiceFactory,
                roomTemplateService, playerInfoService, bgPrivateRoomInfoService, multiNodePrivateRoomInfoService, roomPlayersMonitorService);
        this.botConfigInfoService = botConfigInfoService;
        this.hazelcast = hazelcast;
    }

    @Override
    long getRequestedRoomIdFromMessage(GetBattlegroundStartGameUrl message) {
        return message.getRoomId() == null ? -1 : message.getRoomId();
    }

    private int getTotalObserversCount(ILobbySocketClient client, long serverId, long roomId, int gameId) {

        getLog().debug("getTotalObserversCount {}, {}: start: serverId: {}, roomId: {}, gameId: {}",
                client.getNickname(), client.getSessionId(), serverId, roomId, gameId);

        long now = System.currentTimeMillis();

        MutableInt res = new MutableInt(0);

        IRoomInfoService roomInfoService = getRoomInfoService(client);
        if (roomInfoService != null) {
            CountDownLatch latch = new CountDownLatch(1);
            long timeout = 5; // Set your desired timeout in seconds

            Callable<Collection> observerClientListCollectionTask =
                    roomInfoService.createObserverClientListCollectionTask(roomId, gameId);

            MultiExecutionCallback allNodesExecutionCallback = new MultiExecutionCallback() {
                @Override
                public void onResponse(Member member, Object value) {
                    // Handle individual responses if needed
                }

                @Override
                public void onComplete(Map<Member, Object> values) {
                    try {
                        for (Map.Entry<Member, Object> memberObjectEntry : values.entrySet()) {
                            Member member = memberObjectEntry.getKey();
                            Object value = memberObjectEntry.getValue();
                            if (value != null) {
                                try {
                                    Collection observerList = (Collection) value;

                                    //gat all observers excluding current user
                                    long observersNumber = observerList.stream().filter(o ->
                                            {
                                                if (o instanceof IGameSocketClient) {
                                                    return !((IGameSocketClient) o).getAccountId().equals(client.getAccountId());
                                                }
                                                return false;
                                            }
                                    ).count();

                                    res.add(observersNumber);

                                    getLog().debug("getTotalObserversCount {}, {}: member: {}, observersNumber: {}, res: {} , roomId: {}, gameId: {}",
                                            client.getNickname(), client.getSessionId(), member, observersNumber, res, roomId, gameId);
                                } catch (Exception e) {
                                    getLog().debug("getTotalObserversCount {}, {}: ERROR!!! member: {}, res: {} , roomId: {}, gameId: {}, error: {}",
                                            client.getNickname(), client.getSessionId(), member, res, roomId, gameId, e.getMessage());
                                }
                            }
                        }
                    } finally {
                        getLog().debug("getTotalObserversCount {}, {}: latch.countDown triggered: res:{}, roomId: {}, gameId: {}",
                                client.getNickname(), client.getSessionId(), res, roomId, gameId);
                        latch.countDown(); // Ensure latch is counted down even if there is an exception
                    }
                }
            };

            ExecutionCallback<Collection>  oneNodeExecutionCallback
                    = new ExecutionCallback<Collection>() {

                @Override
                public void onResponse(Collection observerList) {
                    try {
                        long observersNumber = observerList.stream().filter(o ->
                                {
                                    if (o instanceof IGameSocketClient) {
                                        return !((IGameSocketClient) o).getAccountId()
                                                .equals(client.getAccountId());
                                    }
                                    return false;
                                }
                        ).count();

                        res.add(observersNumber);

                        getLog().debug("getTotalObserversCount {}, {}: observersNumber: {}, roomId: {}, gameId: {}",
                                client.getNickname(), client.getSessionId(), observersNumber, roomId, gameId);

                    } catch (Exception e) {

                        getLog().debug("getTotalObserversCount {}, {}: ERROR!!! , roomId: {}, gameId: {}, error: {}",
                                client.getNickname(), client.getSessionId(), roomId, gameId, e.getMessage());
                    }
                }

                @Override
                public void onFailure(Throwable t) {

                }
            };

            if(serverId == IRoomInfo.NOT_ASSIGNED_ID) {

                getLog().debug("getTotalObserversCount {}, {}: serverId is {}, submit " +
                                "observerClientListCollectionTask To All Members for roomId: {}, gameId: {}",
                        client.getNickname(), client.getSessionId(), serverId, roomId, gameId);

                playerInfoService.getNotifyService()
                        .submitToAllMembers(observerClientListCollectionTask, allNodesExecutionCallback);
            } else {

                boolean memberFound = false;
                for (Member member : hazelcast.getCluster().getMembers()) {

                    if (member.toString().contains("[gs" + serverId + "]")) {

                        getLog().debug("getTotalObserversCount {}, {}: serverId is {}, submit " +
                                        "observerClientListCollectionTask To Member {} for roomId: {}, gameId: {}",
                                client.getNickname(), client.getSessionId(), serverId, member, roomId, gameId);

                        memberFound = true;

                        playerInfoService.getNotifyService()
                                .submitToMember(observerClientListCollectionTask, member, oneNodeExecutionCallback);
                    }
                }

                if(!memberFound) {
                    getLog().debug("getTotalObserversCount {}, {}: can't find Hazelcast cluster member for serverId {} " +
                                    "for roomId: {}, gameId: {}",
                            client.getNickname(), client.getSessionId(), serverId, roomId, gameId);
                }
            }

            try {
                if (!latch.await(timeout, TimeUnit.SECONDS)) {
                    getLog().debug("getTotalObserversCount {}, {}: timeout:{} {} while waiting for " +
                                    "observers count: observers: {}, roomId: {}, gameId: {}",
                            client.getNickname(), client.getSessionId(), timeout, TimeUnit.SECONDS, res, roomId, gameId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                throw new RuntimeException("Thread was interrupted while waiting for observers count", e);
            }

            getLog().debug("getTotalObserversCount {}, {}: end, duration:{} ms, observers: {}, roomId: {}, gameId: {}",
                    client.getNickname(), client.getSessionId(), System.currentTimeMillis() - now, res, roomId, gameId);

            return res.intValue();
        }

        throw new RuntimeException("Cannot collect number of observers!");
    }


    public boolean checkIsBadMessageAndSendError(GetBattlegroundStartGameUrl message, ILobbySocketClient client,
                                                 ILobbySession lobbySession) {
        if (message.getBuyIn() <= 0) {
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Illegal stake value", message.getRid());
            return true;
        }
        if (lobbySession.getBattlegroundBuyIns() == null ||
                !lobbySession.getBattlegroundBuyIns().contains(message.getBuyIn())
                || !lobbySession.isBattlegroundAllowed()
        ) {
            LOG.error("stake {} not allowed, allowed only {}, client={} ", message.getBuyIn(),
                    lobbySession.getStakes(), client);
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Illegal stake value", message.getRid());
            return true;
        }
        return false;
    }

    @Override
    protected Money getStakeFromMessage(GetBattlegroundStartGameUrl message) {
        return Money.fromCents(message.getBuyIn());
    }

    private void addPlayerToWaitingOpenRoom(IRoomInfo roomInfo, long accountId) {
        if (roomInfo instanceof SingleNodeRoomInfo) {
            SingleNodeRoomInfo singleNodeRoomInfo = (SingleNodeRoomInfo) roomInfo;
            singleNodeRoomInfo.addPlayerToWaitingOpenRoom(accountId);
            getLog().debug("addPlayerToWaitingOpenRoom: GetBattlegroundStartGameUrlHandler added account to " +
                            "waiting observers:  {}, roomId: {}, singleNodeRoomInfo.getWaitingOpenRoomPlayersWithCheck(): {}",
                    accountId, roomInfo.getId(), singleNodeRoomInfo.getWaitingOpenRoomPlayersWithCheck());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public synchronized IRoomInfo getBestRoomForStake(LobbySession lobbySession, GameType gameType,
                                                      ILobbySocketClient client, String currency,
                                                      GetBattlegroundStartGameUrl message) throws CommonException {
        //only real mode allowed for battleground
        if (client.getMoneyType() != MoneyType.REAL) {
            throw new CommonException("Incorrect mode found in lobby session");
        }

        Money stakeFromMessage = getStakeFromMessage(message);

        IRoomInfoService roomInfoService = getRoomInfoService(client);
        Collection<IRoomInfo> roomInfos = roomInfoService.getBattlegroundRooms(client.getBankId(), gameType,
                stakeFromMessage, client.getPlayerInfo().getCurrency().getCode());

        long accountId = lobbySession.getAccountId();

        long now = System.currentTimeMillis();

        List<Pair<IRoomInfo, Integer>> roomInfoFiltered = new ArrayList<>();

        for(IRoomInfo roomInfo : roomInfos) {

            int numberOfPlayers = getNumberOfPlayers(roomInfo, accountId, client);

            short maxSeats = roomInfo.getMaxSeats();
            if(numberOfPlayers < maxSeats && !roomInfo.isDeactivated()) {

                LOG.debug("getBestRoomForStake: numberOfPlayers={}, roomInfo={}", numberOfPlayers, roomInfo);
                roomInfoFiltered.add(new Pair<>(roomInfo, numberOfPlayers));
            }
        }

        LOG.debug("getBestRoomForStake: roomInfoFiltered={}", roomInfoFiltered);

        List<Pair<IRoomInfo, Integer>> roomInfoSorted = roomInfoFiltered.stream()
                .sorted(Comparator
                        .comparing((Pair<IRoomInfo, Integer> pair) -> pair.getValue()).reversed() // Sort by integer value descending
                        .thenComparing(pair -> pair.getKey().getId())) // Sort by IRoomInfo.getId() ascending
                .collect(Collectors.toList());

        LOG.debug("getBestRoomForStake: roomInfoSorted={}", roomInfoSorted);

        IRoomInfo bestRoomInfo = roomInfoSorted.isEmpty() ? null : roomInfoSorted.get(0).getKey();

        LOG.debug("getBestRoomForStake {}, {}: duration={} ms, bestRoomInfo={}",
                client.getNickname(), client.getSessionId(), System.currentTimeMillis() - now, bestRoomInfo);

        if (bestRoomInfo != null) {

            IRoom roomWithoutCreation = getRoomServiceFactory()
                    .getRoomWithoutCreation(bestRoomInfo.getGameType(), bestRoomInfo.getId());

            if (roomWithoutCreation != null) {

                LOG.debug("getBestRoomForStake {}, {}: add account:{} as Player To WaitingOpenRoom, bestRoomInfo={}",
                        client.getNickname(), client.getSessionId(), accountId, bestRoomInfo);

                roomWithoutCreation.updateRoomInfo(roomInfo -> {
                    addPlayerToWaitingOpenRoom(roomInfo, accountId);
                });

            } else {
                addPlayerToWaitingOpenRoom(bestRoomInfo, accountId);
            }

        } else {

            LOG.debug("getBestRoomForStake {}, {}: No room found, need create additional rooms, found small free " +
                    "rooms condition",  client.getNickname(), client.getSessionId());

            if (!roomInfos.isEmpty()) {

                IRoomInfo roomInfo = roomInfos.iterator().next();

                if (roomInfo != null) {

                    Collection<IRoomInfo> roomInfosNew = new ArrayList<>();
                    RoomTemplate roomTemplate = getRoomTemplateService().get(roomInfo.getTemplateId());

                    int sizeBefore = roomInfoService.getAllRooms().size();

                    roomInfoService.createForTemplate(roomTemplate, roomInfosNew, client.getBankId(), stakeFromMessage, currency);

                    LOG.debug("getBestRoomForStake {}, {}: added new rooms, cnt: {}",
                            client.getNickname(), client.getSessionId(), (roomInfoService.getAllRooms().size() - sizeBefore));

                    Optional<IRoomInfo> anyRoomInfo = roomInfosNew.stream().findAny();
                    if (anyRoomInfo.isPresent()) {

                        bestRoomInfo = anyRoomInfo.get();
                        addPlayerToWaitingOpenRoom(bestRoomInfo, accountId);
                    }
                }
            }
        }

        LOG.debug("getBestRoomForStake {}, {}:  {}", client.getNickname(), client.getSessionId(), bestRoomInfo);
        return bestRoomInfo;
    }


    private boolean isBot(String nickName) {
        return nickName != null && botConfigInfoService.getByMqNickName(nickName) != null;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}

