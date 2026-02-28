package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * User: flsh
 * Date: 4/11/12
 */
public class CassandraSubCasinoPersister extends AbstractLongDistributedConfigEntryPersister<SubCasino> {
    public static final String SUB_CASINO_CF = "SubCasinoCF";
    private static final Logger LOG = LogManager.getLogger(CassandraSubCasinoPersister.class);

    private CassandraSubCasinoPersister() {
        super();
    }


    public void refresh(String id) {
        final SubCasino cassandraEntity = get(id);
        if (cassandraEntity == null) {
            //remove from cache
            SubCasinoCache.getInstance().remove(Long.parseLong(id));
        } else {
            put(cassandraEntity);
        }
    }


    public SubCasinoCache getCache() {
        return SubCasinoCache.getInstance();
    }

    @Override
    public int loadAll() {
        Map<Long, SubCasino> map = loadAllAsMap(SubCasino.class);
        if (map == null) {
            return 0;
        }
        for (Map.Entry<Long, SubCasino> entry : map.entrySet()) {
            SubCasino subCasino = entry.getValue();
            put(subCasino);
        }
        getLog().info("loadAll: SubCasinoCache.getInstance().size()=" + SubCasinoCache.getInstance().size());
        return SubCasinoCache.getInstance().size();
    }

    @Override
    public String getMainColumnFamilyName() {
        return SUB_CASINO_CF;
    }

    @Override
    public SubCasino get(String id) {
        return super.get(id, SubCasino.class);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
