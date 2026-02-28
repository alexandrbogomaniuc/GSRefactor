package com.abs.casino.gs.managers.payment.transfer.tracker;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.dgphoenix.casino.common.cache.data.payment.transfer.TransactionStatus;
import com.abs.casino.common.cache.data.payment.transfer.processor.IPaymentProcessor;
import com.abs.casino.common.engine.tracker.AbstractCommonTrackingTask;
import com.abs.casino.common.exception.CannotLockException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.TransactionDataTracker;
import com.dgphoenix.casino.gs.managers.payment.transfer.PaymentManager;
import com.abs.casino.gs.managers.payment.transfer.processor.PaymentProcessorFactory;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;

public class PaymentTransactionTrackerTask extends AbstractCommonTrackingTask<Long, PaymentTransactionTracker> {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(PaymentTransactionTrackerTask.class);

    public PaymentTransactionTrackerTask(long accountId, PaymentTransactionTracker tracker) {
        super(accountId, tracker);
    }

    @Override
    public void process() throws CommonException {
        process(false, TransactionDataTracker.START_TASK_PAUSE_IN_MSEC);
    }

    @Override
    protected long getTaskSleepTimeout() throws CommonException {
        return GameServerConfiguration.getInstance().getWalletTrackerSleepTimeout();
    }

    public void process(boolean throwTimeoutException, long timeoutInMillis) throws CommonException {
        PaymentTransaction transaction;
        IPaymentProcessor processor;
        boolean transactionAlreadyStarted;
        Long accountId = getKey();
        try {
            transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().lock(accountId, timeoutInMillis);
            }
        } catch (CannotLockException e) {
            if (throwTimeoutException) {
                throw e;
            }
            LOG.warn("Cannot lock, exit. accountId=" + accountId + ", exception=" + e);
            return;
        } catch (Throwable e) {
            LOG.error("Cannot lock, accountId=" + accountId +
                    ", td=" + SessionHelper.getInstance().getTransactionData());
            throw e;
        }
        boolean foreignTask = false;
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            int currentLocker = SessionHelper.getInstance().getTransactionData().getLastLockerId();
            if (currentLocker != GameServer.getInstance().getServerId()) {
                LOG.warn("lockerId mismatch: accountId=" + accountId + ", currentLocker=" + currentLocker +
                        ", original GS may be down");
                foreignTask = true;
                //return;
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, true);
            if (accountInfo == null || accountInfo.isLocked()) {
                LOG.warn("Stop processing accountInfo is null or locked: accountId=" + accountId);
            } else {
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
                processor = PaymentProcessorFactory.getInstance().getProcessor(bankInfo);

                transaction = PaymentManager.getInstance().getTransaction();
                LOG.info("run tracking for transaction:" + transaction);

                if (transaction != null) {
                    long balance = transaction.getAmount();
                    switch (transaction.getType()) {
                        case DEPOSIT: {
                            boolean processRevokeDepositError = false;
                            try {
                                processor.processRevokeDeposit(transaction, accountInfo);
                            } catch (CommonException e) {
                                processRevokeDepositError = true;
                                throw e;
                            } finally {
                                if (transaction.getStatus().equals(TransactionStatus.APPROVED)) {
                                    try {
                                        finishTransaction(transaction, balance);
                                    } catch (CommonException e) {
                                        if (processRevokeDepositError) {
                                            LOG.error("finishTransaction error: transaction=" + transaction
                                                    + ", balance=" + balance, e);
                                        } else {
                                            throw e;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case WITHDRAWAL: {
                            if (transaction.getStatus() != TransactionStatus.APPROVED) {
                                processor.processRevokeWithdrawal(transaction, accountInfo);
                            } else {
                                LOG.info("Transaction is approved, finish processing: remove transaction " +
                                        "(this may be bug, approved transaction already must be removed), " +
                                        "accountId=" + accountId);
                                SessionHelper.getInstance().getTransactionData().setPaymentTransaction(null);
                            }
                            finishTransaction(transaction, balance);
                            break;
                        }
                        default: {
                            LOG.warn("run transaction type:" + transaction.getType() + " is unsupported, stop tracking");
                            finishTransaction(transaction.copy(), balance);
                            break;
                        }

                    }
                }
                if (!transactionAlreadyStarted) {
                    SessionHelper.getInstance().commitTransaction();
                }
            }
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Throwable e) {
            if (!foreignTask) {
                throw e;
            } else {
                LOG.warn("Stop processing, foreignTask accountId=" + accountId);
            }
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
    }

    public void handleCommonException(boolean done, boolean fatalError, CommonException exception) {
        super.handleCommonException(done, fatalError, exception);
    }

    private void finishTransaction(PaymentTransaction transaction, long balance) throws CommonException {
        PaymentManager.saveToArchive(transaction);
        LOG.info("run transaction:" + transaction + " tracked ok");
    }

}
