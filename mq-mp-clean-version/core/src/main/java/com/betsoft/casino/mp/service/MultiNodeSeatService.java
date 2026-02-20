package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IMultiNodeSeat;
import com.betsoft.casino.mp.model.ISeat;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * User: flsh
 * Date: 20.01.2022.
 */
@SuppressWarnings("rawtypes")
@Service
public class MultiNodeSeatService implements IMultiNodeSeatService {
    public static final String SEAT_STORE = "multiNodeSeatStore";
    private static final Logger LOG = LogManager.getLogger(MultiNodeSeatService.class);
    private final HazelcastInstance hazelcast;
    //key is roomId+accountId
    private IMap<String, IMultiNodeSeat> seats;

    public MultiNodeSeatService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    @PostConstruct
    private void init() {
        seats = hazelcast.getMap(SEAT_STORE);
        seats.addIndex("accountId", false);
        seats.addIndex("roomId", false);
        getLogger().info("init: completed");
    }

    @Override
    public Collection<IMultiNodeSeat> getAllRoomSeats() {
        return seats.values();
    }

    public Collection<IMultiNodeSeat> getRoomSeats(long roomId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("roomId").equal(roomId);
        return seats.values(predicate);
    }

    @Override
    public <S extends IMultiNodeSeat> List<S> getRoomSeats(long roomId, Class<S> requiredType) {
        Collection<IMultiNodeSeat> values = getRoomSeats(roomId);
        List<S> result = new ArrayList<>(values.size());
        getLogger().debug("getRoomSeats: values.size()={}", values.size());
        for (IMultiNodeSeat seat : values) {
            if (requiredType.isInstance(seat)) {
                //noinspection unchecked
                result.add((S) seat);
            } else {
                getLogger().warn("getRoomSeats: bad seat type, seat={}, found={} must be={}", seat, seat.getClass(), requiredType);
            }
        }
        result.sort(Comparator.comparingLong(ISeat::getAccountId));
        return result;
    }

    @Override
    public IMultiNodeSeat getSeat(long roomId, long accountId) {
        return seats.get(getKey(roomId, accountId));
    }

    protected IMultiNodeSeat getSeat(String key) {
        return seats.get(key);
    }

    @Override
    public void put(IMultiNodeSeat seat) {
        String key = getKey(seat);
        IMultiNodeSeat actual = getSeat(key);
        if (actual == null) {
            if (seat.getActualVersion() > 0) {
                getLogger().warn("Seat already removed: {}", seat);
                throw new IllegalStateException("Seat already removed");
            } else {
                put(key, seat);
            }
        } else {
            if (actual.getActualVersion() > seat.getActualVersion()) {
                getLogger().warn("Seat already updated: actual={}, old for save={}", actual, seat);
                throw new IllegalStateException("Seat already updated");
            } else {
                put(key, seat);
            }
        }
    }

    protected void put(String key, IMultiNodeSeat seat) {
        getLogger().debug("put: {}, seat={}", key, seat);
        seat.setLastActivityDate(System.currentTimeMillis());
        seat.incrementActualVersion();
        seats.set(key, seat);
    }

    @Override
    public void removeAll(long roomId) {

        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("roomId").equal(roomId);
        Collection<IMultiNodeSeat> values = seats.values(predicate);
        List<Long> accountIds = new ArrayList<>();

        getLogger().debug("removeAll: roomId={}, values.size()={}", roomId, values.size());
        for (IMultiNodeSeat seat : values) {
            accountIds.add(seat.getAccountId());
        }

        getLogger().debug("removeAll: roomId={}, accountIds={}", roomId, accountIds);

        for (Long accountId : accountIds) {
            String key = getKey(roomId, accountId);
            getLogger().debug("removeAll: {}", key);
            seats.delete(key);
        }
    }

    @Override
    public void remove(long roomId, long accountId) {
        String key = getKey(roomId, accountId);
        getLogger().debug("remove: {}", key);
        seats.delete(key);
    }

    @Override
    public void remove(IMultiNodeSeat seat) {
        String key = getKey(seat);
        getLogger().debug("remove: {}", key);
        seats.delete(key);
    }

    @Override
    public int seatsCount(long roomId) {

        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("roomId").equal(roomId);
        Collection<IMultiNodeSeat> values = seats.values(predicate);
        getLogger().debug("seatsCount: roomId={}, values.size()={}", roomId, values.size());
        return values.size();
    }

    private Logger getLogger() {
        return LOG;
    }
}
