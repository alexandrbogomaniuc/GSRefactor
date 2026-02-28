package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IPlayerBet;
import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.payment.*;
import com.abs.casino.common.exception.ObjectAlreadyExistsException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 11.08.2022.
 */
@Service
public class PendingOperationService implements IPendingOperationService {
    private static final Logger LOG = LogManager.getLogger(PendingOperationService.class);
    public static final String OPERATION_STORE = "pendingOperationStore";
    private final HazelcastInstance hazelcast;
    private IMap<Long, IPendingOperation> operations;
    private final Map<PendingOperationType, IPendingOperationProcessor<?>> processors = new EnumMap<>(PendingOperationType.class);

    public PendingOperationService(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }

    @PostConstruct
    private void init() {
        LOG.info("init: start");
        operations = hazelcast.getMap(OPERATION_STORE);
        processors.put(PendingOperationType.BUY_IN, new BuyInOperationProcessor());
        processors.put(PendingOperationType.ADD_WIN, new AddWinOperationProcessor());
        processors.put(PendingOperationType.SIT_OUT, new SitOutOperationProcessor());
        LOG.info("init: completed");
    }

    @PreDestroy
    public void shutdown() {
        LOG.info("shutdown");
    }

    @Override
    public boolean isPendingOperation(IRoomPlayerInfo playerInfo) {
        return playerInfo.isPendingOperation() || isExist(playerInfo.getId());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public IPendingOperationProcessor getProcessor(PendingOperationType type) {
        return processors.get(type);
    }


    @Override
    public IPendingOperation get(long accountId) {
        return operations.get(accountId);
    }

    @Override
    public void save(IPendingOperation operation) {
        LOG.debug("save: {}", operation);
        operations.set(operation.getAccountId(), operation);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void create(IPendingOperation newOperation) {
        LOG.debug("create: {}", newOperation);
        long accountId = newOperation.getAccountId();
        lock(accountId);
        try {
            IPendingOperation oldOperation = operations.get(accountId);
            if (oldOperation == null) {
                operations.set(accountId, newOperation);
            } else if (!isSameOperation(oldOperation, newOperation)) {
                IPendingOperationProcessor processor = getProcessor(newOperation.getOperationType());
                boolean needSave = false;
                try {
                    //noinspection unchecked
                    needSave = processor.mergeNewAndExistingOperation(newOperation, oldOperation);
                } catch (ObjectAlreadyExistsException e) {
                    LOG.error("Cannot merge new and old operation", e);
                }
                if (needSave) {
                    operations.set(accountId, newOperation);
                }
            }
        } finally {
            unlock(accountId);
        }
    }

    public IPendingOperation createBuyInPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId, long amount, int betNumber,
                                                         Long tournamentId, Long currentBalance, long gameId, long bankId) {
        return new BuyInPendingOperation(accountId, sessionId, gameSessionId, roomId, amount, betNumber, tournamentId, currentBalance, gameId, bankId);
    }

    public AddWinPendingOperation createWinPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId,
                                                            long gameId, long bankId, long winAmount, long returnedBet, long gsRound, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo) {
        return new AddWinPendingOperation(accountId, sessionId, gameSessionId,
                roomId, winAmount, returnedBet, gsRound, playerBet, bgRoundInfo, gameId, bankId);
    }

    private boolean isSameOperation(IPendingOperation oldOp, IPendingOperation newOp) {
        boolean same = oldOp.getOperationType().equals(newOp.getOperationType()) && oldOp.getRoomId() == newOp.getRoomId();
        if (same && oldOp instanceof AddWinPendingOperation) {
            AddWinPendingOperation oldAdd = (AddWinPendingOperation) oldOp;
            AddWinPendingOperation newAdd = (AddWinPendingOperation) newOp;
            same = oldAdd.getRoundId() == newAdd.getRoundId();
        }
        return same;
    }

    @Override
    public void remove(long accountId) {
        LOG.debug("remove: accountId={}", accountId);
        IPendingOperation pendingOperation = operations.remove(accountId);
        LOG.debug("remove: success removed={}", pendingOperation);
    }

    @Override
    public boolean isExist(long accountId) {
        return operations.containsKey(accountId);
    }

    @Override
    public Collection<IPendingOperation> getAll() {
        return operations.values();
    }

    @Override
    public Collection<Long> getAllKeys() {
        return operations.keySet();
    }

    @Override
    public boolean tryLock(long accountId) {
        return operations.tryLock(accountId);
    }

    @Override
    public void unlock(long accountId) {
        operations.unlock(accountId);
    }

    @Override
    public void lock(long accountId) {
        operations.lock(accountId);
    }
}
