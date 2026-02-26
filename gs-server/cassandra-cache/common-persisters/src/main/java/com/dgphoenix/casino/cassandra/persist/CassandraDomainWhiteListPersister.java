package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.common.cache.DomainWhiteListCache;
import com.abs.casino.common.cache.data.domain.DomainWhiteList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * User: flsh
 * Date: 4/11/12
 */
public class CassandraDomainWhiteListPersister extends AbstractLongDistributedConfigEntryPersister<DomainWhiteList> {
    public static final String DOMAIN_WHITE_LIST_CF = "DWlCF";
    private static final Logger LOG = LogManager.getLogger(CassandraDomainWhiteListPersister.class);

    private CassandraDomainWhiteListPersister() {
        super();
    }

    @Override
    public DomainWhiteListCache getCache() {
        return DomainWhiteListCache.getInstance();
    }

    @Override
    public int loadAll() {
        final Map<Long, DomainWhiteList> infos = loadAllAsMap(DomainWhiteList.class);
        if (infos == null) {
            return 0;
        }
        int count = 0;
        for (DomainWhiteList info : infos.values()) {
            put(info);
            count++;
        }
        LOG.info("loadAll: count=" + count + ", DomainWhiteListCache.getInstance().size()=" +
                DomainWhiteListCache.getInstance().size());
        return DomainWhiteListCache.getInstance().size();
    }

    @Override
    public String getMainColumnFamilyName() {
        return DOMAIN_WHITE_LIST_CF;
    }

    public DomainWhiteList get(String id) {
        return get(id, DomainWhiteList.class);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
