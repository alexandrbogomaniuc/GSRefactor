package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.common.AbstractBattlegroundGameRoom;
import com.betsoft.casino.mp.maxblastchampions.model.BattleAbstractCrashGameRoom;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.bots.ActiveBot;
import com.betsoft.casino.mp.model.bots.BotConfigInfo;
import com.betsoft.casino.mp.model.bots.dto.BotLogInResult;
import com.betsoft.casino.mp.model.bots.dto.BotLogOutResult;
import com.betsoft.casino.mp.model.bots.dto.BotStatusResult;
import com.betsoft.casino.mp.model.bots.dto.BotsMap;
import com.betsoft.casino.mp.model.bots.dto.SimpleBot;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.impl.predicates.AndPredicate;
import com.hazelcast.query.impl.predicates.OrPredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;

/**
 * User: flsh
 * Date: 14.07.2022.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Service
public class BotManagerService implements
        IGameRoomStartListener,
        ISeatsCountChangedListener,
        IRoomStateChangedListener,
        IRoomOpenedListener,
        IRoomClosedListener,
        ILobbyConnectionClosedListener {

    public static final String ACTIVE_BOTS_STORE = "activeBotsStore";
    public static final String ACTIVE_NICKNAMES_LOG_IN_REQUESTS_STORE = "activeNicknamesLogInRequestsStore";
    public static final String ACTIVE_ROOMS_LOG_IN_REQUESTS_STORE = "activeRoomsNamesLogInRequestsStoreV2";
    public static final String ACTIVE_ROOMS_LOG_OUT_REQUESTS_STORE = "activeRoomsNamesLogOutRequestsStoreV2";
    public static final String BOTS_REQUIRED_IN_SHOOTING_ROOMS_STORE = "botsRequiredInShootingRoomsStore";
    public static final String BOTS_LOCK_STORE = "botsLockStore";
    private static final Logger LOG = LogManager.getLogger(BotManagerService.class);
    private final HazelcastInstance hazelcast;
    private final BotConfigInfoService botConfigInfoService;
    private final IBotServiceClient botServiceClient;
    private final SingleNodeRoomInfoService singleNodeRoomInfoService;
    private final MultiNodeRoomInfoService multiNodeRoomInfoService;
    private final IServerConfigService serverConfigService;
    private final LobbySessionService lobbySessionService;
    private final RoomPlayerInfoService roomPlayerInfoService;
    private IRoomServiceFactory roomServiceFactory;

    private IMap<Long, ActiveBot> activeBots;//accountId, ActiveBot
    private IMap<String, Long> activeNicknamesLogInRequests;//nickname, time logInRequested
    private IMap<Long, Map<String, Long>> activeRoomsLogInRequests;//roomId, Map of logIns for nickname with logIn dateTime stamp
    private IMap<Long, List<Long>> activeRoomsLogOutRequests;//roomId, count of logOuts
    private IMap<Long, Pair<Integer, Long>> botsRequiredInShootingRooms;//roomId, pair key = "count of current bots count required in the room", value = "update key at datetime"

    //sorry for ugly code, hazelcast.getLock() is deprecated, but FencedLock from CPSubsystem.getLock(String)
    // require at least 3 node cluster
    private IMap<String, String> lockMap;
    private final Random rnd = new Random();
    @Value("${server.host}")
    private String serverHost;
    private static final int MAX_NUMBER_BOTS = 150;
    private static final int PLAYERS_THRESHOLD_TO_KEEP_BOTS_IN_MBC_ROOM = 13;
    private static final int BOTS_LIMIT_IN_MBC_ROOM = 6;

    public static final int PLAYERS_THRESHOLD_TO_KEEP_BOTS_IN_SHOOTING_ROOM = 4;
    public static final int MIN_BOTS_LIMIT_IN_SHOOTING_ROOM = 2;
    public static final int MAX_BOTS_LIMIT_IN_SHOOTING_ROOM = 3;
    public static final int MIN_BOTS_LIMIT_IN_SHOOTING_ROOM_THRESHOLD_MINUTES = 3;
    public static final int MAX_BOTS_LIMIT_IN_SHOOTING_ROOM_THRESHOLD_MINUTES = 8;

    public static final int MIN_MANAGED_BOT_EXPIRATION_MINUTES = 3;
    public static final int MAX_MANAGED_BOT_EXPIRATION_MINUTES = 10;

    public static final long[] BG_SHOOTING_GAME_IDS = new long[] {
            GameType.BG_SECTOR_X.getGameId(),
            GameType.BG_DRAGONSTONE.getGameId(),
            GameType.BG_MISSION_AMAZON.getGameId()
    };

    public BotManagerService(HazelcastInstance hazelcastInstance, BotConfigInfoService botConfigInfoService, IBotServiceClient botServiceClient,
                             SingleNodeRoomInfoService singleNodeRoomInfoService, MultiNodeRoomInfoService multiNodeRoomInfoService,
                             IServerConfigService serverConfigService, LobbySessionService lobbySessionService, RoomPlayerInfoService roomPlayerInfoService) {
        this.hazelcast = hazelcastInstance;
        this.botConfigInfoService = botConfigInfoService;
        this.botServiceClient = botServiceClient;
        this.multiNodeRoomInfoService = multiNodeRoomInfoService;
        this.singleNodeRoomInfoService = singleNodeRoomInfoService;
        this.serverConfigService = serverConfigService;
        this.lobbySessionService = lobbySessionService;
        this.roomPlayerInfoService = roomPlayerInfoService;
    }

    @PostConstruct
    private void init() {

        activeBots = hazelcast.getMap(ACTIVE_BOTS_STORE);
        activeBots.addIndex("accountId", false);
        activeBots.addIndex("roomId", false);
        activeBots.addIndex("sessionId", false);
        activeBots.addIndex("gameId", false);
        activeBots.addIndex("dateTime", false);

        activeNicknamesLogInRequests = hazelcast.getMap(ACTIVE_NICKNAMES_LOG_IN_REQUESTS_STORE);
        activeRoomsLogInRequests = hazelcast.getMap(ACTIVE_ROOMS_LOG_IN_REQUESTS_STORE);
        activeRoomsLogOutRequests = hazelcast.getMap(ACTIVE_ROOMS_LOG_OUT_REQUESTS_STORE);
        botsRequiredInShootingRooms = hazelcast.getMap(BOTS_REQUIRED_IN_SHOOTING_ROOMS_STORE);

        singleNodeRoomInfoService.addDelegatedGameRoomStartListeners(this);
        multiNodeRoomInfoService.addDelegatedGameRoomStartListeners(this);

        lockMap = hazelcast.getMap(BOTS_LOCK_STORE);
        lockMap.putIfAbsent(getLockId(), getLockId());

        lobbySessionService.registerCloseLobbyConnectionListener(this);
    }

    public boolean isBotServiceEnabled() {
        return botConfigInfoService != null && botConfigInfoService.isBotServiceEnabled();
    }

    public IBotServiceClient getBotServiceClient() {
        return botServiceClient;
    }

    public IRoomServiceFactory getRoomServiceFactory() {
        return roomServiceFactory;
    }

    public void setRoomServiceFactory(IRoomServiceFactory roomServiceFactory) {
        this.roomServiceFactory = roomServiceFactory;
    }

    public Collection<ActiveBot> getActiveBotsInRoom(long roomId) {
        long[] roomIds = new long[] { roomId };
        return getActiveBotsInRooms(roomIds);
    }


    public Collection<ActiveBot> getActiveBotsInRooms(long[] roomIds) {
        return getActiveBotsForIds("roomId", roomIds);
    }

    public Collection<ActiveBot> getActiveBotsForGameIds(long[] gameIds) {
        return getActiveBotsForIds("gameId", gameIds);
    }

    public Collection<ActiveBot> getActiveBotsForIds(String idName, long[] ids) {

        LOG.debug("getActiveBotsForIds: idName={}, ids={}", idName, ids);

        if(activeBots == null) {
            LOG.debug("getActiveBotsForIds: activeBots is null for idName:{}, ids:{} return",
                    idName, ids);
            return new ArrayList<>();
        }

        Collection<ActiveBot> filteredActiveBots = new ArrayList<>();

        if(ids != null && ids.length > 0) {

            List<Predicate> predicates = new ArrayList<>();

            for (long id : ids) {
                // Create an equal predicate for each id
                EntryObject object = new PredicateBuilder().getEntryObject();
                predicates.add(object.get(idName).equal(id));
            }

            // Combine all predicates with an OrPredicate
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            Predicate combinedPredicate = new OrPredicate(predicatesArray);
            filteredActiveBots = this.activeBots == null ?
                    new ArrayList<>() :
                    this.activeBots.values(combinedPredicate);
        }

        LOG.debug("getActiveBotsForIds: filteredActiveBots={}", filteredActiveBots);

        return filteredActiveBots;
    }

    public Collection<ActiveBot> removeExpiredActiveBotsForGameIds(long[] gameIds, long expirationThresholdMs) {

        if(activeBots == null) {
            LOG.debug("removeExpiredActiveBotsForGameIds: activeBots is null for gameIds:{}, expirationThresholdMs:{} return",
                    gameIds, expirationThresholdMs);
            return new ArrayList<>();
        }

        long minDateTime = System.currentTimeMillis() - expirationThresholdMs;

        LOG.debug("removeExpiredActiveBotsForGameIds: expirationThresholdMs={}, minDateTime={}, gameIds={}",
                expirationThresholdMs, toHumanReadableFormat(minDateTime), gameIds);

        Collection<ActiveBot> activeBotsToRemove = new ArrayList<>();

        if(gameIds != null && gameIds.length > 0) {

            List<Predicate> gameIdPredicates = new ArrayList<>();

            for (long id : gameIds) {
                // Create an equal predicate for each id
                EntryObject gameIdObject = new PredicateBuilder().getEntryObject();
                gameIdPredicates.add(gameIdObject.get("gameId").equal(id));
            }

            // Combine all gameIdPredicates with an OrPredicate
            Predicate combinedGameIdPredicate = new OrPredicate(gameIdPredicates.toArray(new Predicate[0]));

            EntryObject dateTimeObject = new PredicateBuilder().getEntryObject();
            Predicate dateTimePredicate = dateTimeObject.get("dateTime").lessThan(minDateTime);

            Predicate combinedPredicate = new AndPredicate(dateTimePredicate, combinedGameIdPredicate);

            activeBotsToRemove = this.activeBots == null ?
                    new ArrayList<>() :
                    this.activeBots.values(combinedPredicate);

            LOG.debug("removeExpiredActiveBotsForGameIds: to remove older than {} activeBotsToRemove={}",
                    toHumanReadableFormat(minDateTime), activeBotsToRemove);

            if(activeBots != null && !activeBotsToRemove.isEmpty()) {
                for(ActiveBot expiredActiveBot : activeBotsToRemove) {
                    LOG.debug("removeExpiredActiveBotsForGameIds: remove expiredActiveBot:{} from Hazelcast", expiredActiveBot);
                    this.activeBots.remove(expiredActiveBot.getAccountId());
                }
            }
        }

        return activeBotsToRemove;
    }

    @Override
    public void notifyRoomStarted(IRoom room) {
        IRoomInfo roomInfo = room.getRoomInfo();
        if (
                (isMQBBanks(roomInfo.getBankId()) || isStubBank(roomInfo.getBankId())) &&
                (room instanceof AbstractBattlegroundGameRoom || room instanceof BattleAbstractCrashGameRoom )
        ) {
            room.registerStateChangedListener(this);
            room.registerSeatsCountChangedListener(this);
            room.registerOpenRoomListener(this);
            room.registerCloseRoomListener(this);
        }
    }

    private static boolean isCrashBtg(IRoomInfo roomInfo) {
        boolean isCrashBtg = roomInfo != null && roomInfo.getGameType() != null &&
                roomInfo.getGameType().getGameId() == GameType.BG_MAXCRASHGAME.getGameId();
        return isCrashBtg;
    }

    private static boolean isCrashBtg(IRoom room) {
        boolean isCrashBtg = room != null && isCrashBtg(room.getRoomInfo());
        return isCrashBtg;
    }

    private void updateBalanceForBotConfigInfos(List<ISeat>  seats, long roomId) {
        for (ISeat seat : seats) {
            updateBalanceForBotConfigInfo(seat, roomId);
        }
    }

    private void updateBalanceForBotConfigInfo(ISeat seat, long roomId) {
        LOG.debug("updateBalanceForBotConfigInfo: roomId={}, seat={}", roomId, seat);
        updateBalanceForBotConfigInfo(seat.getAccountId(), seat.getNickname(), roomId);
    }

    private void updateBalanceForBotConfigInfos(Collection<IGameSocketClient> observers, long roomId) {
        for (IGameSocketClient observer : observers) {
            updateBalanceForBotConfigInfo(observer, roomId);
        }
    }

    private void updateBalanceForBotConfigInfo(IGameSocketClient gameSocketClient, long roomId) {
        LOG.debug("updateBalanceForBotConfigInfo: roomId={}, gameSocketClient={}", roomId, gameSocketClient);
        updateBalanceForBotConfigInfo(gameSocketClient.getAccountId(), gameSocketClient.getNickname(), roomId);
    }

    private void updateBalanceForBotConfigInfo(long accountId, String nickname, long roomId) {

        if(!isBotServiceEnabled()) {
            LOG.debug("updateBalanceForBotConfigInfo: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return;
        }

        LOG.debug("updateBalanceForBotConfigInfo: roomId={}, accountId={}, nickname={}", roomId, accountId, nickname);

        if(activeBots == null) {
            LOG.debug("updateBalanceForBotConfigInfo: activeBots is null for accountId:{}, nickname:{}, roomId={} return",
                    accountId, nickname, roomId);
            return;
        }

        if (isBot(nickname)) {
            ActiveBot activeBot = this.activeBots.get(accountId);
            if (activeBot == null) {
                LOG.warn("updateBalanceForBotConfigInfo: roomId={}, cannot find activeBot for accountId={}",
                        roomId, accountId);
                return;
            }
            try {
                BotStatusResult botStatus = botServiceClient.getStatus(activeBot.getBotId(), activeBot.getSessionId(), nickname, roomId);
                LOG.debug("updateBalanceForBotConfigInfo: getStatus result={}, activeBot={}", botStatus, activeBot);
                if (botStatus.isSuccess()) {
                    BotConfigInfo botConfigInfo = botConfigInfoService.get(activeBot.getBotId());
                    botConfigInfoService.updateBalance(botConfigInfo.getId(), botStatus.getMmcBalance(), botStatus.getMqcBalance());
                }
            } catch (Exception e) {
                LOG.error("updateBalanceForBotConfigInfo: getStatus error, roomId={}, accountId={}, nickname={}",
                        roomId, accountId, nickname, e);
            }
        }
    }

    //key -  Expired Bots Account Ids for room
    //value -  Not Expired Bots Account Ids for room
    private Pair<List<Long>, List<Long>> getExpiredNonExpiredBotsAccountIds(long roomId) {

        List<Long> expiredBotsAccountIds;
        List<Long> notExpiredBotsAccountIds;

        Collection<ActiveBot> activeBotsInRoom = getActiveBotsInRoom(roomId);

        if(activeBotsInRoom == null || activeBotsInRoom.isEmpty()) {
            LOG.debug("getExpiredNonExpiredBotsAccountIds: roomId={}, activeBotsInRoom is null or empty:{}", roomId, activeBotsInRoom);
            expiredBotsAccountIds = new ArrayList<>();
            notExpiredBotsAccountIds = new ArrayList<>();
        } else {

            LOG.debug("getExpiredNonExpiredBotsAccountIds: roomId={}, there are {} bots in the room", roomId, activeBotsInRoom.size());

            expiredBotsAccountIds = activeBotsInRoom.stream()
                    .filter(ActiveBot::expired)
                    .map(ActiveBot::getAccountId)
                    .collect(Collectors.toList());

            notExpiredBotsAccountIds = activeBotsInRoom.stream()
                    .filter(ab -> !ab.expired())
                    .map(ActiveBot::getAccountId)
                    .collect(Collectors.toList());
        }

        LOG.debug("getExpiredNonExpiredBotsAccountIds: roomId={}, expiredBotsAccountIds={}, notExpiredBotsAccountIds={}",
                roomId, expiredBotsAccountIds, notExpiredBotsAccountIds);

        return new Pair<>(expiredBotsAccountIds, notExpiredBotsAccountIds);
    }

    private Set<Long> getSimilarRoomsWithBots(IRoom room) {

        long roomId = room.getId();
        GameType gameType = room.getGameType();
        long bankId = room.getRoomInfo().getBankId();
        long buyIn = room.getRoomInfo().getBattlegroundBuyIn();

        LOG.debug("getSimilarRoomsWithBots: roomId={}, bankId={}, buyIn={}, gameType={}", roomId, bankId, buyIn, gameType);

        Collection<ActiveBot> activeBotsForGame = getActiveBotsForGameIds(new long[] {gameType.getGameId()});

        LOG.debug("getSimilarRoomsWithBots: roomId={}, there are {} activeBotsForGame for gameType={}",
                roomId, activeBotsForGame != null ? activeBotsForGame.size() : null, gameType);

        Set<Long> similarRoomsWithBots = new HashSet<>();

        if(activeBotsForGame != null && !activeBotsForGame.isEmpty()) {
            similarRoomsWithBots = activeBotsForGame.stream()
                    .filter(activeBot ->
                             activeBot.getRoomId() != roomId
                            && activeBot.getBankId() == bankId
                            && activeBot.getBuyIn() == buyIn
                    ).map(ActiveBot::getRoomId)
                    .collect(Collectors.toSet());
        }

        LOG.debug("getSimilarRoomsWithBots: roomId={}, gameType={}, similarRoomsWithBots={}", roomId, gameType, similarRoomsWithBots);

        return similarRoomsWithBots;
    }

    //return == 0 if no sitIn/sitOut required
    //return < 0 if sitOut required
    //return > 0 if sitIn required
    private int checkBotsToLogInOrLogOut(IRoom room, int botsLimit, int playersThresholdToKeepBots) {
        int botsToLogInOrLogOut = 0;
        long roomId = room.getId();
        boolean isMaxCrash = isCrashBtg(room);

        Pair<Integer, Integer> playerAndBotsCount = getPlayerAndBotsCount(room);
        int botsCount = playerAndBotsCount.getValue();
        int realCount = playerAndBotsCount.getKey();
        int allCount = realCount + botsCount;
        Set<Long> similarRoomsWithBots = getSimilarRoomsWithBots(room);
        boolean thereAreSimilarRoomsWithBots = similarRoomsWithBots != null && !similarRoomsWithBots.isEmpty();

        LOG.debug("checkBotsToLogInOrLogOut: roomId={}, botsLimit={}, playersThresholdToKeepBots={}, " +
                        "botsCount={}, realCount={}, thereAreSimilarRoomsWithBots={}, similarRoomsWithBots={}",
                roomId, botsLimit, playersThresholdToKeepBots, botsCount, realCount, thereAreSimilarRoomsWithBots, similarRoomsWithBots);

        if (realCount == 0 && (thereAreSimilarRoomsWithBots || isMaxCrash)) {

            LOG.debug("checkBotsToLogInOrLogOut: number of real players is 0 roomId={}, thereAreSimilarRoomsWithBots={}, isMaxCrash={}",
                    roomId, thereAreSimilarRoomsWithBots, isMaxCrash);

            if (botsCount > 0) {
                LOG.debug("checkBotsToLogInOrLogOut: number of real players is 0, bots count {}, need logOut {} bot(s), roomId={}",
                        botsCount, botsCount, roomId);
                botsToLogInOrLogOut = -botsCount;
            }
        } else {

            if (botsCount > botsLimit) {

                int botsToLogOut = botsCount - botsLimit;

                LOG.debug("checkBotsToLogInOrLogOut: number of bots {}>{} (limit), need logOut {} bot(s), roomId={}",
                        botsCount, botsLimit, botsToLogOut, roomId);
                if (botsToLogOut > 0) {
                    botsToLogInOrLogOut = -botsToLogOut;
                }

            } else if (allCount > playersThresholdToKeepBots && botsCount > 0) {

                int botsToLogOut = allCount - playersThresholdToKeepBots;

                if (botsToLogOut < 0) {
                    botsToLogOut = 0;
                }

                if (botsCount < botsToLogOut) {
                    botsToLogOut = botsCount;
                }

                LOG.debug("checkBotsToLogInOrLogOut: number of all players {}>{} and number of bots {}>0 , need " +
                                "logOut {} bot(s), roomId={}",
                        allCount, playersThresholdToKeepBots, botsCount, botsToLogOut, roomId);

                if (botsToLogOut > 0) {
                    botsToLogInOrLogOut = -botsToLogOut;
                }

            } else if (allCount < playersThresholdToKeepBots && botsCount < botsLimit) {

                int botsToLogIn = playersThresholdToKeepBots - allCount;

                if (botsToLogIn < 0) {
                    botsToLogIn = 0;
                }

                if (botsToLogIn > botsLimit - botsCount) {
                    botsToLogIn = botsLimit - botsCount;
                }

                LOG.debug("checkBotsToLogInOrLogOut: number of all players {}<{} and number of bots {}<{} " +
                                "(limit), need logIn {} new bot(s), roomId={}",
                        allCount, playersThresholdToKeepBots, botsCount, botsLimit, botsToLogIn, roomId);

                if (botsToLogIn > 0) {
                    botsToLogInOrLogOut = botsToLogIn;
                }

            } else {
                LOG.debug("checkBotsToLogInOrLogOut: found {} bot(s) and {} player(s), all players threshold={} " +
                                "room bots limit={}, skip logIn and logOut, roomId={}",
                        botsCount, realCount, playersThresholdToKeepBots, botsLimit, roomId);
            }
        }

        //add one more bot if no real players are present
        //if(realCount == 0 && botsToLogInOrLogOut >= 0 && botsToLogInOrLogOut < playersThresholdToKeepBots) {
        //    botsToLogInOrLogOut++;
        //}

        return botsToLogInOrLogOut;
    }

    //key - bot's account ids for logOuts required
    //value - count of bot for logIns required
    private Pair<List<Long>, Integer> checkBotsToLogInOrLogOutAdjustedForExpiredBots(IRoom room, int botsLimit, int playersThresholdToKeepBots) {

        long roomId = room.getId();
        int botsToLogInOrLogOut = checkBotsToLogInOrLogOut(room, botsLimit, playersThresholdToKeepBots);

        Pair<List<Long>, List<Long>> expiredNonExpiredBotsAccountIds = getExpiredNonExpiredBotsAccountIds(room.getId());
        LOG.debug("checkBotsToLogInOrLogOutAdjustedForExpiredBots: roomId={}, botsToLogInOrLogOut={}, expiredNonExpiredBotsAccountIds={}",
                roomId, botsToLogInOrLogOut, expiredNonExpiredBotsAccountIds);

        List<Long> expiredBotsAccountIds = expiredNonExpiredBotsAccountIds.getKey();
        List<Long> notExpiredBotsAccountIds = expiredNonExpiredBotsAccountIds.getValue();
        LOG.debug("checkBotsToLogInOrLogOutAdjustedForExpiredBots: roomId={}, expiredBotsAccountIds={}, notExpiredBotsAccountIds={}",
                roomId, expiredBotsAccountIds, notExpiredBotsAccountIds);

        List<Long> botsAccountIdsToLogOut;
        int botsToLogIn;

        LOG.debug("checkBotsToLogInOrLogOutAdjustedForExpiredBots: roomId={}, botsToLogInOrLogOut: {}", roomId, botsToLogInOrLogOut);

        if(botsToLogInOrLogOut < 0) { //logOut bots required
            int botsToLogOut = -botsToLogInOrLogOut;

            LOG.debug("checkBotsToLogInOrLogOutAdjustedForExpiredBots: roomId={}, logOut Bots required botsToLogOut:{}, " +
                            "expiredBotsAccountIds.size() >= botsToLogOut: {}",
                    roomId, botsToLogOut, (expiredBotsAccountIds.size() >= botsToLogOut));

            if(expiredBotsAccountIds.size() >= botsToLogOut) {
                botsToLogIn = expiredBotsAccountIds.size() - botsToLogOut;
                botsAccountIdsToLogOut = new ArrayList<>(expiredBotsAccountIds);
                LOG.debug("checkBotsToLogInOrLogOut: roomId={}, botsToLogIn={}, botsAccountIdsToLogOut={}",
                        roomId, botsToLogIn, botsAccountIdsToLogOut);
            } else {
                botsToLogIn = 0;
                botsAccountIdsToLogOut = new ArrayList<>(expiredBotsAccountIds);
                int addMoreToLogOut = botsToLogOut - expiredBotsAccountIds.size();
                List<Long> notExpiredBotsAccountIdsToLogOut =
                        notExpiredBotsAccountIds.subList(0, Math.min(addMoreToLogOut, notExpiredBotsAccountIds.size()));
                botsAccountIdsToLogOut.addAll(notExpiredBotsAccountIdsToLogOut);
                LOG.debug("checkBotsToLogInOrLogOutAdjustedForExpiredBots: roomId={}, botsToLogIn={}, addMoreToLogOut={}, " +
                                "notExpiredBotsAccountIdsToLogOut={}, botsAccountIdsToLogOut={}",
                        roomId, botsToLogIn, addMoreToLogOut, notExpiredBotsAccountIdsToLogOut, botsAccountIdsToLogOut);
            }
        } else { //logIn bots Required
            botsToLogIn = botsToLogInOrLogOut + expiredBotsAccountIds.size();

            LOG.debug("checkBotsToLogInOrLogOutAdjustedForExpiredBots: roomId={}, logIn Bots required with expired bots count " +
                    "botsToLogIn:{}", roomId, botsToLogIn);

            botsAccountIdsToLogOut = new ArrayList<>(expiredBotsAccountIds);
        }

        LOG.debug("checkBotsToLogInOrLogOutAdjustedForExpiredBots: roomId={}, botsAccountIdsToLogOut={}, botsToLogIn: {}",
                roomId, botsAccountIdsToLogOut, botsToLogIn);

        return new Pair<>(botsAccountIdsToLogOut, botsToLogIn);
    }

    private void adjustMaxCrashBots(IRoom room) {

        Pair<List<Long>, Integer> botsToLogInOrLogOut =
                checkBotsToLogInOrLogOutAdjustedForExpiredBots(room, BOTS_LIMIT_IN_MBC_ROOM, PLAYERS_THRESHOLD_TO_KEEP_BOTS_IN_MBC_ROOM);

        LOG.debug("adjustMaxCrashBots: roomId={}, botsToLogInOrLogOut:{} ", room.getId(), botsToLogInOrLogOut);

        List<Long> botsAccountIdsToLogout = botsToLogInOrLogOut.getKey();
        int botsToLogIn = botsToLogInOrLogOut.getValue();

        if (botsAccountIdsToLogout != null && !botsAccountIdsToLogout.isEmpty()) {
            LOG.debug("adjustMaxCrashBots: botsAccountIdsToLogout: {} bot(s), roomId={}", botsAccountIdsToLogout, room.getId());
            logOutBots(room, botsAccountIdsToLogout);
        }

        if (botsToLogIn > 0) {
            LOG.debug("adjustMaxCrashBots: logInNewBots: {} bot(s), roomId={}", botsToLogIn, room.getId());
            logInNewBots(room, new ArrayList<>(), botsToLogIn);
        }
    }

    private void adjustShootingBots(IRoom room, ISeat<?, ?, ?, ?, ?> seat) {
        long roomId = room.getId();
        int botsRequired = 0;

        botsRequiredInShootingRooms.lock(roomId);

        RoomState roomState = room.getState();

        LOG.debug("adjustShootingBots: roomId={} in roomState={}", roomId, roomState);

        try {
            long currentTime = System.currentTimeMillis();
            Pair<Integer, Long> botsRequiredPair = botsRequiredInShootingRooms.get(roomId);
            LOG.debug("adjustShootingBots: roomId={} in botsRequiredInShootingRooms, botsRequiredPair={}", roomId, botsRequiredPair);

            if (botsRequiredPair == null || botsRequiredPair.getValue() < currentTime) {

                int intervalBegin = Math.min(MIN_BOTS_LIMIT_IN_SHOOTING_ROOM, room.getMaxSeats());
                int intervalEnd = Math.min(MAX_BOTS_LIMIT_IN_SHOOTING_ROOM, room.getMaxSeats());
                int numberOfBotsInTheRoom = RNG.nextInt(intervalBegin, intervalEnd + 1);
                int minutesToRenew = RNG.nextInt(MIN_BOTS_LIMIT_IN_SHOOTING_ROOM_THRESHOLD_MINUTES, MAX_BOTS_LIMIT_IN_SHOOTING_ROOM_THRESHOLD_MINUTES + 1);
                long duration = Duration.ofMinutes(minutesToRenew).toMillis();
                long dateTimeRenew = currentTime + duration;

                LOG.debug("adjustShootingBots: roomId={}, create or renew botsRequiredPair with" +
                                " numberOfBotsInTheRoom={} and currentTime={}, duration={}ms, dateTimeRenew={}", roomId,
                        numberOfBotsInTheRoom, toHumanReadableFormat(currentTime), duration, toHumanReadableFormat(dateTimeRenew));

                botsRequiredPair = new Pair<>(numberOfBotsInTheRoom, dateTimeRenew);

                botsRequiredInShootingRooms.put(roomId, botsRequiredPair);
            }
            botsRequired = botsRequiredPair.getKey();
            LOG.debug("adjustShootingBots: roomId={}, botsRequiredPair={}", roomId, botsRequiredPair);

        } catch (Exception e) {
            LOG.error("adjustShootingBots: failed, roomId={}", room.getId(), e);
        } finally {
            botsRequiredInShootingRooms.unlock(roomId);
            LOG.debug("adjustShootingBots: botsRequiredInShootingRooms.unlock for roomId={}", roomId);
        }

        Pair<List<Long>, Integer> botsToLogInOrLogOut =
                checkBotsToLogInOrLogOutAdjustedForExpiredBots(room, botsRequired, PLAYERS_THRESHOLD_TO_KEEP_BOTS_IN_SHOOTING_ROOM);

        LOG.debug("adjustShootingBots: roomId={}, botsToLogInOrLogOut:{} ", room.getId(), botsToLogInOrLogOut);

        if (roomState == RoomState.PLAY) {

            LOG.debug("adjustShootingBots: roomId={} is in PLAY state, skip adjustShootingBots", roomId);

        } else {

            List<Long> botsAccountIdsToLogout = botsToLogInOrLogOut.getKey();
            if (botsAccountIdsToLogout != null && !botsAccountIdsToLogout.isEmpty()) {
                LOG.debug("adjustShootingBots: botsAccountIdsToLogout: {} bot(s), roomId={}", botsAccountIdsToLogout, room.getId());
                logOutBots(room, botsAccountIdsToLogout);
            }

            int botsToLogIn = botsToLogInOrLogOut.getValue();
            if (botsToLogIn > 0) {
                LOG.debug("adjustShootingBots: logInNewBots: {} bot(s), roomId={}", botsToLogIn, room.getId());
                logInNewBots(room, new ArrayList<>(), botsToLogIn);
            }
        }
    }

    @Override
    public void notifyStateChanged(IRoom room, RoomState oldState, RoomState newState) {
        LOG.debug("notifyStateChanged: roomId={}, oldState={}, newState={}", room.getId(), oldState, newState);

        if(!isBotServiceEnabled()) {
            LOG.debug("notifyStateChanged: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return;
        }

        lockRoom(room.getId());
        try {
            if (newState.equals(RoomState.WAIT)) {

                boolean isMaxCrash = isCrashBtg(room);
                LOG.debug("notifyStateChanged: isMaxCrash={} ", isMaxCrash);

                if (isMaxCrash) {

                    adjustMaxCrashBots(room);

                    Pair<Integer, Integer> playerAndBotsCount = getPlayerAndBotsCount(room);
                    int botsCount = playerAndBotsCount.getValue();
                    int realCount = playerAndBotsCount.getKey();

                    LOG.debug("notifyStateChanged: there are {} bot(s) {} real player(s)", botsCount, realCount);
                    if(botsCount > 0) {
                        LOG.debug("notifyStateChanged: there are {} bot(s), need go to next round", botsCount);
                        @SuppressWarnings("unchecked")
                        List<ISeat> seats = room.getSeats();
                        updateBalanceForBotConfigInfos(seats, room.getId());
                    }

                } else {
                    adjustShootingBots(room , null);
                }
            }
        } catch (Exception e) {
            LOG.error("notifyStateChanged: failed, roomId={}", room.getId(), e);
        } finally {
            unlockRoom(room.getId());
        }
    }

    private void removeNicknameForRoomIdFormActiveRoomsLogInRequests(String nickname, long roomId) {
        activeRoomsLogInRequests.lock(roomId);
        LOG.debug("removeNicknameForRoomIdFormActiveRoomsLogInRequests: activeRoomsLogInRequests.lock(" +
                "roomId={}), nickname={}", roomId, nickname);
        try {
            //try to find LogIn request in activeRoomsLogInRequests related to room, remove it if it was found
            Map<String, Long> logInRequests = activeRoomsLogInRequests.get(roomId);
            if (logInRequests == null) {
                LOG.warn("removeNicknameForRoomIdFormActiveRoomsLogInRequests: logInRequests Map was not found " +
                        "in activeRoomsLogInRequests for nickname={}, roomId={}", nickname, roomId);
                activeRoomsLogInRequests.put(roomId, new HashMap<>());
            } else {
                LOG.debug("removeNicknameForRoomIdFormActiveRoomsLogInRequests: current logInRequests.size={} for " +
                                "roomId={}, logInRequests={}", logInRequests.size(), roomId, logInRequests.keySet());

                if (logInRequests.containsKey(nickname)) {
                    LOG.debug("removeNicknameForRoomIdFormActiveRoomsLogInRequests: logInRequests Map contains " +
                            "nickname {} for roomId={}, remove nickname from logInRequests " +
                            "and update logInRequests in activeRoomsLogInRequests", nickname, roomId);
                    logInRequests.remove(nickname);
                    activeRoomsLogInRequests.put(roomId, logInRequests);
                } else {
                    LOG.warn("removeNicknameForRoomIdFormActiveRoomsLogInRequests: logInRequests Map does not " +
                            "contain nickname {} for roomId={}, skip removal nickname " +
                            "from logInRequests", nickname, roomId);
                }
            }
        } catch (Exception e) {
            LOG.error("removeNicknameForRoomIdFormActiveRoomsLogInRequests: failed, roomId={}", roomId, e);
        } finally {
            activeRoomsLogInRequests.unlock(roomId);
            LOG.debug("removeNicknameForRoomIdFormActiveRoomsLogInRequests: activeRoomsLogInRequests.unlock " +
                    "for roomId={}, nickname={}", roomId, nickname);
        }
    }

    private void removeNicknameFormActiveNicknamesLogInRequests(String nickname) {
        activeNicknamesLogInRequests.lock(nickname);
        try {
            //try to find LogIn request in activeNicknamesLogInRequests related to all rooms, remove it if it was found
            if (activeNicknamesLogInRequests.containsKey(nickname)) {
                LOG.debug("removeNicknameFormActiveNicknamesLogInRequests: activeNicknamesLogInRequests Map " +
                        "contains nickname {}, remove nickname from activeNicknamesLogInRequests", nickname);
                activeNicknamesLogInRequests.remove(nickname);
            } else {
                LOG.debug("removeNicknameFormActiveNicknamesLogInRequests: activeNicknamesLogInRequests Map does " +
                        "not contain nickname {}, skip removal nickname from logInRequests", nickname);
            }
        } catch (Exception e) {
            LOG.error("removeNicknameFormActiveNicknamesLogInRequests: Exception for nickname={}", nickname, e);
        } finally {
            activeNicknamesLogInRequests.unlock(nickname);
        }
    }

    @Override
    public void notifyRoomOpened(IRoom room, IGameSocketClient client) {
        long roomId = room.getId();

        if(client == null) {
            LOG.error("notifyRoomOpened: client is null, skip");
            return;
        }

        String nickname = client.getNickname();
        Long accountId = client.getAccountId();

        LOG.debug("notifyRoomOpened: roomId.id={}, seat.nickName={}, seat.accountId={}", roomId, nickname, accountId);
        if(StringUtils.isTrimmedEmpty(nickname) || accountId == null) {
            LOG.error("notifyRoomOpened: nickname is empty or accountId is null, skip");
            return;
        }

        boolean isBot = isBot(client);
        boolean isMaxCrash = isCrashBtg(room);

        if(!isBotServiceEnabled()) {
            LOG.debug("notifyRoomOpened: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return;
        }

        lockRoom(room.getId());
        LOG.debug("notifyRoomOpened: lockRoom for roomId={}, nickname={}, isMaxCrash={}", roomId, nickname, isMaxCrash);
        try {
            if (isMaxCrash) {
                //TODO: if required
            } else {
                if(isBot) {
                    LOG.debug("notifyRoomOpened: nickName={} is bot, insert/update active bots", nickname);
                    upsertActiveBot(room, client);
                    removeNicknameForRoomIdFormActiveRoomsLogInRequests(nickname, roomId);
                    removeNicknameFormActiveNicknamesLogInRequests(nickname);
                } else {
                    LOG.debug("notifyRoomOpened: nickName={} is not a bot, adjustShootingBots", nickname);
                    adjustShootingBots(room, null);
                }
            }
        } catch (Exception e) {
            LOG.error("notifyRoomOpened: Exception for roomId={}, nickname={}", roomId, nickname, e);
        } finally {
            unlockRoom(room.getId());
            LOG.debug("notifyRoomOpened: unlockRoom for roomId={}, nickname={}", roomId, nickname);
        }
    }

    @Override
    public void notifyRoomClosed(IRoom room, IGameSocketClient client) {
        long roomId = room.getId();

        if(client == null) {
            LOG.error("notifyRoomClosed: client is null, skip");
            return;
        }

        String nickname = client.getNickname();
        Long accountId = client.getAccountId();

        LOG.debug("notifyRoomClosed: roomId.id={}, client.nickName={}, client.accountId={}", roomId, nickname, accountId);
        if(StringUtils.isTrimmedEmpty(nickname) || accountId == null) {
            LOG.error("notifyRoomClosed: nickname is empty or accountId is null, skip");
            return;
        }

        if(!isBotServiceEnabled()) {
            LOG.debug("notifyRoomClosed: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return;
        }

        lockRoom(roomId);
        LOG.debug("notifyRoomClosed: lockRoom for roomId={}, nickname={}", roomId, nickname);
        try {

            boolean isBot = isBot(client);
            boolean isMaxCrash = isCrashBtg(room);

            LOG.debug("notifyRoomClosed: isMaxCrash {} ", isMaxCrash);

            if (isBot) {
                LOG.debug("notifyRoomClosed: found bot, nickName={}", nickname);

                //for MBC or Shooting game when no observer exists
                //remove active bot from BotManagerService and from BotService
                if(isMaxCrash || room.getObserver(client.getAccountId()) == null) {
                    LOG.debug("notifyRoomClosed: remove from active bots and bot service, nickName={}", nickname);
                    removeActiveBot(room, client);
                }

                removeAccountIdForRoomIdFormActiveRoomsLogOutRequests(accountId, roomId);
            }

            if (isMaxCrash) {
                LOG.debug("notifyRoomClosed: adjustMaxCrashBots, nickName={}", nickname);
                adjustMaxCrashBots(room);
            }else {
                LOG.debug("notifyRoomClosed: adjustShootingBots, nickName={}", nickname);
                adjustShootingBots(room , null);
            }
        } catch (Exception e) {
            LOG.error("notifyRoomClosed: Exception for roomId={}, accountId={}", roomId, accountId, e);
        } finally {
            unlockRoom(room.getId());
            LOG.debug("notifyRoomClosed: unlockRoom for roomId={}, nickname={}", roomId, nickname);
        }
    }

    @Override
    public void notifyLobbyConnectionClosed(ILobbySocketClient client) {

        if(client == null) {
            LOG.error("notifyLobbyConnectionClosed: client is null, skip");
            return;
        }

        String nickname = client.getNickname();
        Long accountId = client.getAccountId();

        LOG.debug("notifyLobbyConnectionClosed: client.nickName={}, client.accountId={}", nickname, accountId);
        if(StringUtils.isTrimmedEmpty(nickname) || accountId == null) {
            LOG.error("notifyLobbyConnectionClosed: nickname is empty or accountId is null, skip");
            return;
        }

        if(!isBotServiceEnabled()) {
            LOG.debug("notifyLobbyConnectionClosed: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return;
        }

        boolean isBot = isBot(nickname);

        if (isBot) {
            LOG.debug("notifyLobbyConnectionClosed: found bot, nickName={}, remove from active bots and " +
                    "bot service by accountId={}", nickname, accountId);

            ActiveBot activeBot = removeActiveBot(-1, accountId, nickname);

            if(activeBot != null) {

                long roomId = activeBot.getRoomId();
                GameType gameType = GameType.getByGameId((int)activeBot.getGameId());

                if (roomId > 0) {
                    IRoom room = null;
                    try {

                        room = roomServiceFactory.getRoomWithoutCreation(gameType, roomId);

                    } catch (Exception e) {
                        LOG.error("notifyLobbyConnectionClosed: Exception to get room for roomId={}, for bot with " +
                                "nickName={}, accountId={}, error message={}", roomId, nickname, accountId, e.getMessage(), e);
                    }

                    if(room == null) {
                        LOG.debug("notifyLobbyConnectionClosed: room is null for roomId={}, nickName={}, accountId={}",
                                roomId, nickname, accountId);
                    } else {
                        boolean isMaxCrash = isCrashBtg(room);

                        if (isMaxCrash) {
                            //TODO
                        }else {
                            LOG.debug("notifyLobbyConnectionClosed: adjustShootingBots, nickName={}", nickname);
                            adjustShootingBots(room , null);
                        }
                    }
                } else {

                    LOG.debug("notifyLobbyConnectionClosed: roomId is {} for bot with accountId={} skip adjustShootingBots",
                            roomId, accountId);
                }
            }
        }
    }

    @Override
    public void notifySeatAdded(IRoom room, ISeat<?, ?, ?, ?, ?> seat) {
        long roomId = room.getId();

        if(seat == null) {
            LOG.error("notifySeatAdded: seat is null, skip");
            return;
        }

        String nickname = seat.getNickname();

        LOG.debug("notifySeatAdded: roomId.id={}, seat.nickName={}", roomId, nickname);
        if(StringUtils.isTrimmedEmpty(nickname)) {
            LOG.error("notifySeatAdded: nickname is empty or accountId is null, skip");
            return;
        }

        boolean isBot = isBot(seat);
        boolean isMaxCrash = isCrashBtg(room);

        LOG.debug("notifySeatAdded: roomId={}, nickname={}, isBot={}, isMaxCrash={}", roomId, nickname, isBot, isMaxCrash);

        if(!isBotServiceEnabled()) {
            LOG.debug("notifySeatAdded: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return;
        }

        lockRoom(roomId);
        LOG.debug("notifySeatAdded: lockRoom for roomId={}, nickname={}", roomId, nickname);
        try {

            if (isMaxCrash) {
                if (isBot) {
                    LOG.debug("notifySeatAdded: found bot, insert/update active bots, nickName={}", nickname);
                    upsertActiveBot(room, seat);
                    removeNicknameForRoomIdFormActiveRoomsLogInRequests(nickname, roomId);
                    removeNicknameFormActiveNicknamesLogInRequests(nickname);
                } else {
                    LOG.debug("notifySeatAdded: adjustMaxCrashBots, nickName={}", nickname);
                    adjustMaxCrashBots(room);
                }
            } else {
                //TODO: if required
            }

        } catch (Exception e) {
                LOG.error("notifySeatAdded: Exception " +
                        "for roomId={}, nickname={}", roomId, nickname, e);
        } finally {
            unlockRoom(roomId);
            LOG.debug("notifySeatAdded: unlockRoom for roomId={}, nickname={}", roomId, nickname);
        }
    }

    private void removeAccountIdForRoomIdFormActiveRoomsLogOutRequests(Long accountId, long roomId) {
        activeRoomsLogOutRequests.lock(roomId);
        LOG.debug("removeAccountIdForRoomIdFormActiveRoomsLogOutRequests: activeRoomsLogOutRequests.lock for " +
                "roomId={}, accountId={}", roomId, accountId);
        try {
            List<Long> accountIds = activeRoomsLogOutRequests.get(roomId);

            if(accountIds != null) {
                LOG.debug("removeAccountIdForRoomIdFormActiveRoomsLogOutRequests: before removing accountId={} from " +
                        "accountIds={}, roomId={}", accountId, accountIds, roomId);
                List<Long> accountIdsAfterRemove = new ArrayList<>();

                for(Long accId : accountIds) {
                    if(accId != null && !accId.equals(accountId)) {
                        accountIdsAfterRemove.add(accId);
                    }
                }

                LOG.debug("removeAccountIdForRoomIdFormActiveRoomsLogOutRequests: after removing accountId={} from " +
                        "accountIdsAfterRemove={}, roomId={}", accountId, accountIdsAfterRemove, roomId);

                activeRoomsLogOutRequests.put(roomId, accountIdsAfterRemove);
            }
        } catch (Exception e) {
            LOG.error("removeAccountIdForRoomIdFormActiveRoomsLogOutRequests: Exception " +
                    "for roomId={}, accountId={}", roomId, accountId, e);
        } finally {
            activeRoomsLogOutRequests.unlock(roomId);
            LOG.debug("removeAccountIdForRoomIdFormActiveRoomsLogOutRequests: activeRoomsLogOutRequests.unlock " +
                    "for roomId={}, accountId={}", roomId, accountId);
        }
    }

    @Override
    public void notifySeatRemoved(IRoom room, ISeat<?, ?, ?, ?, ?> seat) {
        long roomId = room.getId();

        if(seat == null) {
            LOG.error("notifySeatRemoved: seat is null, skip");
            return;
        }

        String nickname = seat.getNickname();
        long accountId = seat.getAccountId();

        LOG.debug("notifySeatRemoved: seat.nickName={}, seat.accountId={}", nickname, accountId);
        if(StringUtils.isTrimmedEmpty(nickname)) {
            LOG.error("notifySeatRemoved: nickname is empty, skip");
            return;
        }

        LOG.debug("notifySeatRemoved: roomId.id={}, seat.nickName={}, seat.accountId={}", roomId, nickname, accountId);

        if(!isBotServiceEnabled()) {
            LOG.debug("notifySeatRemoved: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return;
        }

        lockRoom(roomId);
        LOG.debug("notifySeatRemoved: lockRoom for roomId={}, nickname={}", roomId, nickname);
        try {

            boolean isBot = isBot(seat);
            boolean isMaxCrash = isCrashBtg(room);

            LOG.debug("notifySeatRemoved: isMaxCrash {} ", isMaxCrash);

            if (isBot) {
                LOG.debug("notifySeatRemoved: found bot, nickName={}", nickname);

                //for MBC or Shooting game when no observer exists
                //remove active bot from BotManagerService and from BotService
                if(isMaxCrash || room.getObserver(seat.getAccountId()) == null) {
                    LOG.debug("notifySeatRemoved: remove from active bots and bot service, nickName={}", nickname);
                    removeActiveBot(room, seat);
                }

                removeAccountIdForRoomIdFormActiveRoomsLogOutRequests(accountId, roomId);
            }

            if (isMaxCrash) {
                LOG.debug("notifySeatRemoved: adjustMaxCrashBots, nickName={}", nickname);
                adjustMaxCrashBots(room);
            }else {
                LOG.debug("notifySeatRemoved: adjustShootingBots, nickName={}", nickname);
                adjustShootingBots(room , seat);
            }
        } catch (Exception e) {
            LOG.error("notifySeatRemoved: Exception for roomId={}, nickname={}", roomId, nickname, e);
        } finally {
            unlockRoom(room.getId());
            LOG.debug("notifySeatRemoved: unlockRoom for roomId={}, nickname={}", roomId, nickname);
        }
    }

    public String getDetailBotInfo(Long botId, String botNickName) throws CommonException {

        if(!isBotServiceEnabled()) {
            LOG.debug("getDetailBotInfo: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return "";
        }

        return botServiceClient.getDetailBotInfo(botId == null ? -1 : botId, botNickName);
    }

    public boolean tryLock() throws InterruptedException {
        return lockMap.tryLock(getLockId(), 3, TimeUnit.SECONDS);
    }

    public void lock() {
        lockMap.lock(getLockId());
    }

    public void unlock() {
        lockMap.unlock(getLockId());
    }

    public void lockRoom(long roomId) {
        lockMap.lock(getRoomLockId(roomId));
    }

    public void unlockRoom(long roomId) {
        lockMap.unlock(getRoomLockId(roomId));
    }

    private void logOutBots(IRoom room, List<Long> botsAccountIdsToLogout) {

        if (botsAccountIdsToLogout == null || botsAccountIdsToLogout.isEmpty()) {
            LOG.error("logOutBots: room is null, skip");
            return;
        }

        long roomId = room.getId();
        LOG.debug("logOutBots: roomId={}, botsAccountIdsToLogout:{}", roomId, botsAccountIdsToLogout);

        if (botsAccountIdsToLogout == null || botsAccountIdsToLogout.isEmpty()) {
            LOG.debug("logOutBots: botsAccountIdsToLogout is null or empty for roomId={}, skip", roomId);
            return;
        }

        activeRoomsLogOutRequests.lock(roomId);
        LOG.debug("logOutBots: activeRoomsLogOutRequests.lock for roomId={}", roomId);

        try {
            if (!activeRoomsLogOutRequests.containsKey(roomId)) {
                LOG.debug("logOutBots: no roomId={} key found in activeRoomsLogOutRequests, created it " +
                        "with empty list value", roomId);
                activeRoomsLogOutRequests.put(roomId, new ArrayList<>());
            }

            for (Long botAccountIdToLogout : botsAccountIdsToLogout) {

                this.logOutBot(room, botAccountIdToLogout);

                List<Long> accountIds = activeRoomsLogOutRequests.get(roomId);
                LOG.debug("logOutBots: save botAccountIdToLogout={} to activeRoomsLogOutRequests in roomId={}",
                        botAccountIdToLogout, roomId);

                if(accountIds == null) {
                    accountIds = new ArrayList<>();
                }

                accountIds.add(botAccountIdToLogout);
                activeRoomsLogOutRequests.put(roomId, accountIds);
            }

        } catch (Exception e) {
            LOG.error("removeNicknameFormActiveNicknamesLogInRequests: Exception for roomId={}, " +
                    "botsAccountIdsToLogout={}", roomId, botsAccountIdsToLogout, e);
        } finally {
            activeRoomsLogOutRequests.unlock(roomId);
            LOG.debug("logOutBots: activeRoomsLogOutRequests.unlock for roomId={}", roomId);
        }
    }

    private void logOutBot(IRoom room, long accountId) {
        try {

            if(activeBots == null) {
                LOG.debug("logOutBot: activeBots is null for room: {} and accountId {} return", room, accountId);
                return;
            }

            if(!isBotServiceEnabled()) {
                LOG.debug("logOutBot: isBotServiceEnabled={}, skip", isBotServiceEnabled());
                return;
            }

            ActiveBot activeBot = this.activeBots.get(accountId);
            if (activeBot == null) {
                LOG.warn("logOutBot: possible error for accountId={}, activeBot is null, skip", accountId);
                return;
            }

            boolean isMaxCrash = isCrashBtg(room);
            LOG.debug("logOutBot: isMaxCrash:{} for accountId={}", isMaxCrash, accountId);

            //For BG MaxCrash, the bot service will choose
            //bot priority to Log Out/ to specify botId -1 if isMaxCrash
            long botId = isMaxCrash ? -1 : activeBot.getBotId();
            String sessionId = activeBot.getSessionId();
            String nickname = activeBot.getNickname();

            List<ISeat> seats = room.getSeats();
            seats.forEach(seat -> LOG.debug("logOutBot: remaining seat accountId: {}, currentSeat.getNickname(): {}, isBot: {} ",
                    seat.getAccountId(), seat.getNickname(), isBot(seat)));

            LOG.debug("logOutBot: botServiceClient.logOut for botId={}, sessionId={}, nickname={}, roomId={}",
                    botId, sessionId, nickname, room.getId());
            BotLogOutResult logOutResult = botServiceClient.logOut(botId, sessionId, nickname, room.getId());

            LOG.debug("logOutBot:  accountId={}, logOutResult={}", logOutResult, accountId);

        } catch (Exception e) {
            LOG.debug("logOutBot: failed, roomId={}, botSeat.accountId={}", room.getId(), accountId, e);
        }
    }

    private boolean removeOldRecordsIfPresent(Map<String, Long> requests, long durationSec){
        boolean oldRecordsRemoved = false;

        LOG.debug("removeOldRecordsIfPresent: initial requests: [{}]",
                requests.entrySet().stream()
                        .map(request ->
                                "{" + request.getKey() + ", " + toHumanReadableFormat(request.getValue()) + "}")
                        .collect(Collectors.joining(", ")));

        long dateTimeThreshold = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(durationSec); // minus 30 seconds
        LOG.debug("removeOldRecordsIfPresent: durationSec={}, dateTimeThreshold={}",
                durationSec, toHumanReadableFormat(dateTimeThreshold));

        long oldRecordsCount = requests.entrySet()
                .stream()
                .filter(request ->
                        request.getValue() != null && request.getValue() < dateTimeThreshold
                )
                .count();

        if(oldRecordsCount > 0) {
            LOG.warn("removeOldRecordsIfPresent: requests has {} old record(s) (older 30 sec) in requests, remove old records", oldRecordsCount);
            requests.entrySet()
                    .removeIf(request ->
                        request.getValue() != null && request.getValue() < dateTimeThreshold
                    );

            oldRecordsRemoved = true;
        } else {
            LOG.debug("removeOldRecordsIfPresent: There are no old records (older 30 sec) in requests");
        }

        LOG.debug("removeOldRecordsIfPresent: after old records removal: [{}]",
                requests.entrySet().stream()
                        .map(request ->
                                "{" + request.getKey() + ", " + toHumanReadableFormat(request.getValue()) + "}")
                        .collect(Collectors.joining(", ")));

        return oldRecordsRemoved;
    }

    private void logInNewBots(IRoom room, List<String> preferredBotNames, int count) {

        if(activeBots == null) {
            LOG.debug("logInNewBots: activeBots is null for count: {} and room {} return", count, room);
            return;
        }

        if(!isBotServiceEnabled()) {
            LOG.debug("logInNewBots: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return;
        }

        if(room == null || count <= 0) {
            LOG.debug("logInNewBots wrong parameters count: {} <= 0 or room is null {} return", count, room);
            return;
        }
        long roomId = room.getId();
        boolean isMaxCrash = isCrashBtg(room);
        LOG.debug("logInNewBots: isMaxCrash {} for roomId={}", isMaxCrash, roomId);

        try {
            if (activeBots.size() > MAX_NUMBER_BOTS) {
                LOG.debug("logInNewBots activeBots: {} more max active botConfigInfos, MAX_NUMBER_BOTS: {} ",
                        activeBots.keySet(), MAX_NUMBER_BOTS);
                return;
            }

            Collection<IGameSocketClient> observers = room.getObservers();
            observers.forEach(observer -> LOG.debug("logInNewBots: observers exists in room {} for: accountId={}, nickName={}, isBot={}",
                    roomId, observer.getAccountId(), observer.getNickname(), isBot(observer)));

        } catch(Exception e) {
            LOG.error("logInNewBots: failed to check activeBots size, roomId={}", room.getId(), e);
        }

        activeRoomsLogInRequests.lock(roomId);
        LOG.debug("logInNewBots: activeRoomsLogInRequests.lock for roomId={}", roomId);
        try {
            if(!activeRoomsLogInRequests.containsKey(roomId)) {
                LOG.debug("logInNewBots: no roomId={} key found in activeRoomsLogInRequests, create a new HashMap", roomId);
                activeRoomsLogInRequests.put(roomId, new HashMap<>());
            } else {
                //when some active logInRequests exist the room
                Map<String, Long> logInRequests = activeRoomsLogInRequests.get(roomId);

                if(logInRequests == null) {
                    LOG.warn("logInNewBots: logInRequests Map was not found in activeRoomsLogInRequests for roomId={}", roomId);
                    activeRoomsLogInRequests.put(roomId, new HashMap<>());
                } else {
                    LOG.debug("logInNewBots: roomId={}, initial logInRequests: [{}]", roomId,
                            logInRequests.entrySet().stream().map(request ->
                                            "{" + request.getKey() + ", " + toHumanReadableFormat(request.getValue()) + "}")
                            .collect(Collectors.joining(", ")));

                    //remove old records if present
                    LOG.debug("logInNewBots: remove old logInRequests records if present and update activeRoomsLogInRequests for roomId={}", roomId);
                    boolean removeResult = removeOldRecordsIfPresent(logInRequests, 30);
                    if(removeResult) {
                        activeRoomsLogInRequests.put(roomId, logInRequests);
                    }

                    LOG.debug("logInNewBots: roomId={}, after remove old records logInRequests: [{}]", roomId, logInRequests.entrySet().stream()
                            .map(request -> "{" + request.getKey() + ", " + toHumanReadableFormat(request.getValue()) + "}")
                            .collect(Collectors.joining(", ")));

                    if( logInRequests.size() > 0) {
                        LOG.debug("logInNewBots: There are {} recent record(s) activeRoomsLogInRequests for roomId={} currently, " +
                                "skip logIn new bots, set count=0", logInRequests.size(), roomId);
                        count = 0;
                    }
                }
            }

            if(count > 0) {
                boolean locked = tryLock();
                if (!locked) {
                    LOG.error("logInNewBots: tryLock failed by timeout");
                    return;
                }

                try {

                    //removeActiveBotsNotObserversInRoom(room);
                    LOG.debug("logInNewBots: activeBots: {} ", activeBots.keySet());

                    /*if (!isMaxCrash) {
                        LOG.debug("logInNewBots: For Non MaxCrash game Try to find existing activeBot by roomId={}", roomId);
                        List<ActiveBot> activeBots = findActiveBotsByRoomId(roomId);
                        if (activeBots != null && !activeBots.isEmpty()) {
                            LOG.warn("logInNewBot: Other active bots found, exit function for room.id={}, activeBots: {}", room.getId(), activeBots);
                            return;
                        }
                    }*/

                    IRoomInfo roomInfo = room.getRoomInfo();
                    Collection<BotConfigInfo> allBotConfigInfos = botConfigInfoService.getAll();

                    List<BotConfigInfo> candidates = getMostSuitableBots(allBotConfigInfos, roomInfo, preferredBotNames, count);

                    if (candidates == null || candidates.isEmpty()) {
                        LOG.warn("logInNewBots: cannot find allBotConfigInfos candidates, exit function for room.id={}", room.getId());
                        return;
                    } else {
                        candidates.forEach(candidate ->
                                LOG.debug("logInNewBots: candidate={}", candidate)
                        );
                    }

                    long buyIn = isMaxCrash ? roomInfo.getStake().toCents() : roomInfo.getBattlegroundBuyIn();
                    int botServerId = getBotServerId();
                    long gameId = roomInfo.getGameType().getGameId();

                    for (BotConfigInfo candidate : candidates) {

                        int minutesToExpire = RNG.nextInt(MIN_MANAGED_BOT_EXPIRATION_MINUTES, MAX_MANAGED_BOT_EXPIRATION_MINUTES + 1);
                        long msToExpire = Duration.ofMinutes(minutesToExpire).toMillis();
                        long expiresAt = System.currentTimeMillis() + msToExpire;
                        double shootsRate = candidate.getShootsRate(gameId);
                        double bulletsRate = candidate.getBulletsRate(gameId);

                        LOG.debug("logInNewBots: Request botServiceClient to logIn for botServerId={}, botId={}, username={}, " +
                                        "roomInfo.getBankId()={}, gameId={}, buyIn={}, nickname={}, roomId={}, " +
                                        "getLang()={}, roomUrl={}, expiresAt={}, shootsRate={}, bulletsRate={}",
                                botServerId, candidate.getId(), candidate.getUsername(), roomInfo.getBankId(),
                                gameId, buyIn, candidate.getMqNickname(), roomId, getLang(), getRoomUrl(roomInfo), toHumanReadableFormat(expiresAt), shootsRate, bulletsRate);

                        BotLogInResult logInResult = botServiceClient
                                .logIn(botServerId, candidate.getId(), candidate.getUsername(), candidate.getPassword(), roomInfo.getBankId(),
                                        roomInfo.getGameType().getGameId(), buyIn, candidate.getMqNickname(), room.getId(), getLang(),
                                        null, getRoomUrl(roomInfo), expiresAt, shootsRate, bulletsRate);

                        if (logInResult != null && logInResult.isSuccess()) {
                            //not required create new ActiveBot at this stage, Active bot will be added on bot logIn,
                            // see notifySeatAdded or notifyOpenRoom
                            LOG.debug("logInNewBots: botConfigInfoService update for botId={}, expiresAt={}",
                                    candidate.getId(), toHumanReadableFormat(expiresAt));

                            botConfigInfoService.updateTmpExpiresAt(candidate.getId(), expiresAt);

                            LOG.debug("logInNewBots: botConfigInfoService update for botId={}, mmcBalance={}, mqcBalance={}",
                                    candidate.getId(), logInResult.getMmcBalance(), logInResult.getMqcBalance());
                            botConfigInfoService.updateBalance(candidate.getId(), logInResult.getMmcBalance(), logInResult.getMqcBalance());

                            //add request to activeRoomsLogInRequests to current bot
                            Map<String, Long> logInRequests = activeRoomsLogInRequests.get(roomId);
                            LOG.debug("logInNewBots: current logInRequests.size={} {} for roomId={}",
                                    logInRequests.keySet().size(), logInRequests.keySet(), roomId);

                            LOG.debug("logInNewBots: add/update logInRequests for nickname={} for roomId={}", candidate.getMqNickname(), roomId);
                            logInRequests.put(candidate.getMqNickname(), System.currentTimeMillis());

                            activeRoomsLogInRequests.put(roomId, logInRequests);
                            LOG.debug("logInNewBots: save logInRequests.size={} {} to activeRoomsLogInRequests for roomId={}",
                                    logInRequests.keySet().size(), logInRequests.keySet(), roomId);
                        }
                        LOG.debug("logInNewBots: logInResult={}", logInResult);
                    }
                } catch (Exception e) {
                    LOG.error("logInNewBots: Exception for roomId={}", roomId, e);
                } finally {
                    unlock();
                }
            }
        } catch (Exception e) {
            LOG.error("logInNewBots: failed, roomId={}", room.getId(), e);
        } finally {
            activeRoomsLogInRequests.unlock(roomId);
            LOG.debug("logInNewBots: activeRoomsLogInRequests.unlock for roomId={}", roomId);
        }
    }

    private boolean isPresentInRoomPlayerInfoService(String nickname) {
        Collection<IRoomPlayerInfo> roomPlayerInfos =  roomPlayerInfoService.getByNickname(nickname);
        if(roomPlayerInfos != null && roomPlayerInfos.size() > 0) {
            return true;
        }
        return false;
    }

    private List<BotConfigInfo> getMostSuitableBots(Collection<BotConfigInfo> botConfigInfos, IRoomInfo roomInfo, List<String> preferredBotNames, int count) {

        boolean isMaxCrash = isCrashBtg(roomInfo);
        LOG.debug("getMostSuitableBots: isMaxCrash {} for roomId={}", isMaxCrash, roomInfo.getId());

        long buyIn = isMaxCrash ? roomInfo.getStake().toCents() : roomInfo.getBattlegroundBuyIn();

        if(preferredBotNames == null) {
            preferredBotNames = new ArrayList<>();
        }

        //remove all empty strings of preferred Bot Names
        if(preferredBotNames.size() > 0) {
            preferredBotNames = preferredBotNames.stream()
                    .filter(preferredBotName -> preferredBotName != null && !preferredBotName.isEmpty())
                    .collect(Collectors.toList());
        }

        LOG.debug("getMostSuitableBots: preferredBotNames={}", preferredBotNames.toArray());

        String roomCurrency = roomInfo.getCurrency();

        List<BotConfigInfo> preferredCandidates = new ArrayList<>();
        List<BotConfigInfo> candidates = new ArrayList<>();
        List<BotConfigInfo> candidatesWithUnknownBalances = new ArrayList<>();

        for (BotConfigInfo botConfigInfo : botConfigInfos) {
            //LOG.debug("getMostSuitableBots: Try to find activeBot by botId={}", botConfigInfo.getId());

            String botNickname = botConfigInfo.getMqNickname();
            long botId = botConfigInfo.getId();
            ActiveBot activeBot = findActiveBotByBotId(botId);

            if (activeBot != null) {
                LOG.debug("getMostSuitableBots: skip cycle for botId={}, nickname={} is in activeBots list",
                        botId, botNickname);
                continue;
            }

            boolean needStubBot = isStubBank(botConfigInfo.getBankId()) && isStubBank(roomInfo.getBankId());
            boolean needMqbBot = isMQBBanks(botConfigInfo.getBankId()) && isMQBBanks(roomInfo.getBankId());

            if (!(needStubBot || needMqbBot)) {
                LOG.debug("getMostSuitableBots: skip cycle for botId={}, nickname={} has needStubBot={}, needMqbBot={}",
                        botId, botNickname, needStubBot, needMqbBot);
                continue;
            }


            //if there is a botNickname saved in activeNicknamesLogInRequests
            if(activeNicknamesLogInRequests.containsKey(botNickname)) {
                long currentDateTimeM1 = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1); // minus 1 minute(s)
                long logInRequestDateTime =  activeNicknamesLogInRequests.get(botNickname);

                LOG.debug("getMostSuitableBots: botId={}, nickname={} has currentDateTimeM1={}, logInRequestDateTime={}, " +
                                "logInRequestDateTime <= currentDateTimeM1 is {}",
                        botId, botNickname, toHumanReadableFormat(currentDateTimeM1),
                        toHumanReadableFormat(logInRequestDateTime), logInRequestDateTime <= currentDateTimeM1);


                if(logInRequestDateTime <= currentDateTimeM1) {
                    LOG.debug("getMostSuitableBots: botId={}, nickname={} activeNickname is old, remove from " +
                            "activeNicknamesLogInRequests", botId, botNickname);

                    activeNicknamesLogInRequests.remove(botNickname);
                } else {
                    //It is recent (not older 1 min), skip cycle
                    LOG.debug("getMostSuitableBots: skip cycle for botId={}, nickname={} " +
                                    "activeNickname is recent (not older 1 min)",
                            botId, botNickname);
                    continue;
                }
            }

            boolean isPresentInRoomPlayerInfoService = isPresentInRoomPlayerInfoService(botNickname);
            if (isPresentInRoomPlayerInfoService) {
                LOG.debug("getMostSuitableBots: skip cycle for botId={}, nickname={} isPresentInRoomPlayerInfoService={}",
                        botId, botNickname, isPresentInRoomPlayerInfoService);
                continue;
            }

            long botBalance = roomCurrency.equals("MMC") ? botConfigInfo.getMmcBalance() : botConfigInfo.getMqcBalance();

            boolean isActive = botConfigInfo.isActive();
            boolean withInOperationHours = botConfigInfo.currentTimeIsWithInTimeFrames();
            boolean gameTypeIsAllowed = botConfigInfo.isAllowedGameType(roomInfo.getGameType());
            boolean bankIsAllowed = botConfigInfo.isAllowedBankId(roomInfo.getBankId());
            boolean roomValueIsAllowed = botConfigInfo.isAllowedRoomValue(roomInfo.getBankId(), buyIn);
            boolean suitableBot = isActive && withInOperationHours && gameTypeIsAllowed && bankIsAllowed && roomValueIsAllowed;

            LOG.debug("getMostSuitableBots: botId={}, nickname={} is suitableBot={} because active={}, " +
                            "withInOperationHours={}, gameTypeIsAllowed={}, bankIsAllowed={}, roomValueIsAllowed={}",
                    botId, botNickname, suitableBot, isActive, withInOperationHours, gameTypeIsAllowed, bankIsAllowed, roomValueIsAllowed);

            if(suitableBot) {
                if (botBalance >= buyIn) {
                    LOG.debug("getMostSuitableBots: botId={}, nickname={} has botBalance ({}) >= buyIn({}) is {}",
                            botId, botNickname, botBalance, buyIn, botBalance >= buyIn);

                    if (preferredBotNames.contains(botConfigInfo.getUsername())) {
                        LOG.debug("getMostSuitableBots: botId={}, nickname={} add botConfigInfo={} to preferredCandidates",
                                botId, botNickname, botConfigInfo.getUsername());
                        preferredCandidates.add(botConfigInfo);
                    } else {
                        LOG.debug("getMostSuitableBots: botId={}, nickname={} add botConfigInfo={} to candidates",
                                botId, botNickname, botConfigInfo.getUsername());
                        candidates.add(botConfigInfo);
                    }
                } else if (botBalance == -1L) {
                    LOG.debug("getMostSuitableBots:  botId={}, nickname={} has botBalance is {}, " +
                                    "add botConfigInfo={} to candidatesWithUnknownBalances",
                            botId, botNickname, botBalance, botConfigInfo.getUsername());
                    candidatesWithUnknownBalances.add(botConfigInfo);
                }
            }
        }

        List<BotConfigInfo> mostSuitableBots = new ArrayList<>();

        int toAddMore = count - mostSuitableBots.size();
        if(toAddMore > 0 && preferredCandidates.size() > 0) {
            if (toAddMore > preferredCandidates.size()) {
                toAddMore = preferredCandidates.size(); // Make sure toAddMore doesn't exceed the list size
            }
            LOG.debug("getMostSuitableBots: Add random {} botConfigInfos from preferredCandidates " +
                    "to mostSuitableBots and activeNicknamesLogInRequests", toAddMore);
            for (BotConfigInfo bcInfo : getRandomObjects(preferredCandidates, toAddMore)) {
                activeNicknamesLogInRequests.put(bcInfo.getMqNickname(), System.currentTimeMillis());
                mostSuitableBots.add(bcInfo);
                LOG.debug("getMostSuitableBots: bot={} added to mostSuitableBots and activeNicknamesLogInRequests={} " +
                        "from preferredCandidates ", bcInfo.getMqNickname(), activeNicknamesLogInRequests.keySet().toArray());
            }
        }

        toAddMore = count - mostSuitableBots.size();
        if( toAddMore > 0 && candidates.size() > 0) {
            if (toAddMore > candidates.size()) {
                toAddMore = candidates.size(); // Make sure toAddMore doesn't exceed the list size
            }
            LOG.debug("getMostSuitableBots: Add random {} botConfigInfos from candidates " +
                    "to mostSuitableBots and activeNicknamesLogInRequests", toAddMore);
            for (BotConfigInfo bcInfo : getRandomObjects(candidates, toAddMore)) {
                activeNicknamesLogInRequests.put(bcInfo.getMqNickname(), System.currentTimeMillis());
                mostSuitableBots.add(bcInfo);
                LOG.debug("getMostSuitableBots: bot={} added to mostSuitableBots and activeNicknamesLogInRequests={} " +
                        "from candidates ", bcInfo.getMqNickname(), activeNicknamesLogInRequests.keySet().toArray());
            }
        }

        toAddMore = count - mostSuitableBots.size();
        if( toAddMore > 0 && candidatesWithUnknownBalances.size() > 0) {
            if (toAddMore > candidatesWithUnknownBalances.size()) {
                toAddMore = candidatesWithUnknownBalances.size(); // Make sure toAddMore doesn't exceed the list size
            }
            LOG.debug("getMostSuitableBots: Add random {} botConfigInfos from candidatesWithUnknownBalances " +
                    "to mostSuitableBots and  activeNicknamesLogInRequests", toAddMore);
            for (BotConfigInfo bcInfo : getRandomObjects(candidatesWithUnknownBalances, toAddMore)) {
                activeNicknamesLogInRequests.put(bcInfo.getMqNickname(), System.currentTimeMillis());
                mostSuitableBots.add(bcInfo);
                LOG.debug("getMostSuitableBots: bot={} added to mostSuitableBots and activeNicknamesLogInRequests={} " +
                        "from candidatesWithUnknownBalances ", bcInfo.getMqNickname(), activeNicknamesLogInRequests.keySet().toArray());
            }
        }

        return mostSuitableBots;
    }

    private void upsertActiveBot(long roomId, long gameId, long accountId, String nickname, String sessionId, long bankId, long buyIn) {

        LOG.debug("upsertActiveBot: roomId:{}, gameId:{}, accountId={}, nickname={}, sessionId={}, bankId={}, buyIn={}",
                roomId, gameId, accountId, nickname, sessionId, bankId, buyIn );

        if(activeBots == null) {
            LOG.debug("upsertActiveBot: activeBots is null for roomId:{}, gameId:{}, accountId={}, nickname={}, sessionId={} return",
                    roomId, gameId, accountId, nickname, sessionId );
            return;
        }

        ActiveBot activeBot = activeBots.get(accountId);

        if (activeBot == null) {
            LOG.debug("upsertActiveBot: activeBot is null (not found in activeBots) for accountId={}, roomId={}, gameId={}, nickName={}, " +
                    "try get it from botConfigInfoService by nickname, and create new activeBot", accountId, roomId, gameId, nickname);
            BotConfigInfo botConfigInfo = botConfigInfoService.getByMqNickName(nickname);
            if(botConfigInfo != null) {
                long botId = botConfigInfo.getId();
                long expiresAt = botConfigInfo.getTmpExpiresAt();

                activeBot = new ActiveBot(botId, roomId, gameId, accountId, sessionId, expiresAt, nickname, bankId, buyIn);

                LOG.debug("upsertActiveBot: accountId={}, roomId={}, gameId={}, nickName={}, activeBot={}",
                        accountId, roomId, gameId, nickname, activeBot);
            }

        } else {
            LOG.debug("upsertActiveBot: activeBot has been found in activeBots for accountId={}, nickName={}, update roomId to {}",
                    accountId, nickname, roomId);
            activeBot.setRoomId(roomId);
            activeBot.setGameId(gameId);
            activeBot.setAccountId(accountId);
            activeBot.setSessionId(sessionId);
            activeBot.setDateTime(System.currentTimeMillis());
            activeBot.setBankId(bankId);
            activeBot.setBuyIn(buyIn);
        }

        if(activeBot != null) {
            activeBots.put(accountId, activeBot);
        }

        LOG.debug("upsertActiveBot: activeBot inserted/updated to activeBots, accoutnId={}, nickName={}, activeBot={}, " +
                "activeBots size={}", accountId, nickname, activeBot, activeBots.size());
    }

    private void upsertActiveBot(IRoom room, ISeat<?, ?, ?, ?, ?> seat) {
        long accountId = seat.getAccountId();
        String nickname = seat.getNickname();
        long roomId = room.getId();
        long bankId = room.getRoomInfo().getBankId();
        long buyIn = room.getRoomInfo().getBattlegroundBuyIn();
        GameType gameType = room.getGameType();
        long gameId = gameType == null ? 0 : gameType.getGameId();
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        String sessionId = playerInfo.getSessionId();

        upsertActiveBot(roomId, gameId, accountId, nickname, sessionId, bankId, buyIn);
    }

    private void upsertActiveBot(IRoom room, IGameSocketClient client) {
        long accountId = client.getAccountId();
        String nickname = client.getNickname();
        long roomId = room.getId();
        long bankId = room.getRoomInfo().getBankId();
        long buyIn = room.getRoomInfo().getBattlegroundBuyIn();
        GameType gameType = room.getGameType();
        long gameId = gameType == null ? 0 : gameType.getGameId();
        String sessionId = client.getSessionId();

        upsertActiveBot(roomId, gameId, accountId, nickname, sessionId, bankId, buyIn);
    }

    private ActiveBot removeActiveBot(long roomId, long accountId, String nickName) {

        if(activeBots == null) {
            LOG.debug("removeActiveBot: activeBots is null for roomId:{}, accountId={}, nickName={} return",
                    roomId, accountId, nickName);
            return null;
        }

        ActiveBot activeBot = activeBots.get(accountId);
        String sessionId = activeBot != null ? activeBot.getSessionId() : "";
        LOG.debug("removeActiveBot: try to remove activeBot nickName={}, accountId={} from activeBots", nickName, accountId);

        activeBots.remove(accountId);
        if (activeBot == null) {
            LOG.warn("removeActiveBot: activeBot not found in activeBots, nickName={} accountId={}", nickName, accountId);
        }

        try {

            if(!isBotServiceEnabled()) {
                LOG.debug("removeActiveBot: isBotServiceEnabled={}, skip botServiceClient.removeBot", isBotServiceEnabled());
            } else {
                long botId = activeBot == null ? -1 : activeBot.getBotId();
                LOG.debug("removeActiveBot: try to remove activeBot nickName={}, botId={} from botServiceClient", nickName, botId);
                botServiceClient.removeBot(botId, nickName, roomId);
            }

        } catch (Exception e) {
            LOG.error("removeActiveBot: removeBot failed");
        }

        if (!StringUtils.isTrimmedEmpty(sessionId)) {

            lobbySessionService.remove(sessionId);
            LOG.debug("removeActiveBot: sessionId={} for bot nickName={} from lobbySessionService", sessionId, nickName);
        }

        return activeBot;
    }

    private void removeActiveBot(IRoom room, ISeat<?, ?, ?, ?, ?> seat) {
        long accountId = seat.getAccountId();
        String nickName = seat.getNickname();
        long roomId = room.getId();

        removeActiveBot(roomId, accountId, nickName);
    }

    private void removeActiveBot(IRoom room, IGameSocketClient client) {
        long accountId = client.getAccountId();
        String nickName = client.getNickname();
        long roomId = room.getId();

        removeActiveBot(roomId, accountId, nickName);
    }

    public ActiveBot findActiveBotByBotId(long botId) {

        LOG.debug("findActiveBotByBotId: botId:{}", botId);

        if(activeBots == null) {
            LOG.debug("findActiveBotByBotId: activeBots is null for botId:{} return", botId);
            return null;
        }

        for (ActiveBot bot : activeBots.values()) {
            if (bot.getBotId() == botId) {
                return bot;
            }
        }
        return null;
    }

    public List<ActiveBot> findActiveBotsByRoomId(long roomId) {

        LOG.debug("findActiveBotsByRoomId: roomId:{}", roomId);

        if(activeBots == null) {
            LOG.debug("findActiveBotsByRoomId: activeBots is null for roomId:{} return", roomId);
            return new ArrayList<>();
        }

        List<ActiveBot> activeBotByRoomId = new ArrayList<>();

        for (ActiveBot bot : activeBots.values()) {
            if (bot.getRoomId() == roomId) {
                activeBotByRoomId.add(bot);
            }
        }
        return activeBotByRoomId;
    }

    public String getActiveBotInfo(long botId) {

        LOG.debug("getActiveBotInfo: botId:{}", botId);

        if(!isBotServiceEnabled()) {
            LOG.debug("getActiveBotInfo: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return "";
        }

        if (activeBots == null) {
            LOG.debug("getActiveBotInfo: activeBots is null for botId:{} return", botId);
            return "";
        }

        String detailBotInfo = "";
        ActiveBot bot = activeBots.get(botId);
        if (bot != null) {
            try {
                detailBotInfo = botServiceClient.getDetailBotInfo(botId, "");
            } catch (CommonException e) {
            }
        }

        return detailBotInfo;
    }

    public Collection<ActiveBot> getAllActiveBots() {
        if(activeBots == null) {
            LOG.debug("getAllActiveBots: activeBots is null return");
            return new ArrayList<>();
        }
        return activeBots.values();
    }

    public List<SimpleBot> getBotsMap() {

        if(!isBotServiceEnabled()) {
            LOG.debug("getBotsMap: isBotServiceEnabled={}, skip", isBotServiceEnabled());
            return new ArrayList<>();
        }

        try {

            BotsMap botMap = botServiceClient.getBotsMap();
            LOG.debug("getBotsMap: botMap={}", botMap);

            if(botMap == null || botMap.getBotsMap() == null || !botMap.isSuccess()) {
                return new ArrayList<>();
            }

            return botMap.getBotsMap();

        } catch (Exception e) {
            LOG.error("getBotsMap: error", e);
            return new ArrayList<>();
        }
    }

    public Map<Long, Pair<Integer, Long>> getBotsRequiredInShootingRooms() {
        // Create a copy using a HashMap
        return new HashMap<>(botsRequiredInShootingRooms);
    }

    public <T> Collection<T> getRandomObjects(Collection<T> sourceCollection, int n) {
        if (n <= 0 || n > sourceCollection.size()) {
            throw new IllegalArgumentException("Invalid value of n");
        }

        List<T> resultList = new ArrayList<>();
        List<T> tempList = new ArrayList<>(sourceCollection);

        for (int i = 0; i < n; i++) {
            int randomIndex = rnd.nextInt(tempList.size());
            resultList.add(tempList.remove(randomIndex));
        }

        return resultList;
    }

    private String getRoomUrl(IRoomInfo roomInfo) {
        IServerConfig config = getServerConfig(roomInfo);
        String host = config.getHost();
        String roomWebSocketUrl;
        String socketAddress = isCrashBtg(roomInfo) ? "mpunified" : "mpgame";
        if (host.endsWith("mp.local") || host.endsWith("mp.local.com") || host.endsWith(".mydomain")) { //hack for local/dev deploy
            if (!StringUtils.isTrimmedEmpty(serverHost) && serverHost.equals("10.2.0.170")) { //ks dev config
                roomWebSocketUrl = "wss://" + serverHost + ":8081/websocket/" + socketAddress;
            } else {
                roomWebSocketUrl = "ws://" + config.getHost() + ":8081/websocket/" + socketAddress;
            }
        } else if (host.endsWith("maxquest.com")) { //hack for testing env. deploy
            roomWebSocketUrl = "ws://" + config.getHost() + "/websocket/" + socketAddress;
        } else {
            roomWebSocketUrl = "wss://" + "games" + config.getDomain() + "/" + config.getId() + "/websocket/" + socketAddress;
        }
        return roomWebSocketUrl;
    }

    private IServerConfig getServerConfig(IRoomInfo roomInfo) {
        int serverId = serverConfigService.getServerId();
        if (roomInfo instanceof ISingleNodeRoomInfo) {
            serverId = ((ISingleNodeRoomInfo) roomInfo).getGameServerId();
        }
        return serverConfigService.getConfig(serverId);
    }

    private String getLang() {
        return "en";
    }

    private boolean isMQBBanks(long bankId) {
        return bankId == 6274 || bankId == 6275;
    }

    private boolean isStubBank(long bankId) {
        return bankId == 271;
    }

    public boolean isBot(ISeat seat) {
        return seat != null && isBot(seat.getNickname());
    }

    public boolean isBot(IGameSocketClient client) {
        return client != null && isBot(client.getNickname());
    }

    public boolean isBot(String nickname) {
        return !StringUtils.isTrimmedEmpty(nickname) && botConfigInfoService.getByMqNickName(nickname) != null;
    }

    private void cleanShootingActiveBots() {
        long minDateTimeBotsLimitMs =
                Duration.ofMinutes(
                        MAX_BOTS_LIMIT_IN_SHOOTING_ROOM_THRESHOLD_MINUTES + MAX_MANAGED_BOT_EXPIRATION_MINUTES + 1)
                        .toMillis();
        removeExpiredActiveBotsForGameIds(BG_SHOOTING_GAME_IDS, minDateTimeBotsLimitMs);
    }

    private Pair<Integer, Integer> getPlayerAndBotsCount(IRoom room) {

        int botsCount = 0;
        int realCount = 0;

        if(isCrashBtg(room)) {
            List<ISeat> seats = room.getSeats();
            for (ISeat seat : seats) {
                if (isBot(seat)) {
                    botsCount++;
                } else {
                    realCount++;
                }
            }
        } else {

            cleanShootingActiveBots();

            Collection<ActiveBot> activeBotsInRoom = getActiveBotsInRoom(room.getId());
            botsCount = activeBotsInRoom == null ? 0 : activeBotsInRoom.size();

            Collection<IGameSocketClient> observers = room.getObservers();
            for(IGameSocketClient observer : observers) {
                if(!isBot(observer)) {
                    realCount++;
                }
            }
        }

        return new Pair<>(realCount, botsCount);
    }

    private int getBotServerId() {
        //only one bot server with id=1 supported
        //return BotServerConfigService.BOT_SERVER_ID;
        return 1;
    }

    private String getLockId() {
        return "MQB_BOT";
    }

    private String getRoomLockId(long roomId) {
        return "MQB_BOT_" + roomId;
    }
}