package com.abs.casino.cassandra.persist.mp;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.promo.ai.IMQReservedNicknamePersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 13.02.2020.
 */
public class MQReservedNicknamePersister extends AbstractCassandraPersister<String, String> implements IMQReservedNicknamePersister {
    private static final Logger LOG = LogManager.getLogger(MQReservedNicknamePersister.class);
    private static final String AI_BOT_REGION = "AI_BOT_REGION";
    private static final Long ENTIRE_SYSTEM_ID = -1L;
    private static final String CF_NAME = "MqReservedNicknames";
    private static final String REGION_COLUMN = "rnRegion";
    private static final String NICK_NAME_COLUMN = "rnNick";
    private static final String OWNER_COLUMN = "rnOwner";

    //owner: -1 for entire system, else - bankId
    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(REGION_COLUMN, text(), false, false, true),
                    new ColumnDefinition(NICK_NAME_COLUMN, text(), false, false, true),
                    new ColumnDefinition(OWNER_COLUMN, bigint(), false, true, false)
            ), REGION_COLUMN);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void remove(String region, String nickname) {
        deleteItem(eq(REGION_COLUMN, region), eq(NICK_NAME_COLUMN, nickname));
    }

    public void persistForEntireSystem(String region, String nickname) {
        persist(region, nickname, ENTIRE_SYSTEM_ID);
    }

    public void persist(String region, String nickname, long owner) {
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(REGION_COLUMN, region)
                .value(NICK_NAME_COLUMN, nickname)
                .value(OWNER_COLUMN, owner);
        execute(query, "persist");
    }

    public boolean isExistForEntireSystem(String region, String nickname) {
        return isExist(region, nickname, ENTIRE_SYSTEM_ID);
    }

    public boolean isExist(String region, String nickname, long owner) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(OWNER_COLUMN)
                .where(eq(REGION_COLUMN, region))
                .and(eq(NICK_NAME_COLUMN, nickname));
        com.datastax.driver.core.Row result = execute(query, "isExist").one();
        return result != null && result.getLong(OWNER_COLUMN) == owner;
    }

    public Set<String> getNicknamesForEntireSystem(String region) {
        return getNicknames(region, ENTIRE_SYSTEM_ID);
    }

    public Set<String> getAIBotNames() {
        return getNicknamesForEntireSystem(AI_BOT_REGION);
    }

    public Set<String> getNicknames(String region, Long owner) {
        com.datastax.driver.core.Statement query;
        if (owner == null) {
            query = getSelectColumnsQuery(NICK_NAME_COLUMN)
                    .where(eq(REGION_COLUMN, region));
        } else {
            query = getSelectColumnsQuery(NICK_NAME_COLUMN)
                    .where(eq(REGION_COLUMN, region))
                    .and(eq(OWNER_COLUMN, owner))
                    .allowFiltering();
        }
        com.datastax.driver.core.ResultSet rs = execute(query, "getNickNamesForRegion");
        Set<String> result = new HashSet<>(128);
        for (com.datastax.driver.core.Row row : rs) {
            result.add(row.getString(NICK_NAME_COLUMN));
        }
        return result;
    }
}
