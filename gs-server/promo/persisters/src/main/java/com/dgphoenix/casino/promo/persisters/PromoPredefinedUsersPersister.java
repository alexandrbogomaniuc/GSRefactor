package com.dgphoenix.casino.promo.persisters;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.*;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PromoPredefinedUsersPersister extends AbstractCassandraPersister<Long, Long> {
    private static final Logger LOG = LogManager.getLogger(PromoPredefinedUsersPersister.class);
    private static final String BG_CONFIG_CF = "PromoPredefinedUsersPersisterCF";
    private static final String BANK_ID = "bankId";
    private static final String PROMO_ID = "promoId";
    private static final String ACCOUNT_ID = "accountId";
    private static final String EXT_USER_ID = "extUserId";
    private static final int BATCH_LIMIT = 100;

    private static final TableDefinition PROMO_PREDEFINED_TABLE = new TableDefinition(BG_CONFIG_CF,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(PROMO_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(EXT_USER_ID, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID, com.datastax.driver.core.DataType.bigint(), false, true, false)
            ), BANK_ID)
            .compaction(CompactionStrategy.LEVELED);

    @Override
    public TableDefinition getMainTableDefinition() {
        return PROMO_PREDEFINED_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void persist(long promoId, long bankId, String extUserId, Long account) {
        Insert queryInsert = createQueryInsert(promoId, bankId, extUserId, account);
        execute(queryInsert, "persist");
    }

    public void persist(long promoId, long bankId, Set<Pair<String, Long>> accounts) {
        Batch batch = batch();
        int counter = 0;
        for (Pair<String, Long> pairAccount : accounts) {
            batch.add(createQueryInsert(promoId, bankId, pairAccount.getKey(), pairAccount.getValue()));
            if (++counter > BATCH_LIMIT) {
                execute(batch, "persist collection");
                counter = 0;
                batch = batch();
            }
        }
        if (counter > 0) {
            execute(batch, "persist collection");
        }
    }

    public boolean isExist(long promoId, long bankId, long accountId) {
        Select select = getSelectByClause(promoId, bankId, eq(ACCOUNT_ID, accountId));
        com.datastax.driver.core.ResultSet resultSet = execute(select, "isExists [by accountId]");
        return resultSet.one() != null;
    }

    public boolean isExist(long promoId, long bankId, String extUserId) {
        Select select = getSelectByClause(promoId, bankId, eq(EXT_USER_ID, extUserId));
        com.datastax.driver.core.ResultSet resultSet = execute(select, "isExists [by extUserId]");
        return resultSet.one() != null;
    }

    private Select getSelectByClause(long promoId, long bankId, Clause clause) {
        return getSelectAllColumnsQuery(PROMO_PREDEFINED_TABLE)
                .where(eq(PROMO_ID, promoId))
                .and(eq(BANK_ID, bankId))
                .and(clause).limit(1);
    }

    public void remove(long promoId, long bankId, String extUserId) {
        getLog().debug("remove: promoId={}, bankId={}, extUserId={}", promoId, bankId, extUserId);
        Delete query = QueryBuilder.delete().from(getMainColumnFamilyName());
        query.where(eq(BANK_ID, bankId)).and(eq(PROMO_ID, promoId)).and(eq(EXT_USER_ID, extUserId));
        execute(query, "remove");
    }

    public void removeAll(long promoId, long bankId) {
        getLog().debug("remove: promoId={}, bankId={}", promoId, bankId);
        Delete query = QueryBuilder.delete().from(getMainColumnFamilyName());
        query.where(eq(BANK_ID, bankId)).and(eq(PROMO_ID, promoId));
        execute(query, "removeAll");
    }

    public Set<Pair<String, Long>> getByPromoId(long promoId, long bankId) {
        Select select = getSelectColumnsQuery(EXT_USER_ID, ACCOUNT_ID);
        select.where(eq(PROMO_ID, promoId)).and(eq(BANK_ID, bankId));
        com.datastax.driver.core.ResultSet resultSet = execute(select, "getByPromoId");
        Set<Pair<String, Long>> pairs = new HashSet<>();
        for (com.datastax.driver.core.Row row : resultSet.all()) {
            pairs.add(new Pair<>(row.getString(EXT_USER_ID), row.getLong(ACCOUNT_ID)));
        }
        return pairs;
    }

    private Insert createQueryInsert(long promoId, long bankId, String extUserId, Long account) {
        Insert query = getInsertQuery(PROMO_PREDEFINED_TABLE, getTtl());
        query.value(PROMO_ID, promoId);
        query.value(BANK_ID, bankId);
        query.value(EXT_USER_ID, extUserId);
        query.value(ACCOUNT_ID, account);
        return query;
    }
}
