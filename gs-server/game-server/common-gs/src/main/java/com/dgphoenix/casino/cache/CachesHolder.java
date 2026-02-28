package com.abs.casino.cache;

import com.abs.casino.common.cache.AbstractExportableCache;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.*;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.abs.casino.gs.managers.dblink.DBLinkCache;
import com.abs.casino.gs.managers.payment.wallet.WalletPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * User: flsh
 * Date: 3/14/11
 */
public class CachesHolder {
    private static final Logger LOG = LogManager.getLogger(CachesHolder.class);

    private final LinkedList<AbstractExportableCache> exportableCaches = new LinkedList<>();
    private final LinkedList<IDistributedCache> caches = new LinkedList<>();
    private final Map<String, IDistributedCache> cacheMap = new HashMap<>();
    private final Map<String, AbstractCassandraPersister> configPersistersMap = new HashMap<>();

    public void init(CassandraPersistenceManager persistenceManager) {
        LOG.debug("Starting register caches...");
        register(ServerConfigsTemplateCache.getInstance(), persistenceManager.getPersister(CassandraServerConfigTemplatePersister.class));
        register(CurrencyCache.getInstance(), persistenceManager.getPersister(CassandraCurrencyPersister.class));
        register(BankInfoCache.getInstance(), persistenceManager.getPersister(CassandraBankInfoPersister.class));
        register(SubCasinoCache.getInstance(), persistenceManager.getPersister(CassandraSubCasinoPersister.class));
        register(SubCasinoGroupCache.getInstance(), persistenceManager.getPersister(CassandraSubCasinoGroupPersister.class));
        register(BaseGameCache.getInstance(), persistenceManager.getPersister(CassandraBaseGameInfoPersister.class));
        register(MassAwardCache.getInstance(), persistenceManager.getPersister(CassandraMassAwardPersister.class));
        register(persistenceManager.getPersister(CassandraGameSessionPersister.class));
        register(persistenceManager.getPersister(CassandraLasthandPersister.class));
        register(OperationStatisticsCache.getInstance());
        register(persistenceManager.getPersister(CassandraPaymentTransactionPersister.class));
        register(PeriodicReportsCache.getInstance());
        register(DomainWhiteListCache.getInstance(), persistenceManager.getPersister(CassandraDomainWhiteListPersister.class));
        register(BaseGameInfoTemplateCache.getInstance(), persistenceManager.getPersister(CassandraBaseGameInfoTemplatePersister.class));
        register(ExternalGameIdsCache.getInstance(), persistenceManager.getPersister(CassandraExternalGameIdsPersister.class));
        register(IdGenerator.getInstance());
        register(persistenceManager.getPersister(CassandraFrBonusPersister.class));
        register(persistenceManager.getPersister(CassandraFrbWinOperationPersister.class));
        register(persistenceManager.getPersister(CassandraBonusPersister.class));
        register(persistenceManager.getPersister(CassandraTransactionDataPersister.class));
        register(WalletPersister.getInstance());
        register(DBLinkCache.getInstance());
        LOG.debug("All caches register, total size: {}", caches.size());
    }

    public void register(IDistributedCache cache) {
        register(cache, null);
    }

    public void register(IDistributedCache cache, AbstractCassandraPersister configEntryPersister) {
        if (!caches.contains(cache)) {
            caches.addLast(cache);
            if (cache instanceof AbstractExportableCache) {
                exportableCaches.addLast((AbstractExportableCache) cache);
            }
            cacheMap.put(cache.getClass().getCanonicalName(), cache);
        }
        if (configEntryPersister != null) {
            configPersistersMap.put(cache.getClass().getCanonicalName(), configEntryPersister);
        }
    }

    public Map<String, AbstractCassandraPersister> getConfigPersistersMap() {
        return configPersistersMap;
    }

    public LinkedList<IDistributedCache> getCaches() {
        return caches;
    }

    public LinkedList<AbstractExportableCache> getExportableCaches() {
        return exportableCaches;
    }

    public Map<String, IDistributedCache> getCacheMap() {
        return cacheMap;
    }
}