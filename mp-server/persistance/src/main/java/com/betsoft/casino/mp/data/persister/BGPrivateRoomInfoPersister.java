package com.betsoft.casino.mp.data.persister;

import com.betsoft.casino.mp.model.BGPrivateRoomInfo;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class BGPrivateRoomInfoPersister extends AbstractRoomInfoPersister<BGPrivateRoomInfo> {
    private static final Logger LOG = LogManager.getLogger(BGPrivateRoomInfoPersister.class);
    private static final String CF_NAME = "BGPrivateRoomInfo";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(ROOM_ID_COLUMN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
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
