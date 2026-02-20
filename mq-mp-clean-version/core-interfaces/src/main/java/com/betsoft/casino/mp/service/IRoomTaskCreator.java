package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.ICrashRoundInfo;
import com.betsoft.casino.mp.model.ISeat;
import com.betsoft.casino.utils.ITransportObject;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * User: flsh
 * Date: 24.08.18.
 */
public interface IRoomTaskCreator {
    Runnable createNewSeatNotifyTask(long roomId, long serverId, ISeat seat);

    Runnable createSeatRemovedNotifyTask(long roomId);

    Runnable createSendSeatOwnerMessageTask(long roomId, GameType gameType, long senderServerId, ITransportObject message);

    Runnable createSendSeatsMessageTask(long roomId, GameType gameType, long senderServerId, Long relatedAccountId,
                                        boolean notSendToRelatedAccountId, long relatedRequestId, ITransportObject message,
                                        boolean sendToAllObservers);

    Runnable createUpdateCrashHistoryTask(long roomId, GameType gameType, long senderServerId, ICrashRoundInfo crashRoundInfo);

    Runnable createSendSeatMessageTask(long roomId, GameType gameType, long senderServerId, Long accountId, ITransportObject message);

    Runnable createSendAllObserversNoSeatMessageTask(long roomId, GameType gameType, long senderServerId, ITransportObject message);

    Callable<Integer> createObserversCollectTask(long roomId, int gameId);

    Callable<Collection> createObserverClientListCollectionTask(long roomId, int gameId);

    void executeOnAllMembers(Runnable task);
}
