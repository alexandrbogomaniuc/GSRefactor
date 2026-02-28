package com.betsoft.casino.mp.payment;

import com.betsoft.casino.mp.service.ISocketService;
import com.abs.casino.common.exception.ObjectAlreadyExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 12.08.2022.
 */
public class BuyInOperationProcessor implements IPendingOperationProcessor<BuyInPendingOperation> {
    private static final Logger LOG = LogManager.getLogger(BuyInOperationProcessor.class);

    @Override
    public boolean process(ISocketService socketService, int serverId, BuyInPendingOperation operation) {
        LOG.debug("process: operation={}", operation);
        return socketService.refundBuyIn(serverId, operation.getSessionId(), operation.getAmount(), operation.getAccountId(),
                operation.getGameSessionId(), operation.getRoomId(), operation.getBetNumber());
    }

    @Override
    public boolean mergeNewAndExistingOperation(BuyInPendingOperation newOperation, IPendingOperation oldOperation) throws ObjectAlreadyExistsException {
        LOG.debug("mergeNewAndExistingOperation: merge not supported, newOperation={}, oldOperation={}", newOperation, oldOperation);
        throwMergeException();
        return false;
    }
}
