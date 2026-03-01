package com.abs.casino.promo.persisters;
import com.abs.casino.common.promo.*;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.promo.ISupportedPlatform;
import com.dgphoenix.casino.common.promo.SupportedPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CassandraSupportedPromoPlatformsPersister extends AbstractCassandraPersister<Long, Object> {

    private static final Logger LOG = LogManager.getLogger(CassandraSupportedPromoPlatformsPersister.class);

    private static final String PROMO_ID = "promoId";
    private static final String PLATFORM = "platform";
    private static final String PROMO_PLATFORMS_CF = "PromoPlatformsCf";
    private static final TableDefinition TABLE = new TableDefinition(PROMO_PLATFORMS_CF,
            Arrays.asList(
                    new ColumnDefinition(PROMO_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(PLATFORM, com.datastax.driver.core.DataType.blob(), false, false, false),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text(), false, false, false)
            ), PROMO_ID);

    public void persist(long campaignId, ISupportedPlatform supportedPlatform) {
        ByteBuffer supportedPlatformAsBytes = getMainTableDefinition().serializeWithClassToBytes(supportedPlatform);
        String json = getMainTableDefinition().serializeWithClassToJson(supportedPlatform);
        try {
            com.datastax.driver.core.Statement insert = getInsertQuery()
                    .value(PROMO_ID, campaignId)
                    .value(PLATFORM, supportedPlatformAsBytes)
                    .value(JSON_COLUMN_NAME, json);
            execute(insert, "persist supported platform");
        } finally {
            releaseBuffer(supportedPlatformAsBytes);
        }
    }

    public ISupportedPlatform getSupportedPlatform(long campaignId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(PLATFORM)
                .where(eq(PROMO_ID, campaignId));
        com.datastax.driver.core.Row result = execute(query, "getSupportedPlatform").one();

        ISupportedPlatform supportedPlatform = SupportedPlatform.ALL;
        if (result != null) {
            String json = result.getString(JSON_COLUMN_NAME);
            supportedPlatform = getMainTableDefinition().deserializeWithClassFromJson(json);
            if (supportedPlatform == null) {
                ByteBuffer platformAsBytes = result.getBytes(PLATFORM);
                supportedPlatform = getMainTableDefinition().deserializeWithClassFrom(platformAsBytes);
            }
        }

        return supportedPlatform;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
