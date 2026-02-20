package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 10.11.17.
 */
@SuppressWarnings("rawtypes")
@Service
public class RoomPlayerInfoService implements IRoomPlayerInfoService {
    private static final Logger LOG = LogManager.getLogger(RoomPlayerInfoService.class);
    public static final String ROOM_PLAYER_INFO_STORE = "roomPlayerInfoStore";
    private final HazelcastInstance hazelcast;
    //key is accountId
    private IMap<Long, IRoomPlayerInfo> players;
    private IExecutorService notifyService;

    public RoomPlayerInfoService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    @PostConstruct
    private void init() {
        players = hazelcast.getMap(ROOM_PLAYER_INFO_STORE);
        players.addIndex("id", false);
        players.addIndex("bankId", false);
        players.addIndex("roomId", false);
        players.addIndex("seatNumber", false);
        players.addIndex("sessionId", false);
        players.addIndex("gameSessionId", false);
        this.notifyService = hazelcast.getExecutorService("mqRemoteExecutor");
        notifyService = hazelcast.getExecutorService("default");
        LOG.info("init: completed");
    }

    @Override
    public IRoomPlayerInfo get(long accountId) {
        return players.get(accountId);
    }

    public Collection<IRoomPlayerInfo> getByNickname(String nickname) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("nickname").equal(nickname);
        return players.values(predicate);
    }

    public Collection<IRoomPlayerInfo> getBySessionId(String sid) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("sessionId").equal(sid);
        return players.values(predicate);
    }

    public Collection<IRoomPlayerInfo> getByGameSessionId(long gameSessionId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("gameSessionId").equal(gameSessionId);
        return players.values(predicate);
    }

    @Override
    public boolean hasPlayersWithPendingOperation(long roomId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("roomId").equal(roomId);
        Collection<IRoomPlayerInfo> playerInfos = players.values(predicate);
        for (IRoomPlayerInfo playerInfo : playerInfos) {
            if (playerInfo.isPendingOperation()) {
                LOG.debug("Found playerInfo with pending operation, roomId={}, player={}", roomId, playerInfo);
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<IRoomPlayerInfo> getForRoom(long roomId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("roomId").equal(roomId);
        Collection<IRoomPlayerInfo> playerInfos = players.values(predicate);
        //todo: remove possible duplicated check after debug and just return playerInfos
        HashMap<Long, IRoomPlayerInfo> result = new HashMap<>(playerInfos.size());
        for (IRoomPlayerInfo playerInfo : playerInfos) {
            IRoomPlayerInfo duplicatePlayer = result.get(playerInfo.getId());
            if (duplicatePlayer != null) {
                LOG.debug("getForRoom: found duplicate player, one={}, two={}", duplicatePlayer, playerInfo);
            } else {
                IRoomPlayerInfo roomPlayerInfo = players.get(playerInfo.getId());
                if (roomPlayerInfo != null && roomPlayerInfo.getRoomId() == roomId) {
                    result.put(playerInfo.getId(), playerInfo);
                } else {
                    //nop, may be null if rat race
                }
            }
        }
        return result.values();
    }

    @Override
    public void lock(long accountId) {
        LOG.debug("HC lock: {}", accountId);
        players.lock(accountId);
    }

    @Override
    public boolean tryLock(long accountId, long time, TimeUnit timeunit) throws InterruptedException {
        LOG.debug("HC tryLock: {}", accountId);
        return players.tryLock(accountId, time, timeunit);
    }

    @Override
    public void unlock(long accountId) {
        LOG.debug("HC unlock: {}", accountId);
        players.unlock(accountId);
    }

    @Override
    public boolean isLocked(long accountId) {
        return players.isLocked(accountId);
    }

    @Override
    public void forceUnlock(long accountId) {
        LOG.debug("HC forceUnlock: {}", accountId);
        players.forceUnlock(accountId);
    }

    @Override
    public void put(IRoomPlayerInfo playerInfo) {
        LOG.debug("put: playerInfo={}", playerInfo.toShortString());
        IRoomPlayerInfo existPlayer = players.put(playerInfo.getId(), playerInfo);
        if (existPlayer != null && !existPlayer.getClass().equals(playerInfo.getClass())) {
            //this temporary code for catch strange bag with overwrite playerInfo with wrong class
            LOG.error("put: found bad RoomPlayerInfo class: newPlayer={}, exist={}", playerInfo, existPlayer);
            // rollback changes and throw error
            players.set(playerInfo.getId(), existPlayer);
            throw new IllegalStateException("Bad playerInfo.class, must be=" + existPlayer.getClass());
        }
    }

    @Override
    public void remove(IRoomTaskCreator roomTaskCreator, long roomId, long accountId) {
        LOG.debug("remove: accountId={}, roomId={}", accountId, roomId);
        players.delete(accountId);
        if(roomTaskCreator == null) {
            LOG.debug("remove: skip add SeatRemovedNotifyTask, roomTaskCreator is null roomId={}", roomId);
            return;
        }

        if (notifyService != null && !notifyService.isShutdown()) {
            try {
                notifyService.executeOnAllMembers(roomTaskCreator.createSeatRemovedNotifyTask(roomId));
            } catch (RejectedExecutionException e) {
                LOG.warn("remove: cannot execute SeatRemovedNotifyTask, may shutdown started, roomId={}", roomId);
            }
        } else {
            LOG.warn("remove: skip add SeatRemovedNotifyTask, executor not started, roomId={}", roomId);
        }
    }

    public IExecutorService getNotifyService() {
        return notifyService;
    }
}
