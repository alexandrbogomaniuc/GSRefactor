package com.betsoft.casino.mp.payment;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.service.ISocketService;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.ObjectAlreadyExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 24.08.2022.
 */
public class SitOutOperationProcessor implements IPendingOperationProcessor<SitOutPendingOperation> {
    private static final Logger LOG = LogManager.getLogger(SitOutOperationProcessor.class);

    @Override
    public boolean process(ISocketService socketService, int serverId, SitOutPendingOperation operation) throws CommonException {
        LOG.debug("process: operation={}", operation);
        if (operation.getInnerOperation() instanceof BuyInPendingOperation) {
            BuyInPendingOperation innerOperation = (BuyInPendingOperation) operation.getInnerOperation();
            boolean refundResult = socketService.refundBuyIn(serverId, innerOperation.getSessionId(), innerOperation.getAmount(),
                    innerOperation.getAccountId(), innerOperation.getGameSessionId(), innerOperation.getRoomId(),
                    innerOperation.getBetNumber());
            if (!refundResult) {
                return false;
            }
            operation.setInnerOperation(null);
        } else if(operation.getInnerOperation() != null) {
            LOG.error("process: unsupported innerOperation={}", operation.getInnerOperation());
        }
        return socketService.closeGameSession(serverId, operation.getSessionId(), operation.getAccountId(), operation.getGameSessionId(),
                operation.getRoomId(), operation.getGameId(), operation.getBankId(), getBuyIn(operation));
    }

    private long getBuyIn(SitOutPendingOperation operation) {
        GameType gameType = GameType.getByGameId((int) operation.getGameId());
        if (gameType != null && !gameType.isBattleGroundGame()) {
            return 0L;
        }
        if (operation.getInnerOperation() instanceof BuyInPendingOperation) {
            BuyInPendingOperation innerOperation = (BuyInPendingOperation) operation.getInnerOperation();
            return innerOperation.getAmount();
        }
        return 0L;
    }

    @Override
    public boolean mergeNewAndExistingOperation(SitOutPendingOperation newOperation, IPendingOperation oldOperation)
            throws ObjectAlreadyExistsException {
        LOG.debug("mergeNewAndExistingOperation: merge not supported, newOperation={}, oldOperation={}", newOperation, oldOperation);
        if (oldOperation instanceof SitOutPendingOperation) {
            //nop, duplicate
        } else if (oldOperation instanceof AddWinPendingOperation) {
            //store old, addWin always sitOut
            return false;
        } else if (oldOperation instanceof BuyInPendingOperation) {
            newOperation.setInnerOperation(oldOperation);
            return true;
        }
        return false;
    }
}
