package com.betsoft.casino.mp.payment;

import com.betsoft.casino.mp.model.IAddWinResult;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.PaymentTransactionStatus;
import com.betsoft.casino.mp.service.ISocketService;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.ObjectAlreadyExistsException;
import com.dgphoenix.casino.common.mp.TransactionErrorCodes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 12.08.2022.
 */
public class AddWinOperationProcessor implements IPendingOperationProcessor<AddWinPendingOperation> {
    private static final Logger LOG = LogManager.getLogger(AddWinOperationProcessor.class);
    private static final long PENDING_STATUS_TIMEOUT = 120000;

    @Override
    public boolean process(ISocketService socketService, int serverId, AddWinPendingOperation operation) throws CommonException {
        LOG.debug("process: operation={}", operation);
        BuyInPendingOperation innerOperation = operation.getInnerOperation();
        if (innerOperation != null) {
            boolean refundResult = socketService.refundBuyIn(serverId, innerOperation.getSessionId(), innerOperation.getAmount(),
                    innerOperation.getAccountId(), innerOperation.getGameSessionId(), innerOperation.getRoomId(),
                    innerOperation.getBetNumber());
            if (!refundResult) {
                return false;
            }
            operation.setInnerOperation(null);
        }
        PaymentTransactionStatus winStatusForPlayer = socketService.getPaymentOperationStatus(operation.getAccountId(), operation.getRoomId(), operation.getRoundId(),
                operation.getSessionId(), operation.getGameSessionId(), operation.getGameId(), operation.getBankId(), Boolean.FALSE, -1);
        if (winStatusForPlayer != null) {
            LOG.debug("process: found winStatusForPlayer={}, not required resend win, completed processing", winStatusForPlayer);
            return true;
        } else if (!isPendingStatusTimeout(operation)) {
            LOG.debug("process: not found winStatusForPlayer=, but timeout not reached, just wait");
            return false;
        }
        LOG.debug("process: not found winStatusForPlayer, and reached timeout, need resend addWin");
        IAddWinResult result = socketService.addWinWithSitOutSync(serverId, operation.getSessionId(), operation.getGameSessionId(),
                Money.fromCents(operation.getWinAmount()), Money.fromCents(operation.getReturnedBet()),
                operation.getRoundId(), operation.getRoomId(), operation.getAccountId(), operation.getPlayerBet(),
                operation.getBgRoundInfo(), true);
        boolean thisOperationIsPending = result.getErrorCode() == TransactionErrorCodes.FOUND_PENDING_TRANSACTION &&
                getPendingOperationId(result.getErrorDetails()).equals(operation.getOperationId());
        if (result.isSuccess() || thisOperationIsPending) {
            return true;
        } else {
            LOG.error("process failed result={}", result);
            return false;
        }
    }

    private static String getPendingOperationId(String errorDetails) {
        String startString = "operationId=";
        if (errorDetails.contains(startString)) {
            int startIndex = errorDetails.indexOf(startString) + startString.length();
            return errorDetails.trim().substring(startIndex);
        }
        return "";
    }

    private boolean isPendingStatusTimeout(AddWinPendingOperation operation) {
        return System.currentTimeMillis() - operation.getCreateDate() > PENDING_STATUS_TIMEOUT;
    }

    @Override
    public boolean mergeNewAndExistingOperation(AddWinPendingOperation newOperation, IPendingOperation oldOperation)
            throws ObjectAlreadyExistsException {
        LOG.debug("mergeNewAndExistingOperation: merge not supported, newOperation={}, oldOperation={}", newOperation, oldOperation);
        if (oldOperation instanceof BuyInPendingOperation) {
            newOperation.setInnerOperation((BuyInPendingOperation) oldOperation);
            return true;
        } else if (oldOperation instanceof SitOutPendingOperation) {
            //nop, AddWinPendingOperation always sitOut
            return true;
        } else {
            throwMergeException();
        }
        return false;
    }
}
