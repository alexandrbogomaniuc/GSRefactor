package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.geoip.CountryRestrictionList;
import com.dgphoenix.casino.common.geoip.RestrictionType;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CassandraCountryRestrictionPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraCountryRestrictionPersister.class);
    private static final String COLUMN_FAMILY_NAME = "CountryRestrictionsCF";
    private static final String OBJECT_ID = "ObjectId";
    private static final String RESTRICTION_TYPE = "RestrictionType";

    private static final TableDefinition COUNTRIES_TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
        Arrays.asList(
            new ColumnDefinition(OBJECT_ID, DataType.bigint(), false, false, true),
            new ColumnDefinition(RESTRICTION_TYPE, DataType.cint(), false, false, true),
            new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob(), false, false, false),
            new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
        ), OBJECT_ID)
        .compaction(CompactionStrategy.LEVELED);

    private CassandraCountryRestrictionPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return COUNTRIES_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void persist(long objectId, RestrictionType type, CountryRestrictionList restrictions) {
        ByteBuffer byteBuffer = COUNTRIES_TABLE.serializeToBytes(restrictions);
        String json = COUNTRIES_TABLE.serializeToJson(restrictions);
        try {
            Insert query = getInsertQuery(type.getCassandraTtl());
            query.value(OBJECT_ID, objectId)
                .value(RESTRICTION_TYPE, type.ordinal())
                .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                .value(JSON_COLUMN_NAME, json);
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public CountryRestrictionList get(long objectId, RestrictionType type) {
        long now = System.currentTimeMillis();
        CountryRestrictionList result = null;
        Select query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        query.where().and(eq(OBJECT_ID, objectId)).and(eq(RESTRICTION_TYPE, type.ordinal()));
        ResultSet resultSet = execute(query, "get");
        Row row = resultSet.one();
        if (row != null) {
            String json = row.getString(JSON_COLUMN_NAME);
            result = COUNTRIES_TABLE.deserializeFromJson(json, CountryRestrictionList.class);

            if (result == null) {
                ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
                result = COUNTRIES_TABLE.deserializeFrom(bytes, CountryRestrictionList.class);
            }

        }
        StatisticsManager.getInstance().updateRequestStatistics(getMainColumnFamilyName() + "get",
            System.currentTimeMillis() - now);
        return result;
    }

    public void delete(long objectId, RestrictionType type) {
        Delete query = QueryBuilder.delete().from(getMainColumnFamilyName());
        query.where().and(eq(OBJECT_ID, objectId)).and(eq(RESTRICTION_TYPE, type));
        execute(query, "delete");
    }
}
