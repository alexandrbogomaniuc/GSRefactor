package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.abs.casino.common.exception.CommonException;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.MapListener;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IRoomInfoService<RI extends IRoomInfo, RT extends IRoomTemplate> extends IRoomTaskCreator, IGameRoomStartListener {

    interface IRoomInfoChangeHandler<ROOM_INFO extends IRoomInfo> {
        long getRoomId();
        void processKickAndCancelKick(ROOM_INFO roomInfo);
    }

    class RoomInfoListener implements EntryListener<Long, IRoomInfo> {

        protected final IRoomInfoChangeHandler roomInfoChangeHandler;
        public RoomInfoListener(IRoomInfoChangeHandler roomInfoChangeHandler) {
            this.roomInfoChangeHandler = roomInfoChangeHandler;
        }
        @Override
        public void entryAdded(EntryEvent event) {
            if(roomInfoChangeHandler != null
                    && (long)event.getKey() == roomInfoChangeHandler.getRoomId()) {
                roomInfoChangeHandler.processKickAndCancelKick((IRoomInfo)event.getValue());
            }
        }

        @Override
        public void entryEvicted(EntryEvent event) {
        }

        @Override
        public void entryRemoved(EntryEvent event) {
        }

        @Override
        public void entryUpdated(EntryEvent event) {
            if(roomInfoChangeHandler != null
                    && (long)event.getKey() == roomInfoChangeHandler.getRoomId()) {
                roomInfoChangeHandler.processKickAndCancelKick((IRoomInfo)event.getValue());
            }
        }

        @Override
        public void mapCleared(MapEvent event) {

        }

        @Override
        public void mapEvicted(MapEvent event) {

        }
    }

    String registerListener(MapListener listener);

    boolean unregisterListener(String listenerId);

    boolean isInitialized();

    void lock(Long id);

    void unlock(Long id);

    void forceUnlock(Long id);

    boolean isLocked(Long id);

    boolean tryLock(Long id);

    boolean tryLock(Long id, long time, TimeUnit timeunit) throws InterruptedException;

    Collection<RI> getAllRooms();

    Collection<RI> getServerRooms(int serverId);

    Collection<RI> getThisServerRooms();

    Collection<RI> getRooms(long bankId, RT template, Money stake, String currency);

    Collection<RI> getRooms(long bankId, GameType gameType, MoneyType moneyType, boolean closed,
                            IRoomTemplateService roomTemplateService, List<Long> stakes, String currency);

    void remove(long roomId) throws CommonException;

    RI createForTemplate(RT template, long bankId, Money stake, String currency);

    void createForTemplate(RT template, Collection<RI> rooms, long bankId,
                           Money stake, String currency);

    RT getTemplate(long templateId);

    void checkAndCreateForTemplate(long bankId, RT template, Money stake, String currency);

    RI getRoom(long id);

    void update(RI roomInfo);

    void seatAdded(long roomId);

    void seatRemoved(long roomId);

    RI getBestRoomInfo(Long bankId, int serverId, MoneyType moneyType, GameType gameType, Money stake, String currency);

    RI tryFindThisServerRoomAndNotFull(Collection<RI> roomInfos, int serverId);

    Collection<RI> getBattlegroundRooms(long bankId, GameType gameType, Money stake, String currency);

    RI getBestRoomForBattleground(Collection<RI> roomInfos);

    Collection<RI> getSpecialRooms(long bankId, GameType gameType, Money stake, String currency, MoneyType moneyType);

    Collection<RI> getActiveBattlegroundRooms(long bankId, GameType gameType);

    IRoomPlayerInfoService getRoomPlayerInfoService();
}
