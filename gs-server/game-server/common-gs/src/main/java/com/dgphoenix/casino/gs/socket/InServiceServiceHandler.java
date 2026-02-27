package com.dgphoenix.casino.gs.socket;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abs.casino.cache.CachesHolder;
import com.dgphoenix.casino.cassandra.AccountDistributedLockManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.DistributedLockManager;
import com.dgphoenix.casino.cassandra.persist.AbstractDistributedConfigEntryPersister;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.IDistributedCache;
import com.dgphoenix.casino.common.cache.ILoadingCache;
import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.dgphoenix.casino.common.cache.ServerConfigsCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.config.GameServerConfig;
import com.dgphoenix.casino.common.lock.ILockManager;
import com.dgphoenix.casino.common.promo.Status;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.dgphoenix.casino.gs.IGameServerStatusListener;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusWinRequestFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.RemoteClientStubHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.gs.status.ServersStatusWatcher;
import com.dgphoenix.casino.kafka.dto.GameServerInfoDto;
import com.dgphoenix.casino.kafka.dto.KafkaHandlerException;

@Component
public class InServiceServiceHandler {
    private static final Logger LOG = LogManager.getLogger(InServiceServiceHandler.class);

    private final MQServiceHandler mqServiceHandler;
    private final ServersStatusWatcher serversStatusWatcher;

    private final Map<String, ILockManager> lockManagers = new ConcurrentHashMap<>();

    @Autowired
    public InServiceServiceHandler(MQServiceHandler mqServiceHandler,
                                   ServersStatusWatcher serversStatusWatcher,
                                   CassandraPersistenceManager persistenceManager) {
        this.mqServiceHandler = mqServiceHandler;
        this.serversStatusWatcher = serversStatusWatcher;

        AccountDistributedLockManager accountDistributedLockManager = persistenceManager
                .getPersister(AccountDistributedLockManager.class);
        addLockManager(accountDistributedLockManager);
        DistributedLockManager distributedLockManager = persistenceManager
                .getPersister(DistributedLockManager.class);
        addLockManager(distributedLockManager);
    }

    public void invalidateAllBaseGameInfo(long bankId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        LOG.debug("invalidateAllBaseGameInfo={}, bank={}", bankId, bankInfo);
        if (bankInfo != null) {
            List<Currency> currencies = bankInfo.getCurrencies();
            Set<Long> gamesSet = BaseGameCache.getInstance().getAllGamesSet(bankId, bankInfo.getDefaultCurrency());
            for (Currency currency : currencies) {
                for (Long gameId : gamesSet) {
                    String composedKey = BaseGameCache.getInstance().composeGameKey(bankId, gameId, currency);
                    BaseGameCache.getInstance().invalidate(composedKey);
                }
            }
        }
    }

    public void invalidateLocalBaseGameInfo(long bankId, long gameId, ICurrency currency) {
        BaseGameCache baseGameCache = BaseGameCache.getInstance();
        String composedKey = baseGameCache.composeGameKey(bankId, gameId, currency);
        baseGameCache.invalidate(composedKey);
        //also need invalidate all slave banks
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        long subCasinoId = bankInfo.getSubCasinoId();
        SubCasino subCasino = SubCasinoCache.getInstance().get(subCasinoId);
        List<Long> bankIds = subCasino.getBankIds();
        Set<Long> invalidateBanks = new HashSet<>();
        invalidateBanks.add(bankId);
        for (Long currentBankId : bankIds) {
            if (currentBankId == bankId) {
                //already processed
                continue;
            }
            BankInfo possibleSlave = BankInfoCache.getInstance().getBankInfo(currentBankId);
            if (possibleSlave != null && possibleSlave.getMasterBankId() != null && possibleSlave.getMasterBankId() > 0
                    && possibleSlave.getMasterBankId() == bankId) {
                List<Currency> slaveCurrencies = possibleSlave.getCurrencies();
                for (Currency slaveCurrency : slaveCurrencies) {
                    LOG.debug("saveAndSendNotification: found slave bank, invalidate: slave bankId = {}, gameId = {}," +
                            " currencyCode={}, masterBankId={}", currentBankId, gameId, slaveCurrency, bankId);
                    String invalidatedKey = BaseGameCache.getInstance()
                            .composeGameKey(currentBankId, gameId, slaveCurrency);
                    BaseGameCache.getInstance().invalidate(invalidatedKey);
                }
                invalidateBanks.add(currentBankId);
            }
        }
    }

