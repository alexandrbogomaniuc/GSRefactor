package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraExternalTransactionPersister;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.abs.casino.common.cache.data.payment.PaymentMode;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.abs.casino.common.cache.data.payment.transfer.ExternalPaymentTransaction;
import com.abs.casino.common.cache.data.payment.transfer.TransactionStatus;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.IExternalWalletTransactionHandler;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 16.11.13
 */
public class ExternalTransactionHandler implements IExternalWalletTransactionHandler {
    protected static final Logger LOG = LogManager.getLogger(ExternalTransactionHandler.class);

    private final CassandraExternalTransactionPersister externalTransactionPersister;

    public ExternalTransactionHandler() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        externalTransactionPersister = persistenceManager.getPersister(CassandraExternalTransactionPersister.class);
    }

    @Override
    public void operationCreated(IWalletOperation operation) {
        LOG.info("called operationCreated, but operationCreated not supported");
    }

    @Override
    public void operationCompleted(IWalletOperation operation, long gameId) throws WalletException {
        LOG.debug("operationCompleted: gameId={}", gameId);
        if (!isSupportedGames(gameId)) {
            return;
        }
        //We don't check InternalTransaction status because on complete it must be COMPLETED
        ExternalPaymentTransaction transaction = externalTransactionPersister.
                getByInternalId(PaymentMode.WALLET, operation.getId());
        if (transaction == null) {
            LOG.warn("getExternalPaymentTransaction: ExternalPaymentTransaction not found, operation={}", operation);
            //vivo integration: internal rollback; external payment not found, but all completed
            if (gameId >= 415L && gameId <= 421L && operation.getExternalStatus() == WalletOperationStatus.COMPLETED &&
                    operation.getInternalStatus() == WalletOperationStatus.COMPLETED &&
                    operation.getType() == WalletOperationType.CREDIT) {
                LOG.warn("Found VIVO internal rollback, ext. transaction not created, just exit");
                return;
            }
            //for MQ/MP games only CREDIT external transactions created, skip DEBIT with any status and refunds
            if (isSupportedGames(gameId)) {
                if (operation.getType() == WalletOperationType.DEBIT) {
                    LOG.debug("operationCompleted: found DEBIT for MP games, just exit");
                    return;
                } else if (operation.getExternalStatus() == WalletOperationStatus.COMPLETED) {
                    LOG.debug("operationCompleted: found completed CREDIT (refund) for MP games, just exit");
                    return;
                }
            }
            throw new WalletException("ExternalPaymentTransaction not found");
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
        transaction.setStatus(status);
        transaction.setFinishDate(operation.getEndTime());
        LOG.info("operationCompleted: transaction={}, operation={}", transaction, operation);
        externalTransactionPersister.persist(transaction);
    }

    private boolean isSupportedGames(long gameId) {
        if (gameId >= 415L && gameId <= 421L) { //dump optimization
            return true;
        }
        BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(
                gameId);
        if (template == null) {
            LOG.error("isSupportedGames: BaseGameInfoTemplate not found for game: {}", gameId);
            return false;
        }
        return (template.getDefaultGameInfo() != null && template.getDefaultGameInfo().isThirdPartyGame()) ||
                template.isMultiplayerGame();
    }
}
