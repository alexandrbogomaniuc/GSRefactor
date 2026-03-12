package com.abs.casino.init;

import com.abs.casino.account.AccountManager;
import com.abs.casino.cache.CachesHolder;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.IConfigsInitializer;
import com.abs.casino.cassandra.persist.*;
import com.abs.casino.common.cache.*;
import com.abs.casino.common.configuration.CasinoSystemType;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.IdGenerator;
import com.abs.casino.common.util.IntegerIdGenerator;
import com.abs.casino.gs.maintenance.CacheExporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultConfigsInitializer implements IConfigsInitializer {

    private static final Logger LOG = LogManager.getLogger(DefaultConfigsInitializer.class);

    private final CachesHolder cachesHolder;

    public DefaultConfigsInitializer(CachesHolder cachesHolder) {
        this.cachesHolder = cachesHolder;
    }

    @Override
    public void initialize(CassandraPersistenceManager persistenceManager) {
        cachesHolder.init(persistenceManager);
        IdGenerator.getInstance().init(persistenceManager.getPersister(CassandraSequencerPersister.class));
        IntegerIdGenerator.getInstance().init(persistenceManager.getPersister(CassandraIntSequencerPersister.class));

        CassandraBaseGameInfoPersister baseGameInfoPersister = persistenceManager
                .getPersister(CassandraBaseGameInfoPersister.class);
        BaseGameCache.getInstance().initCache(baseGameInfoPersister, baseGameInfoPersister, 300000);
        ExternalGameIdsCache.getInstance().init(persistenceManager
                .getPersister(CassandraExternalGameIdsPersister.class), 10000);
        BaseGameInfoTemplateCache.getInstance().init();
        CurrencyCache.getInstance().initCache(persistenceManager.getPersister(CassandraCurrencyPersister.class), 1000);

        int loadedCount = loadConfigs(persistenceManager);
        LOG.info("Loaded configs count={}", loadedCount);
        String configImportPath = null;
        boolean importLoadBalancerConfig = false;
        if (loadedCount == 0) {
            LOG.info("Import configs mode: first run");
            configImportPath = System.getProperty("CONFIG_IMPORT_PATH");
            if (configImportPath == null) {
                configImportPath = "/www/html/gs/ROOT/export/";
            }
            try {
                CacheExporter.getInstance().importAll(configImportPath, cachesHolder);
                importLoadBalancerConfig = true;
            } catch (CommonException e) {
                LOG.error("Import error", e);
                throw new RuntimeException(e);
            }
            CurrencyCache.getInstance().initBaseCurrencies();
            saveConfigs(persistenceManager);
            BaseGameCache.getInstance().invalidateAll();
            if (ApplicationContextHelper.getApplicationContext() != null) {
                ApplicationContextHelper.getBean(AccountManager.class).setCasinoSystemType(CasinoSystemType.MULTIBANK);
            } else {
                LOG.warn("Skip AccountManager casino system type update: applicationContext is not initialized yet");
            }

            LOG.info("Import configs completed, recommend restart server");
        }
        if (ApplicationContextHelper.getApplicationContext() != null) {
            LoadBalancerCache loadBalancerCache = ApplicationContextHelper.getBean(LoadBalancerCache.class);
            if (importLoadBalancerConfig) {
                try {
                    CacheExporter.getInstance().importCache(loadBalancerCache, configImportPath);
                } catch (CommonException e) {
                    LOG.error("Import error", e);
                    throw new RuntimeException(e);
                }
            }
            cachesHolder.register(loadBalancerCache);
            cachesHolder.register(ApplicationContextHelper.getBean(AccountManager.class));
        } else {
            LOG.warn("Skip spring cache registration in DefaultConfigsInitializer: applicationContext is not initialized yet");
        }
    }

    private int loadConfigs(CassandraPersistenceManager manager) {
        int loadedCount;
        loadedCount = manager.getPersister(CassandraServerConfigTemplatePersister.class).loadAll();
        loadedCount += manager.getPersister(CassandraSubCasinoPersister.class).loadAll();
        loadedCount += manager.getPersister(CassandraSubCasinoGroupPersister.class).loadAll();
        loadedCount += manager.getPersister(CassandraDomainWhiteListPersister.class).loadAll();
        loadedCount += manager.getPersister(CassandraCurrencyPersister.class).loadAll();
        loadedCount += manager.getPersister(CassandraBankInfoPersister.class).loadAll();
        loadedCount += manager.getPersister(CassandraBaseGameInfoTemplatePersister.class).loadAll();
        loadedCount += manager.getPersister(CassandraMassAwardPersister.class).loadAll();
        loadedCount += manager.getPersister(CassandraExternalGameIdsPersister.class).loadAll();
        return loadedCount;
    }

    private void saveConfigs(CassandraPersistenceManager manager) {
        manager.getPersister(CassandraServerConfigTemplatePersister.class).saveAll();
        manager.getPersister(CassandraSubCasinoPersister.class).saveAll();
        manager.getPersister(CassandraSubCasinoGroupPersister.class).saveAll();
        manager.getPersister(CassandraDomainWhiteListPersister.class).saveAll();
        manager.getPersister(CassandraCurrencyPersister.class).saveAll();
        manager.getPersister(CassandraBankInfoPersister.class).saveAll();
        manager.getPersister(CassandraBaseGameInfoTemplatePersister.class).saveAll();
        manager.getPersister(CassandraBaseGameInfoPersister.class).saveAll();
        manager.getPersister(CassandraMassAwardPersister.class).saveAll();
    }
}
