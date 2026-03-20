package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.privateroom.Player;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.IMultiNodeRoom;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.RNG;
import com.abs.casino.common.util.string.StringUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MultiNodePrivateRoomInfoService extends AbstractRoomInfoService<MultiNodePrivateRoomInfo, RoomTemplate>
        implements IPrivateRoomInfoService {
    private static final Logger LOG = LogManager.getLogger(MultiNodePrivateRoomInfoService.class);
    public static final String ROOM_INFO_STORE = "mnPrRoomInfoStore";
    public static final long ROOM_EXPIRE_TIME = TimeUnit.DAYS.toMillis(7);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ISocketService socketService;
    protected final SharedGameStateService sharedGameStateService;

    private final IPrivateRoomPlayersStatusService privateRoomPlayersStatusService;

    public MultiNodePrivateRoomInfoService(ISocketService socketService,
                                           IPrivateRoomPlayersStatusService privateRoomPlayersStatusService) {
        super();
        this.socketService = socketService;
        this.privateRoomPlayersStatusService = privateRoomPlayersStatusService;
        sharedGameStateService = null;
    }

    public MultiNodePrivateRoomInfoService(HazelcastInstance hazelcast, int serverId, RoomTemplateService roomTemplateService,
                                           RoomPlayerInfoService roomPlayerInfoService, IdGenerator idGenerator, ISocketService socketService,
                                           SharedGameStateService sharedGameStateService, IPrivateRoomPlayersStatusService privateRoomPlayersStatusService) {
        super(hazelcast, roomTemplateService, roomPlayerInfoService, idGenerator);
        this.socketService = socketService;
        this.sharedGameStateService = sharedGameStateService;
        this.privateRoomPlayersStatusService = privateRoomPlayersStatusService;
    }

    @PostConstruct
    protected void init() {
        super.init();

        rooms.addIndex("id", true);
        rooms.addIndex("bankId", false);
        rooms.addIndex("gameType", false);
        rooms.addIndex("moneyType", false);
        rooms.addIndex("currency", false);
        rooms.addIndex("ownerUsername", false);
        rooms.addIndex("closed", false);
        rooms.addIndex("stake", false);
        rooms.addIndex("battlegroundMode", false);
        getLogger().info("init: completed");
    }

    @PreDestroy
    protected void shutdown() {
        LOG.debug("Shutdown");
        scheduler.shutdown();
    }

    @Override
    public String getMapName() {
        return ROOM_INFO_STORE;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public Collection<MultiNodePrivateRoomInfo> getServerRooms(int serverId) {
        return rooms.values();
    }

    @Override
    public Collection<MultiNodePrivateRoomInfo> getThisServerRooms() {
        return rooms.values();
    }

    @Override
    public MultiNodePrivateRoomInfo createForTemplate(RoomTemplate template, long bankId, Money stake, String currency) {
        throw new UnsupportedOperationException();
    }

    public MultiNodePrivateRoomInfo createForTemplate(String ownerUsername, long ownerAccountId, RoomTemplate template, long bankId,
                                                      Money stake, String currency, String domainUrl) {
        if (stake.toCents() <= 0) {
            throw new IllegalArgumentException("Illegal stake value: " + stake.toCents());
        }

        long roundId = idGenerator.getNext(RoundInfo.class);
        long roomId = generateRoomId();
        int mapId = RNG.nextInt(1, 7);

        long currentTimeMillis = System.currentTimeMillis();
        String generatedPrivateRoomId = this.generatePrivateRoomId(ownerUsername + bankId + currency + currentTimeMillis);

        String joinUrl = (domainUrl == null) ?
                null : this.buildJoinUrl(domainUrl, generatedPrivateRoomId);

        MultiNodePrivateRoomInfo roomInfo = new MultiNodePrivateRoomInfo(roomId, template, bankId, System.currentTimeMillis(),
                roundId, RoomState.WAIT, mapId, stake, currency);

        roomInfo.setOwnerUsername(ownerUsername);
        roomInfo.setOwnerAccountId(ownerAccountId);
        roomInfo.setCountGamesPlayed(0);
        roomInfo.setLastTimeActivity(currentTimeMillis);
        roomInfo.setPrivateRoomId(generatedPrivateRoomId);
        roomInfo.setJoinUrl(joinUrl);

        return this.add(roomInfo);
    }

    @Override
    public void createForTemplate(RoomTemplate template, Collection<MultiNodePrivateRoomInfo> rooms, long bankId, Money stake, String currency) {
        //don`t need create new from templates, just ignore
    }

    @Override
    public MultiNodePrivateRoomInfo tryFindThisServerRoomAndNotFull(Collection<MultiNodePrivateRoomInfo> roomInfos, int serverId) {
        MultiNodePrivateRoomInfo bestRoom = null;
        MultiNodePrivateRoomInfo anyRoom = null;
        for (MultiNodePrivateRoomInfo roomInfo : roomInfos) {
            if (anyRoom == null) {
                anyRoom = roomInfo;
            }
            short seatsCount = roomInfo.getSeatsCount(roomPlayerInfoService);
            LOG.debug("tryFindThisServerRoomAndNotFull: seatsCount={}, room.id={}, serverId={},", seatsCount, roomInfo.getId(), serverId);
            if (roomInfo.getMaxSeats() == 1) { //special case, single player rooms
                if (seatsCount == 0) {
                    bestRoom = roomInfo;
                    break;
                } else {
                    continue;
                }
            }

            if (roomInfo.getGameType().isCrashGame() && sharedGameStateService != null) {
                SharedCrashGameState crashGameState = sharedGameStateService.get(roomInfo.getId(), SharedCrashGameState.class);
                if (crashGameState != null) {
                    if(crashGameState.getTotalObservers() >= roomInfo.getMaxSeats()){
                        continue;
                    }
                }
            }
            if (seatsCount >= roomInfo.getMaxSeats()) {
                continue;
            }
            if (seatsCount > 0) {
                bestRoom = roomInfo;
                break;
            } else if (bestRoom == null) {
                bestRoom = roomInfo;
            }
        }
        if (bestRoom == null && anyRoom != null) {
            bestRoom = createForTemplate(getTemplate(anyRoom.getTemplateId()), anyRoom.getBankId(), anyRoom.getStake(), anyRoom.getCurrency());
            LOG.debug("tryFindThisServerRoomAndNotFull: bestRoom not found, create new, bestRoom.id={}", bestRoom.getId());
        }
        return bestRoom;
    }

    @SuppressWarnings("rawtypes")
    public MultiNodePrivateRoomInfo tryFindThisServerRoomAndNotFull(Collection<MultiNodePrivateRoomInfo> roomInfos, IRoomServiceFactory roomServiceFactory) {
        MultiNodePrivateRoomInfo bestRoom = null;
        MultiNodePrivateRoomInfo anyRoom = null;
        int badRoomsCount = 0;
        int fullRoomsCount = 0;
        int totalRoomsCount = 0;

        for (MultiNodePrivateRoomInfo roomInfo : roomInfos) {
            if (anyRoom == null) {
                anyRoom = roomInfo;
            }
            totalRoomsCount++;
            short seatsCount = roomInfo.getSeatsCount(roomPlayerInfoService);
            LOG.debug("tryFindThisServerRoomAndNotFull: seatsCount={}, room.id={}", seatsCount, roomInfo.getId());
            if (roomInfo.getMaxSeats() == 1) { //special case, single player rooms
                if (seatsCount == 0) {
                    bestRoom = roomInfo;
                    break;
                } else {
                    continue;
                }
            }
            IMultiNodeRoom multiNodeRoom;
            try {
                multiNodeRoom = (IMultiNodeRoom) roomServiceFactory.getRoom(roomInfo.getGameType(), roomInfo.getId());
            } catch (CommonException e) {
                LOG.error("tryFindThisServerRoomAndNotFull: unable find room: getGameType={}, room.id={}, error={}", roomInfo.getGameType(), roomInfo.getId(), e);
                continue;
            }
            if (multiNodeRoom == null) {
                badRoomsCount++;
                continue;
            }
            if (multiNodeRoom.isRoomFullOrManyObservers()) {
                fullRoomsCount++;
                continue;
            }
            if (seatsCount > 0) {
                bestRoom = roomInfo;
                break;
            } else if (bestRoom == null) {
                bestRoom = roomInfo;
            }
        }

        if (bestRoom == null && anyRoom != null) {
            LOG.debug("tryFindThisServerRoomAndNotFull: bestRoom not found, totalRoomsCount={}, badRoomsCount={}, fullRoomsCount={}",
                    totalRoomsCount, badRoomsCount, fullRoomsCount);
            if (badRoomsCount > 2) {
                LOG.error("tryFindThisServerRoomAndNotFull: bestRoom not found and badRoomsCount > 2, stop creating new room");
            } else {
                bestRoom = createForTemplate(getTemplate(anyRoom.getTemplateId()), anyRoom.getBankId(), anyRoom.getStake(), anyRoom.getCurrency());
                LOG.debug("tryFindThisServerRoomAndNotFull: bestRoom not found, created new, bestRoom{}", bestRoom);
            }
        }

        return bestRoom;
    }

    public List<String> getActiveRoomsByOwner(long ownerAccountId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("ownerAccountId").equal(ownerAccountId)
                .and(object.is("battlegroundMode"));

        Collection<MultiNodePrivateRoomInfo> filteredRooms = this.rooms.values(predicate);
        List<String> result = new ArrayList<>();
        for (MultiNodePrivateRoomInfo room : filteredRooms) {
            if (!room.isDeactivated()) {
                result.add(room.getPrivateRoomId());
            }
        }
        return result;
    }

    public List<MultiNodePrivateRoomInfo> getActualRoomsByOwnerAndGameParam(String ownerUsername, long bankId, long ownerAccountId, GameType gameType, Money stake, String currency) {
        EntryObject object = new PredicateBuilder().getEntryObject();

        final Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("ownerAccountId").equal(ownerAccountId))
                .and(object.get("gameType").equal(gameType))
                .and(object.get("currency").equal(currency))
                .and(object.is("battlegroundMode"));

        Collection<MultiNodePrivateRoomInfo> filteredRooms = this.rooms.values(predicate);

        List<MultiNodePrivateRoomInfo> result = filteredRooms.stream()
                .filter(room ->
                        room.getStake().equals(stake) && isStillValidByTime(room.getLastTimeActivity()) && !room.isDeactivated())
                .collect(Collectors.toList());

        return result;
    }

    public MultiNodePrivateRoomInfo getRoomByPrivateRoomId(String privateRoomId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("privateRoomId").equal(privateRoomId);

        Collection<MultiNodePrivateRoomInfo> filteredRooms = this.rooms.values(predicate);
        if (filteredRooms.size() > 1) {
            LOG.error("getRoomByPrivateRoomId: Find not unique privateRoomId: {}, rooms: {}.", privateRoomId, filteredRooms);
        }
        return filteredRooms.isEmpty() ? null : filteredRooms.iterator().next();
    }

    private boolean isStillValidByTime(long lastTimeActivity) {
        return System.currentTimeMillis() < (ROOM_EXPIRE_TIME + lastTimeActivity);
    }

    public MultiNodePrivateRoomInfo findOrCreateSuitableRoom(String ownerUsername, String ownerExternalId, long bankId,
                                                             long ownerAccountId, GameType gameType, Money stake, String currency, String domainUrl) {

        List<MultiNodePrivateRoomInfo> existingSuitableRooms =
                this.getActualRoomsByOwnerAndGameParam(ownerUsername, bankId, ownerAccountId, gameType, stake, currency);

        MultiNodePrivateRoomInfo privateRoomInfo = existingSuitableRooms.stream()
                .filter(room -> room != null && room.getCountGamesPlayed() == 0)
                .findFirst()
                .orElse(null);

        if (privateRoomInfo != null) {
            LOG.debug("findOrCreateSuitableRoom: Existing multiNodePrivateRoomInfo found, ownerUsername: {}, multiNodePrivateRoomInfo: {}",
                    ownerUsername, privateRoomInfo);
            this.lock(privateRoomInfo.getId());
            try{
                privateRoomInfo.updateLastTimeActivity();
                this.update(privateRoomInfo);
            } catch (Exception e) {
                LOG.error("findOrCreateSuitableRoom: Exception", e);
            } finally {
                this.unlock(privateRoomInfo.getId());
            }
        } else {

            RoomTemplate roomTemplate = this.getOrCreateTemplate(bankId, gameType, stake, currency);

            privateRoomInfo =
                    this.createForTemplate(ownerUsername, ownerAccountId, roomTemplate, bankId, stake, currency, domainUrl);

            LOG.debug("findOrCreateSuitableRoom: New multiNodePrivateRoomInfo created, ownerUsername: {}, " +
                    "privateRoomInfo: {}", ownerUsername, privateRoomInfo);
        }

        if(privateRoomInfo != null && !StringUtils.isTrimmedEmpty(privateRoomInfo.getPrivateRoomId())) {
            List<Player> players = new ArrayList<>();
            Player player = new Player();
            player.setAccountId(ownerAccountId);
            player.setNickname(ownerUsername);
            player.setExternalId(ownerExternalId);
            player.setStatus(Status.ACCEPTED);
            players.add(player);

            PrivateRoom privateRoom = new PrivateRoom(privateRoomInfo.getId(), privateRoomInfo.getPrivateRoomId(),
                    ownerAccountId, ownerUsername,
                    ownerExternalId, players, System.currentTimeMillis());

            updatePlayersStatusInPrivateRoom(privateRoom, false, true);
        }

        return privateRoomInfo;
    }

    public String findOrCreateRoomAndGetUrl(String ownerUsername, String ownerExternalId, long bankId, GameType gameType, Money stake,
                                            String currency, String domainUrl) {

        MultiNodePrivateRoomInfo multiNodePrivateRoomInfo =
                this.findOrCreateSuitableRoom(ownerUsername,  ownerExternalId, bankId, 0, gameType, stake, currency, domainUrl);

        if(multiNodePrivateRoomInfo != null) {
            return multiNodePrivateRoomInfo.getJoinUrl();
        }

        return null;
    }

    public String findOrCreateRoomAndGetId(String ownerUsername, String ownerExternalId, long bankId, long ownerAccountId, GameType gameType,
                                           Money stake, String currency) {

        MultiNodePrivateRoomInfo multiNodePrivateRoomInfo =
                this.findOrCreateSuitableRoom(ownerUsername, ownerExternalId, bankId, ownerAccountId, gameType, stake, currency, null);

        if(multiNodePrivateRoomInfo != null) {
            return multiNodePrivateRoomInfo.getPrivateRoomId();
        }

        return null;
    }

    private RoomTemplate getOrCreateTemplate(long bankId, GameType gameType, Money stake, String currency) {
        MoneyType moneyType = MoneyType.REAL;
        int minBuyIn = (int) stake.toCents();
        int roundDurationInSecond = 90;
        RoomTemplate existingPrivateRoomTemplate = roomTemplateService.getPrivateTemplate(bankId, moneyType, gameType, minBuyIn);
        if (existingPrivateRoomTemplate == null) {
            long id = idGenerator.getNext(RoomTemplate.class);
            String name = moneyType.name() + ":" + currency;

            RoomTemplate roomTemplate = new RoomTemplate(id, bankId, gameType,
                    gameType.getMaxSeats(), gameType.getMinSeats(), moneyType, gameType.getScreenWidth(), gameType.getScreenHeight(), minBuyIn,
                    1, 1, 1, name, roundDurationInSecond);

            roomTemplate.setBattlegroundBuyIn(minBuyIn);
            roomTemplate.setBattlegroundAmmoAmount(0);
            roomTemplate.setPrivateRoom(true);

            roomTemplateService.put(roomTemplate);

            LOG.debug("getOrCreateTemplate: Create new private template: {}", roomTemplate);

            return roomTemplate;
        }

        LOG.debug("getOrCreateTemplate: Existed roomTemplate applied: {}", existingPrivateRoomTemplate);

        return existingPrivateRoomTemplate;
    }

    @Override
    public IPrivateRoomPlayersStatusService getPrivateRoomPlayersStatusService() {
        return privateRoomPlayersStatusService;
    }

    @Override
    public UpdatePrivateRoomResponse updatePlayersStatusInPrivateRoom(PrivateRoom privateRoom,
                                                                      boolean isTransitionLimited, boolean updateTime) {

        LOG.debug("updatePlayersStatusInPrivateRoom: isTransitionLimited:{} " +
                "update Players Status In Private Room: {}", isTransitionLimited, privateRoom);

        UpdatePrivateRoomResponse updatePrivateRoomResponse =
                privateRoomPlayersStatusService.updatePlayersStatusInPrivateRoom(privateRoom, isTransitionLimited, updateTime);

        return updatePrivateRoomResponse;
    }

    @Override
    public PrivateRoom getPlayersStatusInPrivateRoom(String privateRoomId) {
        LOG.debug("getPlayersStatusInPrivateRoom: get Players Status In Private Room for privateRoomId={}", privateRoomId);
        return privateRoomPlayersStatusService.getPrivateRoom(privateRoomId);
    }
}
