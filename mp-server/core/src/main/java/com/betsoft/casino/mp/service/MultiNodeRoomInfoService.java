package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.common.SharedCrashGameState;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IMultiNodeRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.RNG;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: flsh
 * Date: 09.11.17.
 */
@Service
public class MultiNodeRoomInfoService extends AbstractRoomInfoService<MultiNodeRoomInfo, RoomTemplate> {
    private static final Logger LOG = LogManager.getLogger(MultiNodeRoomInfoService.class);
    public static final String ROOM_INFO_STORE = "mnRoomInfoStore";
    protected final SharedGameStateService sharedGameStateService;

    //only for tests
    public MultiNodeRoomInfoService() {
        super();
        sharedGameStateService = null;
    }

    public MultiNodeRoomInfoService(HazelcastInstance hazelcast, int serverId, RoomTemplateService roomTemplateService,
                                    RoomPlayerInfoService roomPlayerInfoService, IdGenerator idGenerator, SharedGameStateService sharedGameStateService) {
        super(hazelcast, roomTemplateService, roomPlayerInfoService, idGenerator);
        this.sharedGameStateService = sharedGameStateService;
    }

    @PostConstruct
    protected void init() {
        super.init();
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
    public Collection<MultiNodeRoomInfo> getServerRooms(int serverId) {
        return rooms.values();
    }

    @Override
    public Collection<MultiNodeRoomInfo> getThisServerRooms() {
        return rooms.values();
    }

    @Override
    public MultiNodeRoomInfo createForTemplate(RoomTemplate template, long bankId, Money stake, String currency) {
        if (stake.toCents() <= 0) {
            throw new IllegalArgumentException("Illegal stake value: " + stake.toCents());
        }
        long roundId = idGenerator.getNext(RoundInfo.class);
        MultiNodeRoomInfo multiNodeRoomInfo = new MultiNodeRoomInfo(
                generateRoomId(),
                template,
                bankId,
                System.currentTimeMillis(),
                roundId,
                RoomState.WAIT,
                RNG.nextInt(1, 7),
                stake,
                currency);
        multiNodeRoomInfo.updateLastTimeActivity();
        return add(multiNodeRoomInfo);
    }

    @Override
    public synchronized void createForTemplate(RoomTemplate template, Collection<MultiNodeRoomInfo> rooms, long bankId,
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
        //temp fix for prevent modify already created RoomTemplate
        if (needCreateRoomsCount > 1) {
            needCreateRoomsCount = 1;
        }
        for (int i = 0; i < needCreateRoomsCount; i++) {
            long roundId = idGenerator.getNext(RoundInfo.class);
            MultiNodeRoomInfo multiNodeRoomInfo = new MultiNodeRoomInfo(
                    generateRoomId(),
                    template,
                    bankId,
                    System.currentTimeMillis(),
                    roundId,
                    RoomState.WAIT,
                    RNG.nextInt(1, 7),
                    stake,
                    currency);
            multiNodeRoomInfo.updateLastTimeActivity();
            add(multiNodeRoomInfo);
        }
        LOG.info("createForTemplate: completed, created rooms: {}", needCreateRoomsCount);
    }


    @Override
    public MultiNodeRoomInfo tryFindThisServerRoomAndNotFull(Collection<MultiNodeRoomInfo> roomInfos, int serverId) {
        MultiNodeRoomInfo bestRoom = null;
        MultiNodeRoomInfo anyRoom = null;
        for (MultiNodeRoomInfo roomInfo : roomInfos) {
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
    public MultiNodeRoomInfo tryFindThisServerRoomAndNotFull(Collection<MultiNodeRoomInfo> roomInfos, IRoomServiceFactory roomServiceFactory) {

        List<MultiNodeRoomInfo> roomInfosOrdered = new ArrayList<>();

        if(roomInfos != null) {
            roomInfosOrdered = roomInfos.stream()
                    .sorted(Comparator.comparing(MultiNodeRoomInfo::getId))
                    .collect(Collectors.toList());
        }

        MultiNodeRoomInfo bestRoom = null;
        MultiNodeRoomInfo anyRoom = null;
        int badRoomsCount = 0;
        int fullRoomsCount = 0;
        int totalRoomsCount = 0;
        for (MultiNodeRoomInfo roomInfo : roomInfosOrdered) {
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
}
