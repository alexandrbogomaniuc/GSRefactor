package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.SingleNodeRoomInfo;
import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * User: flsh
 * Date: 28.09.18.
 */
public class SingleNodeRoomInfoPersister extends AbstractRoomInfoPersister<SingleNodeRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(SingleNodeRoomInfoPersister.class);
    private static final String CF_NAME = "SNRoomInfo";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ROOM_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), ROOM_ID_COLUMN);

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
}
