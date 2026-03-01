package com.abs.casino.gs.managers.payment.wallet;

import com.abs.casino.common.exception.NotEnoughMoneyException;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: flsh
 * Date: 5/6/11
 */
public class RemoteClientStubHelper {
    private static final Logger LOG = LogManager.getLogger(RemoteClientStubHelper.class);

    private static final RemoteClientStubHelper instance = new RemoteClientStubHelper();
    private final Map<String, ExtAccountInfoStub> infosMap = new ConcurrentHashMap<>();
    private static final int CACHE_SIZE = 300;
    private final Queue<Long> processedWins;
    private final Map<Long, Long> processedBetsWithAmount;
    private final Map<Long, String> processedUnjContrAmount;

    {
        EvictingQueue<Long> queue = EvictingQueue.create(CACHE_SIZE);
        processedWins = Queues.synchronizedQueue(queue);
        processedBetsWithAmount = Collections.synchronizedMap(new LinkedHashMap<Long, Long>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
                return size() > CACHE_SIZE;
            }
        });
        processedUnjContrAmount = Collections.synchronizedMap(new LinkedHashMap<Long, String>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
                return size() > CACHE_SIZE;
            }
        });
    }

    private RemoteClientStubHelper() {
    }

    public static RemoteClientStubHelper getInstance() {
        return instance;
    }

    public ExtAccountInfoStub getExtAccountInfo(String accountId) {
        ExtAccountInfoStub extAccount = infosMap.putIfAbsent(accountId, new ExtAccountInfoStub(accountId));
        return extAccount != null ? extAccount : infosMap.get(accountId);
    }

    public void makeBet(String accountId, long betAmount) throws NotEnoughMoneyException {
        getExtAccountInfo(accountId).makeBet(betAmount);
    }

    public void makeRecordedBet(String accountId, long betAmount, long transactionId) throws NotEnoughMoneyException {
        if (!processedBetsWithAmount.containsKey(transactionId)) {
            makeBet(accountId, betAmount);
            processedBetsWithAmount.put(transactionId, betAmount);
        } else {
            LOG.debug("Bet already completed, amount={}, transactionId={}",
                    processedBetsWithAmount.get(transactionId), transactionId);
        }
    }

    public void makeRecordedUnjContr(String accountId, String unjContr, long transactionId) {
        if (!processedUnjContrAmount.containsKey(transactionId)) {
            //makeBet(accountId, unjContr);
            processedUnjContrAmount.put(transactionId, unjContr);
        } else {
            LOG.debug("Unj contr already completed, amount={}, transactionId={}",
                    processedUnjContrAmount.get(transactionId), transactionId);
        }
    }

    public void makeWin(String accountId, long winAmount) {
        getExtAccountInfo(accountId).makeWin(winAmount);
    }

    public void makeRecordedWin(String accountId, long winAmount, long transactionId) {
        if (!processedWins.contains(transactionId)) {
            makeWin(accountId, winAmount);
            processedWins.add(transactionId);
        } else {
            LOG.debug("Win already completed transactionId={}", transactionId);
        }
    }

    public CWError refundBet(String accountId, long betId) {
        if (infosMap.get(accountId) == null) {
            //may be after reboot, just log and return
            LOG.warn("refundBet: account {} not found", accountId);
            getExtAccountInfo(accountId);
            return null;
        }
        Long amount = processedBetsWithAmount.get(betId);
        if (amount == null) {
            return CommonWalletErrors.UNKNOWN_TRANSACTION_ID;
        }
        makeWin(accountId, amount); // Refund
        processedBetsWithAmount.remove(betId);
        return null;
    }

    public Map<Long, Long> getProcessedBetsWithAmount() {
        return ImmutableMap.copyOf(processedBetsWithAmount);
    }

    public Map<Long, String> getProcessedUnjContrAmount() {
        return ImmutableMap.copyOf(processedUnjContrAmount);
    }

    public List<Long> getProcessedWins() {
        return ImmutableList.copyOf(processedWins);
    }

    public void resetAllStubBalances() {
        infosMap.clear();
        processedBetsWithAmount.clear();
        processedWins.clear();
        processedUnjContrAmount.clear();
    }

    public class ExtAccountInfoStub implements Serializable {
        private final String accountId;
        private long balance;
        private long bet;
        private long win;

        public ExtAccountInfoStub(String accountId) {
            this.accountId = accountId;
            this.balance = 10000000L;
            this.bet = 0;
            this.win = 0;
        }

        public synchronized void makeBet(long bet) throws NotEnoughMoneyException {
            LOG.debug("makeBet: accountId={}, oldBalance={}, bet={}", accountId, this.balance, bet);
            this.bet += bet;
            if (balance - bet < 0) {
                throw new NotEnoughMoneyException("Balance too small, current=" + balance + ", bet=" + bet);
            }
            this.balance -= bet;
        }

        public synchronized void makeWin(long win) {
            LOG.debug("makeWin: accountId={}, oldBalance={}, win={}", accountId, this.balance, win);
            this.win += win;
            this.balance += win;
        }

        public String getAccountId() {
            return accountId;
        }

        public long getBalance() {
            return balance;
        }

        public synchronized void setBalance(long balance) {
            LOG.debug("setBalance: accountId={}, oldBalance={}, balance={}", accountId, this.balance, balance);
            this.balance = balance;
        }

        public long getBet() {
            return bet;
        }

        public synchronized void setBet(long bet) {
            LOG.debug("setBet: accountId={}, balance={}, bet={}", accountId, this.balance, bet);
            this.bet = bet;
        }

        public long getWin() {
            return win;
        }

        public void setWin(long win) {
            LOG.debug("setWin: accountId={}, balance={}, win={}", accountId, this.balance, win);
            this.win = win;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ExtAccountInfoStub [");
            sb.append("accountId='").append(accountId).append('\'');
            sb.append(", balance=").append(balance);
            sb.append(", bet=").append(bet);
            sb.append(", win=").append(win);
            sb.append(']');
            return sb.toString();
        }
    }
}
