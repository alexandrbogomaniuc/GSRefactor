package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.PlayerProfile;
import com.betsoft.casino.mp.service.IPlayerProfileService;
import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 12.07.18.
 */
public class PlayerProfilePersister extends AbstractCassandraPersister<Long, String>
        implements IPlayerProfileService<PlayerProfile> {
    private static final Logger LOG = LogManager.getLogger(PlayerProfilePersister.class);

    private static final String CF_NAME = "PlayerProfile";
    private static final String BANK_ID_COLUMN = "bid";
    private static final String ACCOUNT_ID_COLUMN = "aid";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_COLUMN, bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ), BANK_ID_COLUMN, ACCOUNT_ID_COLUMN);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public String getMainColumnFamilyName() {
        return CF_NAME;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public PlayerProfile load(long bankId, long accountId) {
        com.datastax.driver.core.Row result = execute(getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                        .where()
                        .and(eq(BANK_ID_COLUMN, bankId))
                        .and(eq(ACCOUNT_ID_COLUMN, accountId))
                        .limit(1),
                "load").one();
        if (result == null) {
            return null;
        }
        PlayerProfile profile = 
                TABLE.deserializeFromJson(result.getString(JSON_COLUMN_NAME), PlayerProfile.class);
        if (profile == null) {
            profile = TABLE.deserializeFrom(result.getBytes(SERIALIZED_COLUMN_NAME), PlayerProfile.class);
        }
        return profile;
    }

    @Override
    public void save(long bankId, long accountId, PlayerProfile profile) {
        ByteBuffer buffer = TABLE.serializeToBytes(profile);
        String json = TABLE.serializeToJson(profile);
        try {
            execute(getInsertQuery()
                            .value(BANK_ID_COLUMN, bankId)
                            .value(ACCOUNT_ID_COLUMN, accountId)
                            .value(SERIALIZED_COLUMN_NAME, buffer)
                            .value(JSON_COLUMN_NAME, json),
                    "save");
        } finally {
            releaseBuffer(buffer);
        }
    }
}
