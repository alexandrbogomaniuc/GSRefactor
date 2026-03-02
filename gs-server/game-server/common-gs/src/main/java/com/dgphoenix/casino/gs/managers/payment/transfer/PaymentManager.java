package com.abs.casino.gs.managers.payment.transfer;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraPaymentTransactionPersister;
import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.payment.transfer.PaymentSystemType;
import com.abs.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.abs.casino.common.cache.data.payment.transfer.TransactionStatus;
import com.abs.casino.common.cache.data.payment.transfer.TransactionType;
import com.abs.casino.common.cache.data.payment.transfer.paymentmean.PaymentMeanId;
import com.abs.casino.common.cache.data.payment.transfer.paymentmean.PaymentMeanType;
import com.abs.casino.common.cache.data.payment.transfer.processor.IPaymentProcessor;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.gs.GameServer;
import com.abs.casino.gs.managers.payment.transfer.processor.PaymentProcessorFactory;
import com.abs.casino.gs.managers.payment.transfer.tracker.PaymentTransactionTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaymentManager {
    private static final Logger LOG = LogManager.getLogger(PaymentManager.class);
    private static final PaymentManager instance = new PaymentManager();

    private static CassandraPaymentTransactionPersister paymentTransactionPersister;

    public static PaymentManager getInstance() {
        return instance;
    }

    private PaymentManager() {
    }

    private static CassandraPaymentTransactionPersister getPaymentTransactionPersister() {
        if (paymentTransactionPersister == null) {
            CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                    .getBean("persistenceManager", CassandraPersistenceManager.class);
            paymentTransactionPersister = persistenceManager.getPersister(CassandraPaymentTransactionPersister.class);
        }
        return paymentTransactionPersister;
    }

    public PaymentTransaction processDeposit(AccountInfo accountInfo, BankInfo bankInfo, Long gameSessionId, Long gameId,
                                             long amount, String extTransactionId, boolean realMoney,
                                             ClientType clientType, String comment)
            throws CommonException {

        IPaymentProcessor processor = PaymentProcessorFactory.getInstance().getProcessor(bankInfo);

        PaymentTransaction transaction = processor
                .processDeposit(accountInfo, gameSessionId, gameId, amount, extTransactionId, realMoney, clientType,
                        comment);
        //only Facebook not individually tracked
        if (transaction.getPaymentSystemType().isIndividuallyTracked()) {
            if (transaction.getStatus() == TransactionStatus.PENDING) {
                //means that transaction has failed and need to be revoked
                LOG.info("processDeposit accountId:{}, gameSessionId:{}, amount:{} status:{} IS MARKED FOR TRACKING", accountInfo.getId(),
                        gameSessionId, amount, transaction.getStatus());
                PaymentTransactionTracker.getInstance().addTask(accountInfo.getId());
            } else {
                //approved/failed transaction removed in processDepositTransaction()
                LOG.info("processDeposit accountId:{}, gameSessionId:{}, amount:{}, status:{} completed", accountInfo.getId(), gameSessionId,
                        amount, transaction.getStatus());
            }
        }
        return transaction;
    }

    public PaymentTransaction processWithdrawal(AccountInfo accountInfo, BankInfo bankInfo, Long gameSessionId,
                                                Long gameId, long amount, String extTransactionId, boolean realMoney,
                                                ClientType clientType, String comment)
            throws CommonException {

        IPaymentProcessor processor = PaymentProcessorFactory.getInstance().getProcessor(bankInfo);

        PaymentTransaction transaction = processor
                .processWithdrawal(accountInfo, gameSessionId, gameId, amount, extTransactionId, realMoney, clientType,
                        comment);

        if (transaction.getStatus() == TransactionStatus.PENDING) {
            //means that transaction has failed and need to be revoked
            LOG.info("processWithdrawal accountId:{}, gameSessionId:{}, amount:{}, status:{} IS MARKED FOR TRACKING", accountInfo.getId(),
                    gameSessionId, amount, transaction.getStatus());
            PaymentTransactionTracker.getInstance().addTask(accountInfo.getId());
        } else {
            //approved transaction removed in processWithdrawalTransaction()
            LOG.info("processWithdrawal accountId:{}, gameSessionId:{}, amount:{}, status:{} completed", accountInfo.getId(), gameSessionId,
                    amount, transaction.getStatus());
        }
        return transaction;
    }

    public static void saveToArchive(PaymentTransaction transaction) {
        getPaymentTransactionPersister().save(transaction);
    }

    public static PaymentTransaction createTransaction(IPaymentProcessor processor,
                                                       AccountInfo accountInfo, long amount, Long gameSessionId,
                                                       Long gameId, TransactionType transactionType,
                                                       String extTransactionId, PaymentSystemType psType,
                                                       PaymentMeanType pmType, PaymentMeanId paymentMean,
                                                       ClientType clientType, String comment) throws CommonException {
        ITransactionData data = SessionHelper.getInstance().getTransactionData();
        if (data.getPaymentTransaction() != null) {
            LOG.warn("Previous transaction not completed: {}", data.getPaymentTransaction());
            throw new CommonException("Previous transaction not completed");
        }
        long transactionId = GameServer.getInstance().getIdGenerator().getNext(PaymentTransaction.class);
        PaymentTransaction transaction =
                new PaymentTransaction(transactionId, accountInfo.getId(), amount, gameSessionId,
                        System.currentTimeMillis(), TransactionStatus.STARTED, transactionType, extTransactionId,
                        psType, pmType,
                        paymentMean, true, gameId, accountInfo.getSubCasinoId(), clientType,
                        accountInfo.getCurrency(), comment);
        LOG.debug("createTransaction transactionCreated:{}", transaction);
        if (processor.isTrackable()) {
            data.setPaymentTransaction(transaction);
            SessionHelper.getInstance().getDomainSession().persistPaymentTransaction(
                    transaction.getType() == TransactionType.WITHDRAWAL);
        }
        return transaction;
    }


    public PaymentTransaction getTransaction() {
        return SessionHelper.getInstance().getTransactionData().getPaymentTransaction();
    }

    public Long getTrackingTransactionId() {
        PaymentTransaction transaction = SessionHelper.getInstance().getTransactionData().getPaymentTransaction();
        return transaction == null ? null : transaction.getId();
    }

    public PaymentTransaction getTransactionIdByExtId(long bankId, String extId) {
        PaymentTransaction transaction = SessionHelper.getInstance().getTransactionData().getPaymentTransaction();
        if (transaction != null && transaction.getExternalTransactionId() != null &&
                transaction.getExternalTransactionId().equals(extId)) {
            return transaction;
        }
        return getPaymentTransactionPersister().getTransactionByExtId(bankId, extId);
    }
}
