package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.AbstractDistributedCache;
import com.dgphoenix.casino.common.cache.MassAwardCache;
import com.dgphoenix.casino.common.cache.data.bonus.BaseMassAward;
import com.dgphoenix.casino.common.exception.BonusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 18.12.13
 */
public class CassandraMassAwardPersister extends AbstractLongDistributedConfigEntryPersister<BaseMassAward> {
    private static final String MASS_AWARD_CF = "MassAwardCF";
    private static final String DELAYED_MASS_AWARD_CF = "DelayedMassAwardIndexCF";
    private static final String MASS_AWARD_ID = "MassAwardId";
    private static final String DELAYED_MASS_AWARD_ID = "DelayedMassAwardId";
    private static final Logger LOG = LogManager.getLogger(CassandraMassAwardPersister.class);

    private static final TableDefinition DELAYED_MASS_AWARD_TABLE = new TableDefinition(DELAYED_MASS_AWARD_CF,
            Arrays.asList(
                    new ColumnDefinition(DELAYED_MASS_AWARD_ID, DataType.bigint(), false, false, true),
                    new ColumnDefinition(MASS_AWARD_ID, DataType.bigint(), false, true, false)
            ), DELAYED_MASS_AWARD_ID);

    private CassandraMassAwardPersister() {
        super();
    }

    @Override
    public int loadAll() {
        Map<Long, BaseMassAward> map = loadAllAsMap(BaseMassAward.class);
        if (map == null) {
            LOG.error("loadAll is null");
            return 0;
        }
        int count = 0;
        for (Map.Entry<Long, BaseMassAward> entry : map.entrySet()) {
            try {
                MassAwardCache.getInstance().put(entry.getValue());
                count++;
            } catch (BonusException e) {
                LOG.error("Cannot load MassAward: " + entry.getValue(), e);
            }
        }
        return count;
    }

    public void save(BaseMassAward award) {
        put(award);
    }

    @Override
    public void saveAll() {
        // nop, not required
    }

    public void delete(long massAwardId) {
        LOG.info("delete: " + massAwardId);
        deleteItem(massAwardId);
        deleteDelayedMassAwardId(massAwardId);
    }

    @Override
    public BaseMassAward get(String id) {
        return get(id, BaseMassAward.class);
    }

    public BaseMassAward get(long id) {
        return get(String.valueOf(id), BaseMassAward.class);
    }

    @Override
    public AbstractDistributedCache getCache() {
        return MassAwardCache.getInstance();
    }

    @Override
    public String getMainColumnFamilyName() {
        return MASS_AWARD_CF;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(getMainTableDefinition(), DELAYED_MASS_AWARD_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public Long getMassAwardIdByDelayedMassAwardId(long delayedMassAwardId) {
        Statement select = QueryBuilder.select().column(MASS_AWARD_ID)
                .from(DELAYED_MASS_AWARD_CF)
                .where(eq(DELAYED_MASS_AWARD_ID, delayedMassAwardId));
        ResultSet result = execute(select, "getMassAwardIdByDelayedMassAwardId");
        Row row = result.one();
        if (row != null) {
            return row.getLong(MASS_AWARD_ID);
        }
        return null;
    }

    public void saveDelayedMassAwardId(long delayedMassAwardId, long massAwardId) {
        Statement query = QueryBuilder.insertInto(DELAYED_MASS_AWARD_CF)
                .value(DELAYED_MASS_AWARD_ID, delayedMassAwardId)
                .value(MASS_AWARD_ID, massAwardId);
        execute(query, "persist delayedMassAwardId");
    }

    private void deleteDelayedMassAwardId(long massAwardId) {
        Statement select = QueryBuilder.select().column(DELAYED_MASS_AWARD_ID)
                .from(DELAYED_MASS_AWARD_CF)
                .where(eq(MASS_AWARD_ID, massAwardId));
        ResultSet result = execute(select, "getDelayedMassAwardId");
        Row row = result.one();
        if (row != null) {
            long delayedMassAwardId = row.getLong(DELAYED_MASS_AWARD_ID);
            Statement delete = QueryBuilder.delete().from(DELAYED_MASS_AWARD_CF).where(eq(DELAYED_MASS_AWARD_ID, delayedMassAwardId));
            execute(delete, "delete delayedMassAwardId");
        }
    }
}
