package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.utils.ITransportObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.MapListener;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;

/**
 * User: flsh
 * Date: 10.01.2022.
 */
public abstract class AbstractRoomInfoService<RI extends IRoomInfo, RT extends IRoomTemplate>
        implements IRoomInfoService<RI, RT> {
    public static final int DEFAULT_BATTLEGROUND_AMMO_AMOUNT = 2000;
    protected HazelcastInstance hazelcast;
    protected IExecutorService remoteExecutorService;
    protected RoomTemplateService roomTemplateService;
    protected RoomPlayerInfoService roomPlayerInfoService;
    protected IdGenerator idGenerator;
    //key is room.id
    protected IMap<Long, RI> rooms;
    //key is room.id
    protected final ConcurrentMap<Long, Set<IRoomSubscriptionCallback>> subscriptions = new ConcurrentHashMap<>();
    protected final Set<IGameRoomStartListener> delegatedGameRoomStartListeners = new HashSet<>();

    public AbstractRoomInfoService() {
    }

    public AbstractRoomInfoService(HazelcastInstance hazelcast, RoomTemplateService roomTemplateService,
                                   RoomPlayerInfoService roomPlayerInfoService, IdGenerator idGenerator) {
        this.hazelcast = hazelcast;
        this.remoteExecutorService = hazelcast.getExecutorService("default");
        this.roomTemplateService = roomTemplateService;
        this.roomPlayerInfoService = roomPlayerInfoService;
        this.idGenerator = idGenerator;
    }

    public abstract String getMapName();

    protected abstract Logger getLogger();

    protected void init() {
        rooms = hazelcast.getMap(getMapName());
        rooms.addIndex("id", true);
        rooms.addIndex("templateId", false);
        rooms.addIndex("bankId", false);
        rooms.addIndex("gameType", false);
        rooms.addIndex("moneyType", false);
        rooms.addIndex("currency", false);
        rooms.addIndex("closed", false);
        rooms.addIndex("stake", false);
        rooms.addIndex("currency", false);
        rooms.addIndex("battlegroundMode", false);
        getLogger().info("init: completed");
    }

    @Override
    public boolean isInitialized() {
        return hazelcast != null && roomTemplateService != null && roomPlayerInfoService != null
                && idGenerator != null && rooms != null;
    }

    public long generateRoomId() {
        return idGenerator.getNext(IRoomInfo.class);
    }

    @Override
    public String registerListener(MapListener listener) {
        String listenerId = rooms.addEntryListener(listener, true);
        getLogger().debug("registerListener: {}, {}", listenerId, listener);
        return listenerId;
    }

    @Override
    public boolean unregisterListener(String listenerId) {
        getLogger().debug("unregisterListener: {}", listenerId);
        return rooms.removeEntryListener(listenerId);
    }

    protected RI add(RI roomInfo) {
        getLogger().debug("add: {}", roomInfo);
        long id = roomInfo.getId();
        rooms.put(id, roomInfo);
        return rooms.get(roomInfo.getId());
    }

    @Override
    public void lock(Long id) {
        rooms.lock(id);
    }

    @Override
    public void unlock(Long id) {
        rooms.unlock(id);
    }

    @Override
    public void forceUnlock(Long id) {
        rooms.forceUnlock(id);
    }

    @Override
    public boolean isLocked(Long id) {
        return rooms.isLocked(id);
    }

    @Override
    public boolean tryLock(Long id) {
        return rooms.tryLock(id);
    }

    @Override
    public boolean tryLock(Long id, long time, TimeUnit timeunit) throws InterruptedException {
        return rooms.tryLock(id, time, timeunit);
    }

    @Override
    public Collection<RI> getAllRooms() {
        return rooms.values();
    }

    @Override
    public Collection<RI> getRooms(long bankId, RT template, Money stake, String currency) {
        if (stake.toCents() <= 0) {
            throw new IllegalArgumentException("Illegal stake value: " + stake.toCents());
        }
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("templateId").equal(template.getId()))
                .and(object.get("stake").equal(stake))
                .and(object.get("currency").equal(currency));

        Collection<RI> rooms = this.rooms.values(predicate);
        if (rooms.isEmpty()) {
            createForTemplate(template, rooms, bankId, stake, currency);
            rooms = this.rooms.values(predicate);
        }
        return rooms;
    }

    public List<Long> getRoomIds(long bankId, GameType gameType) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("bankId").equal(bankId).and(object.get("gameType").equal(gameType));

        Collection<RI> filteredRooms = rooms.values(predicate);
        List<Long> result = new ArrayList<>(filteredRooms.size());
        for (RI info : filteredRooms) {
            result.add(info.getId());
        }
        result.sort(Long::compareTo);
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<RI> getRooms(long bankId, GameType gameType, MoneyType moneyType, boolean closed,
                                   IRoomTemplateService roomTemplateService, List<Long> stakes, String currency) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("gameType").equal(gameType))
                .and(object.get("moneyType").equal(moneyType))
                .and(object.get("closed").equal(closed));

        Collection<RI> filteredRooms = this.rooms.values(predicate);
        if (filteredRooms.isEmpty() && !closed) {
            @SuppressWarnings("unchecked")
            RT template = (RT) roomTemplateService.getForBankOrDefault(bankId, gameType, moneyType, false);
            for (Long stake : stakes) {
                getRooms(bankId, template, Money.fromCents(stake), currency);
            }
            filteredRooms = this.rooms.values(predicate);
        }
        return filteredRooms;
    }


    private HashSet<RI> filterActiveRoomsForStake(Collection<RI> filteredRooms, Money stake) {
        HashSet<RI> thisStakeRooms = new HashSet<>();

        for (RI roomInfo : filteredRooms) {
            if (roomInfo.getStake().equals(stake) && !roomInfo.isDeactivated()) {
                thisStakeRooms.add(roomInfo);
            }
        }

        return thisStakeRooms;
    }

    @Override
    public Collection<RI> getBattlegroundRooms(long bankId, GameType gameType, Money stake,
                                               String currency) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("gameType").equal(gameType))
                .and(object.get("currency").equal(currency))
                .and(object.is("battlegroundMode"));

        Collection<RI> filteredRooms = this.rooms.values(predicate);
        getLogger().debug("getBattlegroundRooms: filteredRooms={}", filteredRooms);

        HashSet<RI> thisStakeRooms = filterActiveRoomsForStake(filteredRooms, stake);

        if (thisStakeRooms.isEmpty()) {

            RT template = createSpecialTemplate(bankId, gameType, currency, MoneyType.REAL, true, (int) stake.toCents());

            getLogger().debug("getBattlegroundRooms: created template={}", template);

            createForTemplate(template, thisStakeRooms, bankId, stake, currency);

            filteredRooms = this.rooms.values(predicate);
            getLogger().debug("getBattlegroundRooms: after create for template 1 filteredRooms={}", filteredRooms);

            thisStakeRooms = filterActiveRoomsForStake(filteredRooms, stake);

        } else {

            RI roomInfo = thisStakeRooms.iterator().next();
            RT template = roomInfo != null ? (RT) roomTemplateService.get(roomInfo.getTemplateId()) : null;

            if (template == null) {
                template = createSpecialTemplate(bankId, gameType, currency, MoneyType.REAL, true, (int) stake.toCents());
            }

            if (thisStakeRooms.size() < template.getMinFreeRooms()) {

                getLogger().debug("getBattlegroundRooms: need create additional Battleground rooms, found small " +
                        "free rooms condition, thisStakeRooms.size={}, template={}", thisStakeRooms.size(), template);

                createForTemplate(template, thisStakeRooms, bankId, stake, currency);

                filteredRooms = this.rooms.values(predicate);
                getLogger().debug("getBattlegroundRooms: after create for template 2 filteredRooms={}", filteredRooms);

                thisStakeRooms = filterActiveRoomsForStake(filteredRooms, stake);
            }
        }

        return thisStakeRooms;
    }

    public RT createSpecialTemplate(long bankId, GameType gameType, String currency, MoneyType moneyType,
                                    boolean battlegroundMode, int minBuyIn) {
        short maxSeats = 1;
        short minSeats = 1;
        int roundDuration = 290;

        if (moneyType.equals(MoneyType.TOURNAMENT)) {
            maxSeats = gameType.getMaxSeats();
            minSeats = gameType.getMinSeats();
        } else if (battlegroundMode) {
            maxSeats = gameType.getMaxSeats();
            minSeats = 2;
            roundDuration = 90;
        }

        RoomTemplate roomTemplate = new RoomTemplate(idGenerator.getNext(RoomTemplate.class), bankId, gameType,
                maxSeats, minSeats, moneyType, gameType.getScreenWidth(),
                gameType.getScreenHeight(), minBuyIn, 10, 5, 50, "" + moneyType.name() + ":" + currency, roundDuration);
        roomTemplate.setBattlegroundBuyIn(minBuyIn);
        roomTemplate.setBattlegroundAmmoAmount(DEFAULT_BATTLEGROUND_AMMO_AMOUNT);
        roomTemplateService.put(roomTemplate);
        getLogger().debug("createSpecialTemplate: {}", roomTemplate);
        return (RT) roomTemplate;
    }

    @Override
    public Collection<RI> getSpecialRooms(long bankId, GameType gameType, Money stake,
                                          String currency, MoneyType moneyType) {
        if (moneyType == MoneyType.REAL || moneyType == MoneyType.FREE) {
            throw new UnsupportedOperationException();
        }
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("gameType").equal(gameType))
                .and(object.get("moneyType").equal(moneyType))
                .and(object.isNot("battlegroundMode"));

        Collection<RI> filteredRooms = this.rooms.values(predicate);
        getLogger().debug("getSpecialRooms: filteredRooms={}", filteredRooms);
        HashSet<RI> thisStakeRooms = new HashSet<>();
        for (RI room : filteredRooms) {
            if (isAllowedSpecialRoom(stake, moneyType, room)) {
                thisStakeRooms.add(room);
            }
        }
        if (thisStakeRooms.isEmpty()) {
            RT template = createSpecialTemplate(bankId, gameType, currency, moneyType, false, 1);
            getLogger().debug("getSpecialRooms: created template={}", template);
            createForTemplate(template, thisStakeRooms, bankId, stake, currency);
            filteredRooms = this.rooms.values(predicate);
            getLogger().debug("getSpecialRooms: after create template filteredRooms={}", filteredRooms);
            for (RI room : filteredRooms) {
                if (room.getStake().equals(stake)) {
                    thisStakeRooms.add(room);
                }
            }
        } else {
            RI roomInfo = thisStakeRooms.iterator().next();
            RoomTemplate template = roomInfo != null ? roomTemplateService.get(roomInfo.getTemplateId()) : null;
            if (template == null) {
                template = (RoomTemplate) createSpecialTemplate(bankId, gameType, currency, moneyType, false, 1);
            }
            if (thisStakeRooms.size() < template.getMinFreeRooms()) {
                getLogger().debug("getSpecialRooms: need create additional rooms, found small free rooms condition, " +
                        "thisStakeRooms.size={}, template={}", thisStakeRooms.size(), template);
                createForTemplate((RT) template, thisStakeRooms, bankId, stake, currency);
            }
        }
        return thisStakeRooms;
    }

    @Override
    public Collection<RI> getActiveBattlegroundRooms(long bankId, GameType gameType) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        PredicateBuilder predicateBuilder = object.get("bankId").equal(bankId)
                .and(object.is("battlegroundMode"));
        if(gameType != null) {
            predicateBuilder.and(object.get("gameType").equal(gameType));
        }

        Collection<RI> filteredRooms = this.rooms.values(predicateBuilder);
        Collection<RI> activeRoomsResult = new ArrayList<>();
        for (RI room : filteredRooms) {
            if (!RoomState.PLAY.equals(room.getState())) {
                continue;
            }
            activeRoomsResult.add(room);
        }
        return activeRoomsResult;
    }

    @Override
    public void remove(long roomId) throws CommonException {
        IRoomInfo room = getRoom(roomId);
        if(room != null) {
            if (room.getSeatsCount(roomPlayerInfoService) > 0) {
                throw new CommonException("Room not empty");
            }
            getLogger().debug("remove: roomId: {} removed", roomId);
            rooms.remove(roomId);
        } else {
            getLogger().debug("remove: room is null for roomId: {}", roomId);
        }
    }

    @Override
    public RT getTemplate(long templateId) {
        RoomTemplate template = roomTemplateService.get(templateId);
        assert template != null;
        return (RT) template;
    }

    public void checkAndCreateForTemplate(long bankId, RT template, Money stake, String currency) {
        if (stake.toCents() <= 0) {
            throw new IllegalArgumentException("Illegal stake value: " + stake.toCents());
        }
        if (template == null) {
            throw new IllegalArgumentException("RoomTemplate is null");
        }
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("gameType").equal(template.getGameType()))
                .and(object.get("moneyType").equal(template.getMoneyType()));

        Collection<RI> rooms = this.rooms.values(predicate);
        createForTemplate(template, rooms, bankId, stake, currency);
    }

    public RI getRoom(long id) {
        return rooms.get(id);
    }

    @Override
    public void update(RI roomInfo) {
        if(roomInfo != null) {
            long roomId = roomInfo.getId();
            if (rooms.isLocked(roomId)) {
                rooms.put(roomId, roomInfo);
            } else {
                getLogger().warn("update: skip update, entry must be locked. roomInfo={}", roomInfo);
            }
        } else {
            getLogger().warn("update: skip update, roomInfo is null");
        }
    }

    @Override
    public void seatAdded(long roomId) {
        notifySeatsChanged(roomId);
    }

    @Override
    public void seatRemoved(long roomId) {
        notifySeatsChanged(roomId);
    }

    private boolean isSingleNodeRoomGame(long roomId) {
        return getRoom(roomId).getGameType().isSingleNodeRoomGame();
    }

    @Override
    public NewSeatNotifyTask createNewSeatNotifyTask(long roomId, long serverId, ISeat seat) {
        return new NewSeatNotifyTask(roomId, serverId, isSingleNodeRoomGame(roomId), seat);
    }

    @Override
    public Runnable createSendSeatOwnerMessageTask(long roomId, GameType gameType, long senderServerId, ITransportObject message) {
        return new SendSeatOwnerMessageTask(roomId, gameType, senderServerId, message);
    }

    @Override
    public SendSeatsMessageTask createSendSeatsMessageTask(long roomId, GameType gameType, long senderServerId, Long relatedAccountId,
                                                           boolean notSendToRelatedAccountId, long relatedRequestId, ITransportObject message,
                                                           boolean sendToAllObservers) {
        return new SendSeatsMessageTask(roomId, gameType, senderServerId, relatedAccountId, notSendToRelatedAccountId, relatedRequestId, message,
                sendToAllObservers);
    }

    @Override
    public Runnable createSendSeatMessageTask(long roomId, GameType gameType, long senderServerId, Long accountId, ITransportObject message) {
        return new SendSeatMessageTask(roomId, gameType, senderServerId, accountId, message);
    }

    @Override
    public Runnable createSendAllObserversNoSeatMessageTask(long roomId, GameType gameType, long senderServerId, ITransportObject message) {
        return new SendAllObserversNoSeatMessageTask(roomId, gameType, senderServerId, message);
    }

    @Override
    public Runnable createUpdateCrashHistoryTask(long roomId, GameType gameType, long senderServerId, ICrashRoundInfo crashRoundInfo) {
        return new SendUpdateCrashHistoryTask(roomId, gameType, senderServerId, crashRoundInfo);
    }

    @Override
    public SeatRemovedNotifyTask createSeatRemovedNotifyTask(long roomId) {
        return new SeatRemovedNotifyTask(roomId, isSingleNodeRoomGame(roomId));
    }

    @Override
    public RI getBestRoomInfo(Long bankId, int serverId, MoneyType moneyType, GameType gameType, Money stake,
                              String currency) {
        RoomTemplate template = roomTemplateService.getMostSuitable(bankId, stake, moneyType, gameType);
        getLogger().debug("getBestRoomInfo: bankId={}, stake={}, gameType={}, moneyType={}, template={}",
                bankId, stake, gameType, moneyType, template);

        Collection<RI> roomInfos = getRooms(bankId, (RT) template, stake, currency);
        getLogger().debug("getBestRoomInfo: roomInfos={}", roomInfos);
        return tryFindThisServerRoomAndNotFull(roomInfos, serverId);
    }

    @Override
    public RI getBestRoomForBattleground(Collection<RI> roomInfos) {
        getLogger().debug("getBestRoomForBattleground: roomInfos={}", roomInfos);
        //remove rooms in not wait state and full

        List<Pair<RI, Short>> notFullRooms = new ArrayList<>();

        for (RI roomInfo : roomInfos) {

            short maxSeats = roomInfo.getMaxSeats();
            short seatsCount = roomInfo.getSeatsCount(roomPlayerInfoService);

            getLogger().debug("getBestRoomForBattleground: RoomState={}, seats count:{}, maxSeats:{}, roomInfo={}",
                    roomInfo.getState(), seatsCount, maxSeats, roomInfo);

            if (seatsCount < maxSeats) {

                boolean hasPlayersWithPendingOperation = roomPlayerInfoService
                        .hasPlayersWithPendingOperation(roomInfo.getId());

                GameType gameType = roomInfo.getGameType();

                getLogger().debug("getBestRoomForBattleground: hasPlayersWithPendingOperation={}: " +
                        "gameType={}, roomInfo={}", hasPlayersWithPendingOperation, gameType.name(), roomInfo);

                if (!hasPlayersWithPendingOperation
                        || GameType.BG_MISSION_AMAZON == gameType
                        || GameType.BG_DRAGONSTONE == gameType
                        || GameType.BG_SECTOR_X == gameType
                ) {
                    getLogger().debug("getBestRoomForBattleground: add roomInfo to the list notFullRooms, roomInfo={}", roomInfo);
                    notFullRooms.add(new Pair<>(roomInfo, seatsCount));
                }
            } else {
                getLogger().debug("getBestRoomForBattleground: skip roomInfo as it is in the " +
                        "room State={} or seats count:{} >= maxSeats:{}, roomInfo={}",
                        roomInfo.getState(), seatsCount, maxSeats, roomInfo);
            }
        }

        notFullRooms.sort(Comparator
                .comparingInt((Pair<RI, Short> roomPair) -> roomPair.getValue())
                .reversed()
                .thenComparingLong((Pair<RI, Short> roomPair) -> roomPair.getKey().getId())
        );

        getLogger().debug("getBestRoomForBattleground: notFullRooms={}", notFullRooms);
        return notFullRooms.isEmpty() ? null : notFullRooms.get(0).getKey();
    }


    protected boolean isAllowedSpecialRoom(Money stake, MoneyType moneyType, RI room) {
        return room.getStake().equals(stake) && (moneyType == MoneyType.TOURNAMENT || room.getSeatsCount(roomPlayerInfoService) == 0);
    }

    protected void notifySeatsChanged(long roomId) {
        Set<IRoomSubscriptionCallback> callbackSet = subscriptions.get(roomId);
        IRoomInfo roomInfo = getRoom(roomId);
        if (callbackSet != null) {
            for (IRoomSubscriptionCallback callback : callbackSet) {
                getLogger().debug("notifySeatsChanged: callback={}", callback);
                try {
                    callback.notifySeatsChanged(roomInfo);
                } catch (Exception e) {
                    getLogger().debug("notifySeatsChanged error, roomId={}, sessionId={}", roomId, callback.getSessionId(), e);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void notifyRoomStarted(IRoom room) {
        for (IGameRoomStartListener listener : delegatedGameRoomStartListeners) {
            listener.notifyRoomStarted(room);
        }
    }

    public void addDelegatedGameRoomStartListeners(IGameRoomStartListener listener) {
        delegatedGameRoomStartListeners.add(listener);
    }

    @Override
    public Callable<Integer> createObserversCollectTask(long roomId, int gameId) {
        return new ObserversCollectTask(roomId, gameId);
    }


    static class RoomsStat {
        int allRooms;
        int fullRooms;
        int emptyRooms;

        @Override
        public String toString() {
            return "RoomsStat [" + "allRooms=" + allRooms +
                    ", fullRooms=" + fullRooms +
                    ", emptyRooms=" + emptyRooms +
                    ']';
        }
    }

    @Override
    public Callable<Collection> createObserverClientListCollectionTask(long roomId, int gameId) {
        return new ObserverClientListCollectTask(roomId,gameId);
    }

    @Override
    public void executeOnAllMembers(Runnable task) {
        if (remoteExecutorService != null && !remoteExecutorService.isShutdown()) {
            try {
                remoteExecutorService.executeOnAllMembers(task);
            } catch (RejectedExecutionException e) {
                getLogger().warn("executeOnAllMembers: Skip sending {}, remoteExecutorService not started", task.getClass().getSimpleName());
            }
        }
    }

    @Override
    public IRoomPlayerInfoService getRoomPlayerInfoService() {
        return roomPlayerInfoService;
    }
}
