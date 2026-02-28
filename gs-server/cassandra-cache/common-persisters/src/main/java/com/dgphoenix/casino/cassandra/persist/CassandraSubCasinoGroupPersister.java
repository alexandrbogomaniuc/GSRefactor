package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.SubCasinoGroupCache;
import com.abs.casino.common.cache.data.bank.SubCasinoGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * User: flsh
 * Date: 4/11/12
 */
public class CassandraSubCasinoGroupPersister extends AbstractStringDistributedConfigEntryPersister<SubCasinoGroup> {
    public static final String SUB_CASINO_GROPUP_CF = "SubCasinoGroupF";
    private static final Logger LOG = LogManager.getLogger(CassandraSubCasinoGroupPersister.class);

    private CassandraSubCasinoGroupPersister() {
        super();
    }


    public void refresh(String id) {
        final SubCasinoGroup cassandraEntity = get(id);
        if (cassandraEntity == null) {
            //remove from cache
            SubCasinoGroupCache.getInstance().remove(id);
        } else {
            put(cassandraEntity);
        }
    }


    public SubCasinoGroupCache getCache() {
        return SubCasinoGroupCache.getInstance();
    }

    @Override
    public int loadAll() {
        Map<String, SubCasinoGroup> map = loadAllAsMap(SubCasinoGroup.class);
        if (map == null) {
            return 0;
        }
        for (Map.Entry<String, SubCasinoGroup> entry : map.entrySet()) {
            SubCasinoGroup subCasino = entry.getValue();
            put(subCasino);
        }
        getLog().info("loadAll: SubCasinoGroupCache.getInstance().size()=" + SubCasinoGroupCache.getInstance().size());
        return SubCasinoCache.getInstance().size();
    }

    @Override
    public String getMainColumnFamilyName() {
        return SUB_CASINO_GROPUP_CF;
    }

    @Override
    public SubCasinoGroup get(String id) {
        return super.get(id, SubCasinoGroup.class);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