    public void notifyPromoCampaignCreated(long promoCampaignId) {
        LOG.debug("notifyPromoCampaignCreated: promoCampaignId={}", promoCampaignId);
        try {
            GameServerComponentsHelper.getTournamentManager().notifyCampaignCreated(promoCampaignId);
        } catch (Exception e) {
            LOG.error("notifyPromoCampaignCreated error", e);
            throw e;
        }
    }

    public void invalidatePromoCampaignCache(long promoCampaignId) {
        LOG.debug("invalidatePromoCampaignCache: promoCampaignId={}", promoCampaignId);
        try {
            GameServerComponentsHelper.getPromoCampaignManager().invalidateCachedCampaign(promoCampaignId);
        } catch (Exception e) {
            LOG.error("invalidatePromoCampaignCache error", e);
            throw e;
        }
    }

    public void notifyPromoCampaignStatusChanged(long promoCampaignId, String oldStatus, String newStatus) {
        LOG.debug("notifyPromoCampaignStatusChanged: promoCampaignId={}, oldStatus={}, " +
                "newStatus={}", promoCampaignId, oldStatus, newStatus);
        try {
            GameServerComponentsHelper.getPromoCampaignManager().invalidateCachedCampaign(promoCampaignId);
            GameServerComponentsHelper.getPromoMessagesDispatcher()
                    .notifyPromoCampaignStatusChanged(promoCampaignId, Status.valueOf(newStatus));
            GameServerComponentsHelper.getTournamentManager().notifyCampaignStatusChanged(promoCampaignId,
                    Status.valueOf(oldStatus), Status.valueOf(newStatus));
        } catch (Exception e) {
            LOG.error("notifyPromoCampaignStatusChanged error, promoCampaignId={}", promoCampaignId, e);
            throw e;
        }
    }

    public void sendPromoNotifications(String sessionId, long campaignId, Set<com.dgphoenix.casino.kafka.dto.PromoNotificationType> notificationsTypes) {
        try {
            LOG.debug("sendPromoNotifications: sessionId = {}, campaignId = {}, notificationsTypes = {}",
                    sessionId, campaignId, notificationsTypes);
            Set<com.dgphoenix.casino.common.promo.PromoNotificationType> types = new HashSet<>();
            for (com.dgphoenix.casino.kafka.dto.PromoNotificationType notificationType : notificationsTypes) {
                types.add(com.dgphoenix.casino.common.promo.PromoNotificationType.valueOf(notificationType.name()));
            }
            GameServerComponentsHelper.getPromoMessagesDispatcher()
                    .sendPromoNotificationsAsync(sessionId, campaignId, types);
        } catch (Exception e) {
            LOG.error("sendPromoNotifications error", e);
            throw e;
        }
    }

    public void notifySessionClosed(String sessionId) {
        LOG.debug("notifySessionClosed: sessionId = {}", sessionId);
        try {
            GameServerComponentsHelper.getWebSocketSessionsController().closeWebSocketForPlayerSession(sessionId);
        } catch (Exception e) {
            LOG.error("notifySessionClosed error", e);
            throw e;
        }
    }

    public void refreshConfig(String configName, String objectId) {
        try {
            CachesHolder cachesHolder = ApplicationContextHelper.getApplicationContext()
                    .getBean("cachesHolder", CachesHolder.class);
            AbstractCassandraPersister persister = cachesHolder.getConfigPersistersMap().get(configName);
            LOG.debug("refreshConfig: configName={}, objectId={}", configName, objectId);
            if (persister == null) {
                IDistributedCache cache = cachesHolder.getCacheMap().get(configName);
                if (cache instanceof ILoadingCache) {
                    ((ILoadingCache) cache).invalidate(objectId);
                } else {
                    LOG.error("Unknown config: {}, id={}", configName, objectId);
                }
            } else {
                if (persister instanceof AbstractDistributedConfigEntryPersister) {
                    ((AbstractDistributedConfigEntryPersister<?, ?>) persister).refresh(objectId);
                }
            }
        } catch (Exception e) {
            LOG.error("refreshConfig error: configName={}, objectId={}", configName, objectId, e);
            throw e;
        }
    }

