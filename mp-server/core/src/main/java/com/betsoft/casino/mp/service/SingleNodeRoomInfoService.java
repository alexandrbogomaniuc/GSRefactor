package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.dgphoenix.casino.common.util.RNG;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * User: flsh
 * Date: 09.11.17.
 */
@Service
public class SingleNodeRoomInfoService extends AbstractRoomInfoService<SingleNodeRoomInfo, RoomTemplate> {
    private static final Logger LOG = LogManager.getLogger(SingleNodeRoomInfoService.class);
    public static final String ROOM_INFO_STORE = "snRoomInfoStore";
    private int serverId;

    //only for tests
    public SingleNodeRoomInfoService() {
        super();
    }

    public SingleNodeRoomInfoService(HazelcastInstance hazelcast, int serverId, RoomTemplateService roomTemplateService,
                                     RoomPlayerInfoService roomPlayerInfoService, IdGenerator idGenerator) {
        super(hazelcast, roomTemplateService, roomPlayerInfoService, idGenerator);
        this.serverId = serverId;
    }

    @PostConstruct
    protected void init() {
        super.init();
        rooms.addIndex("gameServerId", false);
        getLogger().info("init: completed");
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
    public Collection<SingleNodeRoomInfo> getServerRooms(int serverId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate =
                object.get("gameServerId").equal(serverId)
                        .and(object.get("closed").equal(false));
        return rooms.values(predicate);
    }

    @Override
    public Collection<SingleNodeRoomInfo> getThisServerRooms() {
        return getServerRooms(serverId);
    }

    protected boolean isAllowedSpecialRoom(Money stake, MoneyType moneyType, SingleNodeRoomInfo room) {
        return room.getStake().equals(stake) && (room.getGameServerId() == serverId || room.getGameServerId() == IRoomInfo.NOT_ASSIGNED_ID)
                && (moneyType == MoneyType.TOURNAMENT || room.getSeatsCount(roomPlayerInfoService) == 0);
    }

    @Override
    public SingleNodeRoomInfo createForTemplate(RoomTemplate template, long bankId, Money stake, String currency) {
        if (stake.toCents() <= 0) {
            throw new IllegalArgumentException("Illegal stake value: " + stake.toCents());
        }
        long roundId = idGenerator.getNext(RoundInfo.class);
        SingleNodeRoomInfo singleNodeRoomInfo = new SingleNodeRoomInfo(
                generateRoomId(),
                template,
                bankId,
                System.currentTimeMillis(),
                IRoomInfo.NOT_ASSIGNED_ID,
                roundId,
                RoomState.WAIT,
                RNG.nextInt(1, 7),
                stake,
                currency);
        singleNodeRoomInfo.updateLastTimeActivity();
        return add(singleNodeRoomInfo);
    }

    @Override
    public synchronized void createForTemplate(RoomTemplate template, Collection<SingleNodeRoomInfo> rooms, long bankId,
                                               Money stake, String currency) {
        LOG.debug("createForTemplate: rooms={}, template={}", rooms.size(), template);
        if (stake.toCents() <= 0) {
            throw new IllegalArgumentException("Illegal stake value: " + stake.toCents());
        }
        RoomsStat roomsStat = new RoomsStat();
        for (IRoomInfo room : rooms) {
            roomsStat.allRooms++;
            short seatsCount = room.getSeatsCount(roomPlayerInfoService);
            if (seatsCount >= room.getMaxSeats()) {
                roomsStat.fullRooms++;
            } else if (seatsCount == 0) {
                roomsStat.emptyRooms++;
            }
        }

        int needCreateRoomsCount = 0;

        if (rooms.isEmpty()) {
            needCreateRoomsCount = template.getInitialRooms();
            LOG.debug("createForTemplate: rooms not found, create initial rooms for template: {}", template);
        } else {
            if (roomsStat.emptyRooms < template.getMinFreeRooms()) {
                needCreateRoomsCount = template.getMinFreeRooms() - roomsStat.emptyRooms;
            } else if (roomsStat.allRooms <= roomsStat.fullRooms) { //may be if template.getMinFreeRooms()==0
                needCreateRoomsCount = 1;
            }
            //todo: hmmm, may be implement logic for limit rooms by template.getMaxRooms ?
        }

        for (int i = 0; i < needCreateRoomsCount; i++) {

            long roundId = idGenerator.getNext(RoundInfo.class);
            long roomId = this.generateRoomId();
            int mapId = RNG.nextInt(1, 7);

            SingleNodeRoomInfo singleNodeRoomInfo = new SingleNodeRoomInfo(
                    roomId,
                    template,
                    bankId,
                    System.currentTimeMillis(),
                    IRoomInfo.NOT_ASSIGNED_ID,
                    roundId,
                    RoomState.WAIT,
                    mapId,
                    stake,
                    currency);
            singleNodeRoomInfo.updateLastTimeActivity();
            this.add(singleNodeRoomInfo);
        }

        LOG.info("createForTemplate: completed, created rooms: {}", needCreateRoomsCount);
    }

    public int getServerId() {
        return serverId;
    }

    @Override
    public SingleNodeRoomInfo tryFindThisServerRoomAndNotFull(Collection<SingleNodeRoomInfo> roomInfos, int serverId) {

        SingleNodeRoomInfo bestRoom = null;
        int bestRoomSeatsCount = 0;
        SingleNodeRoomInfo anyRoom = null;

        for (SingleNodeRoomInfo roomInfo : roomInfos) {

            if (anyRoom == null) {
                anyRoom = roomInfo;
            }

            short seatsCount = roomInfo.getSeatsCount(roomPlayerInfoService);

            LOG.debug("tryFindThisServerRoomAndNotFull: seatsCount={}, room.id={}, serverId={},roomInfo.getGameServerId(): {}",
                    seatsCount, roomInfo.getId(), serverId, roomInfo.getGameServerId());

            if (roomInfo.getMaxSeats() == 1) { //special case, single player rooms

                if (seatsCount == 0 && (roomInfo.getGameServerId() == serverId || roomInfo.getGameServerId() == -1)) {

                    bestRoom = roomInfo;
                    break;

                } else {

                    continue;
                }
            }

            if (seatsCount >= roomInfo.getMaxSeats()) {
                continue;
            }

            if (roomInfo.getGameServerId() == serverId && seatsCount > 0) {

                bestRoom = roomInfo;
                break;

            } else if (bestRoom == null || (roomInfo.getGameServerId() == serverId && bestRoom.getGameServerId() != serverId) ||
                    (bestRoomSeatsCount == 0 && seatsCount > 0)) {

                bestRoom = roomInfo;
                bestRoomSeatsCount = seatsCount;
            }

        }

        if (bestRoom == null && anyRoom != null) {
            RoomTemplate roomTemplate = getTemplate(anyRoom.getTemplateId());
            bestRoom = createForTemplate(roomTemplate, anyRoom.getBankId(), anyRoom.getStake(), anyRoom.getCurrency());
            LOG.debug("tryFindThisServerRoomAndNotFull: bestRoom not found, create new, bestRoom.id={}", bestRoom.getId());
        }

        return bestRoom;
    }
}
