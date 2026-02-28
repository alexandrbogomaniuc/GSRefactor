package com.abs.casino.cassandra.persist;

import com.abs.casino.common.cache.AbstractDistributedCache;
import com.dgphoenix.casino.common.cache.ServerConfigsCache;
import com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache;
import com.dgphoenix.casino.common.config.GameServerConfigTemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * User: Grien
 * Date: 02.09.2014 11:36
 */
public class CassandraServerConfigTemplatePersister extends AbstractIntegerDistributedConfigEntryPersister<GameServerConfigTemplate> {
    public static final String CF = "ServerConfCF";
    private static final Logger LOG = LogManager.getLogger(CassandraServerConfigTemplatePersister.class);

    private CassandraServerConfigTemplatePersister() {
        super();
    }

    @Override
    public int loadAll() {
        final Map<Integer, GameServerConfigTemplate> infos = loadAllAsMap(GameServerConfigTemplate.class);
        if (infos == null) {
            LOG.error("loadAllForLongKeys return null");
            return 0;
        }
        for (GameServerConfigTemplate config : infos.values()) {
            put(config);
        }
        LOG.info("loadAll: count=" + infos.size());
        return infos.size();
    }

    public void save(GameServerConfigTemplate config) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Save: " + config);
        }
        persist((int) config.getId(), config);
    }

    @Override
    public GameServerConfigTemplate get(String id) {
        return get(GameServerConfigTemplate.TEMPLATE_ID, GameServerConfigTemplate.class);
    }

    @Override
    public AbstractDistributedCache getCache() {
        return ServerConfigsTemplateCache.getInstance();
    }

    @Override
    public String getMainColumnFamilyName() {
        return CF;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}