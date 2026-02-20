package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.privateroom.Player;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.spring.context.SpringAware;
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
@SpringAware
public class BGPrivateRoomInfoService extends AbstractRoomInfoService<BGPrivateRoomInfo, RoomTemplate>
        implements IPrivateRoomInfoService {
    private static final Logger LOG = LogManager.getLogger(BGPrivateRoomInfoService.class);
    public static final String ROOM_INFO_STORE = "BGPrivateRoomInfoStore";
    public static final long ROOM_EXPIRE_TIME = TimeUnit.DAYS.toMillis(7);
    private static final int CLEAN_PERIOD_IN_MINUTES = 60;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ISocketService socketService;
    private final IPrivateRoomPlayersStatusService privateRoomPlayersStatusService;
    private int serverId;

    public BGPrivateRoomInfoService(ISocketService socketService,
                                    IPrivateRoomPlayersStatusService privateRoomPlayersStatusService) {
        super();
        this.socketService = socketService;
        this.privateRoomPlayersStatusService = privateRoomPlayersStatusService;
    }

    public BGPrivateRoomInfoService(HazelcastInstance hazelcast, int serverId, RoomTemplateService roomTemplateService,
                                    RoomPlayerInfoService roomPlayerInfoService, IdGenerator idGenerator, ISocketService socketService,
                                    IPrivateRoomPlayersStatusService privateRoomPlayersStatusService) {
        super(hazelcast, roomTemplateService, roomPlayerInfoService, idGenerator);
        this.serverId = serverId;
        this.socketService = socketService;
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
        rooms.addIndex("gameServerId", false);
        rooms.addIndex("ownerUsername", false);
        rooms.addIndex("closed", false);
        rooms.addIndex("stake", false);
        rooms.addIndex("battlegroundMode", false);
        scheduler.scheduleWithFixedDelay(new RoomCleanerTask(), 0, CLEAN_PERIOD_IN_MINUTES, TimeUnit.MINUTES);
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
    public Collection<BGPrivateRoomInfo> getServerRooms(int serverId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate =
                object.get("gameServerId").equal(serverId)
                        .and(object.get("closed").equal(false));
        return rooms.values(predicate);
    }

    @Override
    public Collection<BGPrivateRoomInfo> getThisServerRooms() {
        return getServerRooms(serverId);
    }

    @Override
    public BGPrivateRoomInfo createForTemplate(RoomTemplate template, long bankId, Money stake, String currency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createForTemplate(RoomTemplate template, Collection<BGPrivateRoomInfo> rooms, long bankId, Money stake, String currency) {
        //don`t need create new from templates, just ignore
    }

    @Override
    public BGPrivateRoomInfo tryFindThisServerRoomAndNotFull(Collection<BGPrivateRoomInfo> roomInfos, int serverId) {
        throw new UnsupportedOperationException();
    }

    public List<String> getActiveRoomsByOwner(long ownerAccountId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("ownerAccountId").equal(ownerAccountId)
                .and(object.is("battlegroundMode"));

        Collection<BGPrivateRoomInfo> filteredRooms = this.rooms.values(predicate);
        List<String> result = new ArrayList<>();
        for (BGPrivateRoomInfo room : filteredRooms) {
            if (!room.isDeactivated()) {
                result.add(room.getPrivateRoomId());
            }
        }
        return result;
    }

    public List<BGPrivateRoomInfo> getActualRoomsByOwnerAndGameParam(String ownerUsername, long bankId, long ownerAccountId, GameType gameType, Money stake, String currency) {
        EntryObject object = new PredicateBuilder().getEntryObject();

        final Predicate predicate = object.get("bankId").equal(bankId)
                .and(object.get("ownerAccountId").equal(ownerAccountId))
                .and(object.get("gameType").equal(gameType))
                .and(object.get("currency").equal(currency))
                .and(object.is("battlegroundMode"));

        Collection<BGPrivateRoomInfo> filteredRooms = this.rooms.values(predicate);

        List<BGPrivateRoomInfo> result = filteredRooms.stream()
                .filter(room ->
                                room.getStake().equals(stake)
                                && isStillValidByTime(room.getLastTimeActivity())
                                && !room.isDeactivated())
                .collect(Collectors.toList());

        return result;
    }

    public BGPrivateRoomInfo getRoomByPrivateRoomId(String privateRoomId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("privateRoomId").equal(privateRoomId);

        Collection<BGPrivateRoomInfo> filteredRooms = this.rooms.values(predicate);
        if (filteredRooms.size() > 1) {
            LOG.error("getRoomByPrivateRoomId: Find not unique privateRoomId: {}, rooms: {}.", privateRoomId, filteredRooms);
        }
        return filteredRooms.isEmpty() ? null : filteredRooms.iterator().next();
    }

    public List<BGPrivateRoomInfo> getRoomsToRemove() {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("ownerAccountId").isNotNull();

        Collection<BGPrivateRoomInfo> filteredRooms = this.rooms.values(predicate);
        List<BGPrivateRoomInfo> result = new ArrayList<>();
        for (BGPrivateRoomInfo room : filteredRooms) {
            if (!isStillValidByTime(room.getLastTimeActivity()) || isNeedToRemoveByOwner(room)) {
                result.add(room);
            }
        }
        return result;
    }

    private boolean isStillValidByTime(long lastTimeActivity) {
        return System.currentTimeMillis() < (ROOM_EXPIRE_TIME + lastTimeActivity);
    }

    private boolean isNeedToRemoveByOwner(BGPrivateRoomInfo room) {
        return room.isDeactivated() && !room.getState().equals(RoomState.PLAY) && !room.getState().equals(RoomState.QUALIFY);
    }

    public BGPrivateRoomInfo findOrCreateSuitableRoom(String ownerUsername, String ownerExternalId, long bankId,
                                                      long ownerAccountId, GameType gameType, Money stake, String currency) {

        List<BGPrivateRoomInfo> existingSuitableRooms =
                this.getActualRoomsByOwnerAndGameParam(ownerUsername, bankId, ownerAccountId, gameType, stake, currency);

        BGPrivateRoomInfo privateRoomInfo = existingSuitableRooms.stream()
                .filter(room -> room != null && room.getCountGamesPlayed() == 0)
                .findFirst()
                .orElse(null);

        if (privateRoomInfo != null) {
            LOG.debug("findOrCreateSuitableRoom: Existed bgPrivateRoomInfo found, ownerUsername: {}, bgPrivateRoomInfo: {}",
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

            long roundId = idGenerator.getNext(RoundInfo.class);
            long roomId = this.generateRoomId();
            int mapId = RNG.nextInt(1, 7);

            RoomTemplate roomTemplate = this.getOrCreateTemplate(bankId, gameType, stake, currency);

            long currentTimeMillis = System.currentTimeMillis();

            privateRoomInfo = new BGPrivateRoomInfo(roomId, roomTemplate, bankId, currentTimeMillis,
                    IRoomInfo.NOT_ASSIGNED_ID, roundId, RoomState.WAIT, mapId, stake, currency);

            privateRoomInfo.setOwnerUsername(ownerUsername);
            privateRoomInfo.setOwnerAccountId(ownerAccountId);
            privateRoomInfo.setCountGamesPlayed(0);
            privateRoomInfo.setLastTimeActivity(currentTimeMillis);

            String privateRoomId = this.generatePrivateRoomId(ownerUsername + bankId + currency + currentTimeMillis);
            privateRoomInfo.setPrivateRoomId(privateRoomId);

            this.add(privateRoomInfo);

            LOG.debug("findOrCreateSuitableRoom: New bgPrivateRoomInfo created, ownerUsername: {}, privateRoomInfo: {}",
                    ownerUsername, privateRoomInfo);
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

        BGPrivateRoomInfo bgPrivateRoomInfo =
                this.findOrCreateSuitableRoom(ownerUsername, ownerExternalId, bankId, 0, gameType, stake, currency);

        if(bgPrivateRoomInfo != null) {
            String generatedPrivateRoomId = bgPrivateRoomInfo.getPrivateRoomId();
            String joinUrl = this.buildJoinUrl(domainUrl, generatedPrivateRoomId);
            bgPrivateRoomInfo.setJoinUrl(joinUrl);
            return bgPrivateRoomInfo.getJoinUrl();
        }

        return null;
    }

    public String findOrCreateRoomAndGetId(String ownerUsername, String ownerExternalId, long bankId, long ownerAccountId, GameType gameType,
                                           Money stake, String currency) {

        BGPrivateRoomInfo bgPrivateRoomInfo =
                this.findOrCreateSuitableRoom(ownerUsername, ownerExternalId, bankId, ownerAccountId, gameType, stake, currency);

        if(bgPrivateRoomInfo != null) {
            return bgPrivateRoomInfo.getPrivateRoomId();
        }

        return null;
    }

    private RoomTemplate getOrCreateTemplate(long bankId, GameType gameType, Money stake, String currency) {
        MoneyType moneyType = MoneyType.REAL;
        int minBuyIn = (int) stake.toCents();
        int roundDurationInSecond = 90;
        RoomTemplate existedPrivateRoomTemplate = roomTemplateService.getPrivateTemplate(bankId, moneyType, gameType, minBuyIn);
        if (existedPrivateRoomTemplate == null) {
            long id = idGenerator.getNext(RoomTemplate.class);
            String name = moneyType.name() + ":" + currency;

            RoomTemplate roomTemplate = new RoomTemplate(id, bankId, gameType,
                    (short) 6, (short) 2, moneyType, gameType.getScreenWidth(), gameType.getScreenHeight(), minBuyIn,
                    1, 1, 1, name, roundDurationInSecond);

            roomTemplate.setBattlegroundBuyIn(minBuyIn);
            roomTemplate.setBattlegroundAmmoAmount(DEFAULT_BATTLEGROUND_AMMO_AMOUNT);
            roomTemplate.setPrivateRoom(true);

            roomTemplateService.put(roomTemplate);

            LOG.debug("getOrCreateTemplate: Create new private template: {}", roomTemplate);

            return roomTemplate;
        }

        LOG.debug("getOrCreateTemplate: Existed roomTemplate applied: {}", existedPrivateRoomTemplate);

        return existedPrivateRoomTemplate;
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

    class RoomCleanerTask implements Runnable {

        @Override
        public void run() {
            List<BGPrivateRoomInfo> roomsToRemove = getRoomsToRemove();
            LOG.debug("run: All rooms to remove: {}", roomsToRemove);
            for (BGPrivateRoomInfo roomInfo : roomsToRemove) {
                try {
                    remove(roomInfo.getId());
                    sendDeactivationNotification(roomInfo.getPrivateRoomId(), roomInfo.getBankId());
                    LOG.debug("run: RoomInfo: {} was deleted successfully", roomInfo.getId());
                } catch (CommonException e) {
                    LOG.error("run: Unable remove room: {}. Reason: {}", roomInfo, e.getMessage());
                }
            }
        }

        private void sendDeactivationNotification(String privateRoomId, long bankId) {
            try {
                socketService.roomWasDeactivated("test", "room expired", bankId);
            } catch (Exception e) {
                LOG.error("sendDeactivationNotification: Cannot notify room was deactivated bankId={}, privateRoomId={}", bankId, privateRoomId, e);
            }
        }
    }
}
