package com.dgphoenix.casino.gs.managers.game.history;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.DistributedLockManager;
import com.dgphoenix.casino.cassandra.persist.CassandraHistoryInformerItemPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.HistoryInformerItem;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.common.util.xml.IXmlRequestResult;
import com.abs.casino.common.util.xml.XmlRequestResult;
import com.abs.casino.common.util.xml.parser.Parser;
import com.dgphoenix.casino.gs.biz.GameHistory;
import com.dgphoenix.casino.gs.biz.GameHistoryListEntry;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HistoryInformerManager {
    private static final int MAX_ATTEMPTS = 3;
    private static final long MIN_INTERAVAL_BETWEEN_ATTEMPS = TimeUnit.MINUTES.toMillis(5);
    private static final long INTERVAL = TimeUnit.MINUTES.toMillis(3);
    private static final long FROZEN_INTERVAL = TimeUnit.MINUTES.toMillis(30);
    private static final int MAX_POOL_SIZE = 4;

    private static final String PARAM_USERID = "usedId";
    private static final String PARAM_GAMEID = "gameId";
    private static final String PARAM_SESSIONID = "sessionId";
    private static final String PARAM_DATA = "data";

    private static ScheduledExecutorService senderExecutorService;
    private static final Logger LOG = LogManager.getLogger(HistoryInformerManager.class);

    private static final Map<Long, Long> frozenBanks = new ConcurrentHashMap<>();

    private final XStream xstream;
    private final CassandraHistoryInformerItemPersister persister;
    private final DistributedLockManager lockManager;
    private final PlayerBetPersistenceManager betPersistenceManager;

    private static final String HISTORY_INFORMER_MANAGER_LOCK = "HISTORY_INFORMER_MANAGER";

    public HistoryInformerManager(CassandraPersistenceManager persistenceManager, PlayerBetPersistenceManager playerBetPersistenceManager) {
        persister = persistenceManager.getPersister(CassandraHistoryInformerItemPersister.class);
        lockManager = persistenceManager.getPersister(DistributedLockManager.class);
        betPersistenceManager = playerBetPersistenceManager;

        xstream = new XStream();
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.**"});
        xstream.alias(GameHistory.class.getSimpleName(), GameHistory.class);
        xstream.alias("Entry", GameHistoryListEntry.class);
        xstream.addImplicitCollection(GameHistory.class, "entries");
    }

    public Set<Long> getFrozenBanks() {
        return frozenBanks.keySet();
    }

    public static int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    public static long getMinIntervalBetweenAttempts() {
        return MIN_INTERAVAL_BETWEEN_ATTEMPS;
    }

    public static long getInterval() {
        return INTERVAL;
    }

    @PostConstruct
    private void startupExecutors() {
        List<BankInfo> bankInfoList = BankInfoCache.getInstance().getAllObjects().values().stream()
                .filter(bankInfo -> !StringUtils.isTrimmedEmpty(bankInfo.getHistoryInformerServiceUrl())
                        && bankInfo.getId() != 696L)
                .collect(Collectors.toList());

        int threadCount = bankInfoList.size();
        if (threadCount > MAX_POOL_SIZE) {
            threadCount = MAX_POOL_SIZE;
        }

        senderExecutorService = Executors.newScheduledThreadPool(threadCount);

        bankInfoList.forEach(
                bankInfo ->
                        senderExecutorService.scheduleWithFixedDelay(new SendItemsTask(bankInfo), 0,
                                INTERVAL, TimeUnit.MILLISECONDS)
        );

        LOG.debug("Executors started");
    }

    @PreDestroy
    private void shutdownExecutors() {
        try {
            ExecutorUtils.shutdownService(this.getClass().getSimpleName(), senderExecutorService, 2000);
            frozenBanks.clear();
            LOG.debug("Executors has been shutdown");
        } catch (Throwable e) {
            LOG.error("Cannot shutdown executors in HistoryInformerManager", e);
        }
    }

    public void createHistoryItem(GameSession gameSession, String accountExternalId) {
        try {
            String url = BankInfoCache.getInstance().getBankInfo(gameSession.getBankId()).getHistoryInformerServiceUrl();
            if (StringUtils.isTrimmedEmpty(url)) {
                return;
            }

            HistoryInformerItem item = new HistoryInformerItem(gameSession.getId(), gameSession.getAccountId(), accountExternalId,
                    gameSession.getBankId(), gameSession.getGameId(), null);

            try {
                persister.insert(item);
                LOG.info("createHistoryItem: {}", item.toString());
            } catch (Throwable ex) {
                LOG.error("Item saving: {}", item.toString(), ex);
            }

        } catch (Throwable e) {
            LOG.error("HistoryInformerManager unable to create HistoryInformerItem", e);
        }
    }

    protected boolean isPost() {
        return true;
    }

    private class SendItemsTask implements Runnable {
        private final BankInfo bankInfo;

        public SendItemsTask(BankInfo bankInfo) {
            this.bankInfo = bankInfo;
        }

        @Override
        public void run() {
            LockingInfo lock;
            String lockId = HISTORY_INFORMER_MANAGER_LOCK + "_" + bankInfo.getId();

            try {
                lock = lockManager.tryLock(lockId);
            } catch (Throwable e) {
                LOG.warn("Can't get lock for bankId=" + bankInfo.getId() + " lockId=" + lockId, e);
                return;
            }
            try {
                //check frozen
                if (frozenBanks.containsKey(bankInfo.getId())) {
                    if (System.currentTimeMillis() - frozenBanks.get(bankInfo.getId()) < FROZEN_INTERVAL) {
                        LOG.info("Bank {} is still frozen", bankInfo.getId());
                        return;
                    } else {
                        frozenBanks.remove(bankInfo.getId());
                        LOG.info("Bank {} has been unfrozen", bankInfo.getId());
                    }
                }

                final String url = bankInfo.getHistoryInformerServiceUrl();
                if (StringUtils.isTrimmedEmpty(url)) {
                    return;
                }
                persister.processItemsForBank(bankInfo.getId(), MIN_INTERAVAL_BETWEEN_ATTEMPS,
                        item -> {
                            boolean dataUpdated = false;
                            if (item.getData() == null) {
                                List<PlayerBet> bets = betPersistenceManager.getBets(item.getGameSessionId());
                                String playerBetsAsString = getPlayerBetsAsString(bets);
                                item.setData(playerBetsAsString);
                                dataUpdated = true;
                            }

                            if (sendItem(item, url, bankInfo.isUseHttpProxy())) {
                                persister.delete(item);
                                LOG.info("Item deleted (sending success): {}", item);
                            } else {
                                item.setIterations(item.getIterations() + 1);

                                if (item.getIterations() > MAX_ATTEMPTS) {
                                    frozenBanks.put(item.getBankId(), System.currentTimeMillis());
                                    LOG.info("Bank {} has been frozen", bankInfo.getId());

                                    persister.update(item, MAX_ATTEMPTS);
                                } else {
                                    if (dataUpdated) {
                                        persister.insert(item);
                                    } else {
                                        persister.update(item, MAX_ATTEMPTS);
                                    }
                                }
                            }
                        }
                );

            } catch (Throwable e) {
                LOG.error("SendItemsTask error", e);
            } finally {
                if (lock != null) {
                    lockManager.unlock(lock);
                }
            }
        }

        private boolean sendItem(HistoryInformerItem item, String url, boolean useProxy) {
            HashMap<String, String> params = new HashMap<>();
            params.put(PARAM_USERID, item.getExternalAccountId());
            params.put(PARAM_GAMEID, String.valueOf(item.getGameId()));
            params.put(PARAM_SESSIONID, String.valueOf(item.getGameSessionId()));
            params.put(PARAM_DATA, item.getData());

            IXmlRequestResult output;
            try {
                output = request(params, url, useProxy);
            } catch (CommonException e) {
                return false;
            }
            return output.isSuccessful();
        }

        private XmlRequestResult request(HashMap<String, String> params, String url, boolean useProxy) throws CommonException {
            try {
                String paramsStr = printRequestParams(params);
                if (paramsStr.length() > 200) {
                    paramsStr = paramsStr.substring(0, 200) + "...";
                }

                LOG.info("HistoryInformerManager::request, request to url: {} is: {}", url, paramsStr);
                String sb = HttpClientConnection.newInstance().doRequest(url, params, isPost(), useProxy);
                LOG.info("HistoryInformerManager::request, response from url: {} is: {}", url, sb);
                XmlRequestResult result = new XmlRequestResult();
                Parser parser = Parser.instance();
                parser.parse(sb, result);
                return result;
            } catch (Exception e) {
                LOG.error("HistoryInformerManager::request error:", e);
                throw new CommonException(e);
            }
        }

        private String printRequestParams(HashMap<String, String> params) {
            StringBuilder sb = new StringBuilder(" request parameters:");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append(";");
            }

            return sb.toString();
        }

        private String getPlayerBetsAsString(List<PlayerBet> bets) {
            List<GameHistoryListEntry> listEntries = bets.stream()
                    .map(GameHistoryListEntry::new)
                    .collect(Collectors.toList());

            GameHistory gameHistory = new GameHistory(listEntries);
            return xstream.toXML(gameHistory);
        }
    }
}
