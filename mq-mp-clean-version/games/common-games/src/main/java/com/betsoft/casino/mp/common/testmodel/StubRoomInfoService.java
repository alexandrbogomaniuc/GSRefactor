package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.IRoomInfoService;
import com.betsoft.casino.mp.service.IRoomPlayerInfoService;
import com.betsoft.casino.utils.ITransportObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.hazelcast.map.listener.MapListener;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 26.05.2020.
 */
public class StubRoomInfoService implements IRoomInfoService {


    @Override
    public String registerListener(MapListener listener) {
        return null;
    }

    @Override
    public boolean unregisterListener(String listenerId) {
        return false;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void lock(Long id) {

    }

    @Override
    public void unlock(Long id) {

    }

    @Override
    public void forceUnlock(Long id) {

    }

    @Override
    public boolean isLocked(Long id) {
        return false;
    }

    @Override
    public boolean tryLock(Long id) {
        return false;
    }

    @Override
    public boolean tryLock(Long id, long time, TimeUnit timeunit) throws InterruptedException {
        return false;
    }

    @Override
    public Collection getAllRooms() {
        return null;
    }

    @Override
    public Collection getServerRooms(int serverId) {
        return null;
    }

    @Override
    public Collection getThisServerRooms() {
        return null;
    }

    @Override
    public Collection getRooms(long bankId, IRoomTemplate template, Money stake, String currency) {
        return null;
    }

    @Override
    public void remove(long roomId) throws CommonException {

    }

    @Override
    public IRoomInfo createForTemplate(IRoomTemplate template, long bankId, Money stake, String currency) {
        return null;
    }

    public ISingleNodeRoomInfo createForTemplate(IRoomTemplate template, long bankId, Money stake, String currency,
                                                 int roundDuration, GameType gameType, String name
    ) {
        return new StubRoomInfo(1, 1, 271, name, gameType,
                false, System.currentTimeMillis(), 1, 1L, (short) 6, (short) 1,
                MoneyType.REAL, RoomState.WAIT,
                1000, stake, 1, currency, roundDuration);
    }


    @Override
    public void createForTemplate(IRoomTemplate template, Collection rooms, long bankId, Money stake, String currency) {

    }

    @Override
    public IRoomTemplate getTemplate(long templateId) {
        return null;
    }

    @Override
    public void checkAndCreateForTemplate(long bankId, IRoomTemplate template, Money stake, String currency) {

    }

    @Override
    public IRoomInfo getRoom(long id) {
        return null;
    }

    @Override
    public void update(IRoomInfo roomInfo) {

    }

    @Override
    public void seatAdded(long roomId) {

    }

    @Override
    public void seatRemoved(long roomId) {

    }

    @Override
    public IRoomInfo getBestRoomInfo(Long bankId, int serverId, MoneyType moneyType, GameType gameType, Money stake, String currency) {
        return null;
    }

    @Override
    public IRoomInfo tryFindThisServerRoomAndNotFull(Collection roomInfos, int serverId) {
        return null;
    }

    @Override
    public Collection getBattlegroundRooms(long bankId, GameType gameType, Money stake, String currency) {
        return null;
    }

    @Override
    public IRoomInfo getBestRoomForBattleground(Collection roomInfos) {
        return null;
    }

    @Override
    public Collection getSpecialRooms(long bankId, GameType gameType, Money stake, String currency, MoneyType moneyType) {
        return null;
    }

    @Override
    public Collection getActiveBattlegroundRooms(long bankId, GameType gameType) {
        return null;
    }

    @Override
    public Collection getRooms(long bankId, GameType gameType, MoneyType moneyType, boolean closed, IRoomTemplateService roomTemplateService,
                               List stakes, String currency) {
        return null;
    }

    @Override
    public Runnable createNewSeatNotifyTask(long roomId, long serverId, ISeat seat) {
        return null;
    }

    @Override
    public Runnable createSeatRemovedNotifyTask(long roomId) {
        return null;
    }

    @Override
    public Runnable createSendSeatOwnerMessageTask(long roomId, GameType gameType, long senderServerId, ITransportObject message) {
        return null;
    }

    @Override
    public Runnable createSendSeatsMessageTask(long roomId, GameType gameType, long senderServerId, Long relatedAccountId,
                                               boolean notSendToRelatedAccountId, long relatedRequestId, ITransportObject message,
                                               boolean sendToAllObservers) {
        return null;
    }

    @Override
    public Runnable createUpdateCrashHistoryTask(long roomId, GameType gameType, long senderServerId, ICrashRoundInfo crashRoundInfo) {
        return null;
    }

    @Override
    public Runnable createSendSeatMessageTask(long roomId, GameType gameType, long senderServerId, Long accountId, ITransportObject message) {
        return null;
    }

    @Override
    public Runnable createSendAllObserversNoSeatMessageTask(long roomId, GameType gameType, long senderServerId, ITransportObject message) {
        return null;
    }

    @Override
    public Callable<Integer> createObserversCollectTask(long roomId, int gameId) {
        return null;
    }

    @Override
    public Callable<Collection> createObserverClientListCollectionTask(long roomId, int gameId) {
        return null;
    }

    @Override
    public void executeOnAllMembers(Runnable task){

    }

    @Override
    public void notifyRoomStarted(IRoom room) {
    }

    @Override
    public IRoomPlayerInfoService getRoomPlayerInfoService() {
        return null;
    }
}
