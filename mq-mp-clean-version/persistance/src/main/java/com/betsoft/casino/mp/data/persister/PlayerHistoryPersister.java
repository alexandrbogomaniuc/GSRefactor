package com.betsoft.casino.mp.data.persister;

import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class PlayerHistoryPersister {

    private static final Logger LOG = LogManager.getLogger(PlayerHistoryPersister.class);

    private static final String CF_NAME = "PlayersHistory";
    private static final String BANK_ID_COLUMN = "bid";
    private static final String GAME_ID_COLUMN = "gid";
    private static final String ACCOUNT_ID_COLUMN = "aid";
    private static final String PLAYER_ROUND_ID_COLUMN = "playerRoundId";
    private static final String ROOM_ROUND_ID_COLUMN = "roomRoundId";
    private static final String VERSION_COLUMN = "v";
    private static final String SERIALIZED_COLUMN_NAME = "scn";
    private static final String JSON_COLUMN_NAME = "jcn";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(PLAYER_ROUND_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(ROOM_ROUND_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(VERSION_COLUMN, DataType.bigint()),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), BANK_ID_COLUMN, GAME_ID_COLUMN, ACCOUNT_ID_COLUMN, PLAYER_ROUND_ID_COLUMN, ROOM_ROUND_ID_COLUMN)
            .compaction(CompactionStrategy.LEVELED);



}
