package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraExternalTransactionPersister;
import com.dgphoenix.casino.common.cache.data.payment.PaymentMode;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.abs.casino.common.cache.data.payment.transfer.ExternalPaymentTransaction;
import com.abs.casino.common.cache.data.payment.transfer.TransactionStatus;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.IExternalWalletTransactionHandler;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 09.08.18.
 */
public class MultiplayerExternalWallettransactionHandler implements IExternalWalletTransactionHandler {
    protected static final Logger LOG = LogManager.getLogger(MultiplayerExternalWallettransactionHandler.class);
    private final long bankId;
    private final String mpSideTransactionId;
    private final CassandraExternalTransactionPersister externalTransactionPersister;

    public MultiplayerExternalWallettransactionHandler(long bankId, String mpSideTransactionId) {
        this.bankId = bankId;
        this.mpSideTransactionId = mpSideTransactionId;
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        externalTransactionPersister = persistenceManager.getPersister(CassandraExternalTransactionPersister.class);
    }

    @Override
    public void operationCreated(IWalletOperation operation) throws WalletException {
        ExternalPaymentTransaction transaction = getExternalPaymentTransaction();
        LOG.debug("operationCreated: transaction={}, operation={}", transaction, operation);
        transaction.setInternalOperationId(operation.getId());
        transaction.setOperation((CommonWalletOperation) operation);
        externalTransactionPersister.persist(transaction);
    }

    private ExternalPaymentTransaction getExternalPaymentTransaction() throws WalletException {
        ExternalPaymentTransaction transaction = externalTransactionPersister.get(bankId, mpSideTransactionId);
        if (transaction == null) {
            LOG.error("getExternalPaymentTransaction error, ExternalPaymentTransaction not found, " +
                    "bankId={}, mpSideTransactionId={}", bankId, mpSideTransactionId);
            throw new WalletException("ExternalPaymentTransaction not found");
        }
        return transaction;
    }

    @Override
    public void operationCompleted(IWalletOperation operation, long gameId) throws WalletException {
        LOG.debug("operationCompleted: operation={}", operation);
        //We don't check InternalTransaction status because on complete it must be COMPLETED
        ExternalPaymentTransaction transaction = externalTransactionPersister.
                getByInternalId(PaymentMode.WALLET, operation.getId());
        boolean notFoundByOperation = false;
        if (transaction == null) {
            LOG.error("getExternalPaymentTransaction error, ExternalPaymentTransaction not found " +
                    "(try load by vivoSideTransactionId), " +
                    "bankId={}, operation.getId={}", bankId, operation.getId());
            notFoundByOperation = true;
        }
        transaction = getExternalPaymentTransaction();
        if (notFoundByOperation) {
            LOG.error("Strange error, not found by operation, but found by vivoSideTransactionId, transaction={}, operation={}",
                    transaction, operation);
            transaction.setInternalOperationId(operation.getId());
        }
        TransactionStatus status;
        if (operation.getExternalStatus() == WalletOperationStatus.COMPLETED) {
            status = TransactionStatus.APPROVED;
        } else if (operation.getExternalStatus() == WalletOperationStatus.FAIL) {
            status = TransactionStatus.FAILED;
        } else if (operation.getExternalStatus() == WalletOperationStatus.PENDING) {
            status = TransactionStatus.PENDING;
        } else {
            LOG.error("Bad external transaction status, operation={}, extTransaction={}", operation, transaction);
            throw new WalletException("Bad external transaction status");
        }
        transaction.setOperation((CommonWalletOperation) operation);
        transaction.setStatus(status);
        transaction.setFinishDate(operation.getEndTime());
        LOG.info("operationCompleted: {}, operation={}", transaction, operation);
        externalTransactionPersister.persist(transaction);
    }

}
