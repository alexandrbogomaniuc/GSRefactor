package com.betsoft.casino.mp.data.persister;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: flsh
 * Date: 11.02.2020.
 */
public class ReservedNicknamePersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(ReservedNicknamePersister.class);
    private static final Long ENTIRE_SYSTEM_ID = -1L;
    private static final String CF_NAME = "ReservedNicknames";
    private static final String REGION_COLUMN = "rnRegion";
    private static final String NICK_NAME_COLUMN = "rnNick";
    private static final String OWNER_COLUMN = "rnOwner";

    //owner: -1 for entire system, else - bankId
    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(REGION_COLUMN, DataType.text(), false, false, true),
                    new ColumnDefinition(NICK_NAME_COLUMN, DataType.text(), false, false, true),
                    new ColumnDefinition(OWNER_COLUMN, DataType.bigint(), false, true, false)
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
        execute(getInsertQuery()
                        .value(REGION_COLUMN, region)
                        .value(NICK_NAME_COLUMN, nickname)
                        .value(OWNER_COLUMN, owner),
                "persist");
    }

    public boolean isExistForEntireSystem(String region, String nickname) {
        return isExist(region, nickname, ENTIRE_SYSTEM_ID);
    }

    public boolean isExist(String region, String nickname, long owner) {
        Row result = execute(getSelectColumnsQuery(OWNER_COLUMN)
                        .where()
                        .and(eq(REGION_COLUMN, region))
                        .and(eq(NICK_NAME_COLUMN, nickname)),
                "isExist").one();
        return result != null && result.getLong(OWNER_COLUMN) == owner;
    }

    public Set<String> getNicknamesForEntireSystem(String region) {
        return getNicknames(region, ENTIRE_SYSTEM_ID);
    }

    public Set<String> getNicknames(String region, Long owner) {
        com.datastax.driver.core.querybuilder.Select.Where where = getSelectColumnsQuery(NICK_NAME_COLUMN)
                .where()
                .and(eq(REGION_COLUMN, region));
        if(owner != null) {
            where.and(eq(OWNER_COLUMN, owner)).allowFiltering();
        }
        com.datastax.driver.core.ResultSet rs = execute(where, "getNickNamesForRegion");
        Set<String> result = new HashSet<>(128);
        for (Row row : rs) {
            result.add(row.getString(NICK_NAME_COLUMN));
        }
        return result;
    }
}
