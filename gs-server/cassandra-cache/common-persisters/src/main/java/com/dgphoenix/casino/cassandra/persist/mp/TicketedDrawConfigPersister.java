package com.dgphoenix.casino.cassandra.persist.mp;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.*;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.mp.LeaderboardStatus;
import com.dgphoenix.casino.common.mp.TicketedDrawConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dgphoenix.casino.common.mp.LeaderboardStatus.SCHEDULED;

public class TicketedDrawConfigPersister extends AbstractCassandraPersister<String, Long> {
    private static final Logger LOG = LogManager.getLogger(TicketedDrawConfigPersister.class);

    private static final String CF_NAME = "TDrawConfig";
    private static final String DRAW_ID_COLUMN = "id";
    private static final String START_DATE = "sd";
    private static final String END_DATE = "ed";
    private static final String CONFIG_COLUMN = "cc";
    private static final String STATUS_COLUMN = "ss";
    private static final String UPDATE_DATE = "ud";

    private static final TableDefinition CONFIG_TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(DRAW_ID_COLUMN, DataType.bigint(), false, false, true),
                    new ColumnDefinition(STATUS_COLUMN, DataType.cint(), false, true, false),
                    new ColumnDefinition(START_DATE, DataType.bigint()),
                    new ColumnDefinition(END_DATE, DataType.bigint()),
                    new ColumnDefinition(CONFIG_COLUMN, DataType.blob()),
                    new ColumnDefinition(UPDATE_DATE, DataType.bigint()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text())
            ), DRAW_ID_COLUMN);

    public void addConfig(TicketedDrawConfig config) {
        ByteBuffer configBuffer = CONFIG_TABLE.serializeWithClassToBytes(config);
        String json = CONFIG_TABLE.serializeWithClassToJson(config);
        try {
            Insert insert = getInsertQuery(CONFIG_TABLE, null)
                    .value(DRAW_ID_COLUMN, config.getId())
                    .value(START_DATE, config.getStartDate())
                    .value(END_DATE, config.getEndDate())
                    .value(CONFIG_COLUMN, configBuffer)
                    .value(STATUS_COLUMN, SCHEDULED.getCode())
                    .value(UPDATE_DATE, 0)
                    .value(JSON_COLUMN_NAME, json);

            execute(insert, "addConfig");
        } finally {
            releaseBuffer(configBuffer);
        }
    }

    public TicketedDrawConfig getConfig(long id) {
        Select query = getSelectColumnsQuery(CONFIG_TABLE, CONFIG_COLUMN, JSON_COLUMN_NAME)
                .where(eq(DRAW_ID_COLUMN, id))
                .limit(1);

        com.datastax.driver.core.ResultSet result = execute(query, "getConfig");
        if (result != null) {
            TicketedDrawConfig tdc = CONFIG_TABLE.deserializeWithClassFromJson(result.one().getString(JSON_COLUMN_NAME));
            if (tdc == null) {
                tdc = CONFIG_TABLE.deserializeWithClassFrom(result.one().getBytes(CONFIG_COLUMN));
            }
            return tdc;
        }
        return null;
    }

    public void removeConfig(long id) {
        Delete.Where delete = QueryBuilder.delete().from(CF_NAME).where(eq(DRAW_ID_COLUMN, id));
        execute(delete, "removeConfig");
    }

    public void updateStatus(long id, LeaderboardStatus status) {
        Update.Assignments update = getUpdateQuery()
                .where(eq(DRAW_ID_COLUMN, id))
                .with(set(STATUS_COLUMN, status.getCode()));

        execute(update, "updateStatus");
    }

    public void updateDate(long id, long date) {
        Update.Assignments update = getUpdateQuery()
                .where(eq(DRAW_ID_COLUMN, id))
                .with(set(UPDATE_DATE, date));

        execute(update, "updateDate");
    }

    public List<TicketedDrawConfig> getTicketedDraws(LeaderboardStatus status) {
        Select.Where query = getSelectColumnsQuery(CONFIG_TABLE, CONFIG_COLUMN, JSON_COLUMN_NAME)
                .where(eq(STATUS_COLUMN, status.getCode()));

        com.datastax.driver.core.ResultSet result = execute(query, "getTicketedDraws");

        List<TicketedDrawConfig> configs = new ArrayList<>();
        for (com.datastax.driver.core.Row row : result) {
            TicketedDrawConfig tdc = CONFIG_TABLE.deserializeWithClassFromJson(row.getString(JSON_COLUMN_NAME));
            if (tdc == null) {
                tdc = CONFIG_TABLE.deserializeWithClassFrom(row.getBytes(CONFIG_COLUMN));
            }
            if (tdc != null) {
                configs.add(tdc);
            }
        }
        return configs;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return CONFIG_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