    public void invalidateFrBonusClient(long bankId) {
        FRBonusManager.getInstance().invalidateClient(bankId);
    }

    public void invalidateBonusClient(long bankId) {
        BonusManager.getInstance().invalidateClient(bankId);
    }

    public void invalidateFrBonusWinManager(long bankId) {
        FRBonusWinRequestFactory.getInstance().invalidateManager(bankId);
    }

    public void invalidateWalletManager(long bankId) {
        WalletProtocolFactory.getInstance().invalidateManager(bankId);
    }

    public void updateStubBalanceByExternalUserId(String externalUserId, long balance) {
        RemoteClientStubHelper.getInstance().getExtAccountInfo(externalUserId).setBalance(balance);
    }

    public void sendPlayerTournamentStateChanged(String sessionId, long tournamentId, boolean cannotJoin,
                                                 boolean joined) {
        mqServiceHandler.sendPlayerTournamentStateChanged(sessionId, tournamentId, cannotJoin, joined);
    }

    public void sendBalanceUpdated(String sessionId, long balance) {
        mqServiceHandler.sendBalanceUpdated(sessionId, balance);
    }

    public void changeServersStatuses(Map<Integer, Boolean> serversStatuses) {
        try {
            for (Map.Entry<Integer, Boolean> serverStatusEntry : serversStatuses.entrySet()) {
                Integer serverId = serverStatusEntry.getKey();
                Boolean serverStatus = serverStatusEntry.getValue();
                LOG.debug("changeServerStatus: server={}, online={}", serverId, serverStatus);
                ServerInfo info = LoadBalancerCache.getInstance().getServerInfoById(serverId);
                if (info != null) {
                    if (info.getId() == GameServer.getInstance().getServerId()) {
                        LOG.warn("Cannot change self status");
                    } else {
                        info.setOnline(serverStatus);
                        //notify listeners on remote GS
                        for (IGameServerStatusListener listener : serversStatusWatcher.getServerStatusListeners()) {
                            listener.notify(serverId, serverStatus);
                        }
                    }
                } else {
                    LOG.error("changeServerStatus: server info not found for id={}", serverId);
                }
            }
        } catch (Exception e) {
            LOG.error("changeServersStatuses error", e);
            throw e;
        }
    }

    public boolean unlock(String lockManagerName, String lockId, long lockTime) {
        try {
            ILockManager lockManager = lockManagers.get(lockManagerName);
            return lockManager.unlock(lockId, lockTime);
        } catch (Exception e) {
            LOG.error("Cannot free lock, lockManagerName: {}, lockId: {}", lockManagerName, lockId, e);
            throw new KafkaHandlerException(-1, "Cannot free lock: " + e.getMessage());
        }
    }

    private void addLockManager(ILockManager lockManager) {
        lockManagers.put(lockManager.getClass().getCanonicalName(), lockManager);
    }

    public Set<GameServerInfoDto> getGameServersInfo() {
        LOG.debug("Getting servers info");
        Map<Integer, GameServerConfig> configMap = ServerConfigsCache.getInstance().getAllObjects();
        Set<GameServerInfoDto> result = new HashSet<>(configMap.size());
        for (GameServerConfig gameServerConfig : configMap.values()) {
            GameServerInfoDto gameServerInfo = new GameServerInfoDto();
            gameServerInfo.setId(gameServerConfig.getServerId());
            gameServerInfo.setHost(gameServerConfig.getTemplate().getHost());
            gameServerInfo.setDomain(gameServerConfig.getTemplate().getDomain());
            result.add(gameServerInfo);
        }
        LOG.debug("Ready to return servers info: {}", result);
        return result;
    }
}
