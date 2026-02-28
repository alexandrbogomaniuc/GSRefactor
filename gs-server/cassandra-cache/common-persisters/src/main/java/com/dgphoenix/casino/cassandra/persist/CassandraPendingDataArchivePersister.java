package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by quant on 28.12.17.
 */
public class CassandraPendingDataArchivePersister extends AbstractCassandraPersister<String, String> {
    public static final String PENDING_DATA_ARCH_CF = "PendDataArchCF";
    public static final String ACCOUNT_ID_FIELD = "AccId";
    public static final String DATA_NAME_FIELD = "DataName";
    public static final String CREATION_TIME_FIELD = "CreationTime";
    public static final String WALLET_DATA_NAME = "cwOp";
    public static final String FRB_WIN_DATA_NAME = "frbWin";
    private static final Logger LOG = LogManager.getLogger(CassandraPendingDataArchivePersister.class);

    private static final TableDefinition PENDING_DATA_ARCHIVE_TABLE = new TableDefinition(PENDING_DATA_ARCH_CF,
            Arrays.asList(
                    new ColumnDefinition(ACCOUNT_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(DATA_NAME_FIELD, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(CREATION_TIME_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), ACCOUNT_ID_FIELD);

    private CassandraPendingDataArchivePersister() {
        super();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return PENDING_DATA_ARCHIVE_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void saveWalletOperation(CommonWalletOperation operation) {
        String json = PENDING_DATA_ARCHIVE_TABLE.serializeToJson(operation);
        ByteBuffer byteBuffer = PENDING_DATA_ARCHIVE_TABLE.serializeToBytes(operation);
        try {
            com.datastax.driver.core.Statement query = getInsertQuery()
                    .value(ACCOUNT_ID_FIELD, operation.getAccountId())
                    .value(DATA_NAME_FIELD, WALLET_DATA_NAME)
                    .value(CREATION_TIME_FIELD, operation.getStartTime())
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(query, "saveWalletOperation");
            LOG.debug("CommonWalletOperation={} was saved successfully", operation);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public List<CommonWalletOperation> getWalletOperations(long accountId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(PENDING_DATA_ARCHIVE_TABLE, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ACCOUNT_ID_FIELD, accountId))
                .and(eq(DATA_NAME_FIELD, WALLET_DATA_NAME));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getWalletOperations");
        List<CommonWalletOperation> result = new ArrayList<>(resultSet.getAvailableWithoutFetching());
        for (com.datastax.driver.core.Row row : resultSet) {
            String json = row.getString(SERIALIZED_COLUMN_NAME);
            CommonWalletOperation operation = PENDING_DATA_ARCHIVE_TABLE.deserializeFromJson(json, CommonWalletOperation.class);

            if (operation == null) {
                ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
                operation = PENDING_DATA_ARCHIVE_TABLE.deserializeFrom(buffer, CommonWalletOperation.class);
            }
            result.add(operation);
        }
        return result;
    }

    public void saveFrbWinOperation(FRBWinOperation operation) {
        String json = PENDING_DATA_ARCHIVE_TABLE.serializeToJson(operation);
        ByteBuffer byteBuffer = PENDING_DATA_ARCHIVE_TABLE.serializeToBytes(operation);
        try {
            com.datastax.driver.core.Statement query = getInsertQuery()
                    .value(ACCOUNT_ID_FIELD, operation.getAccountId())
                    .value(DATA_NAME_FIELD, FRB_WIN_DATA_NAME)
                    .value(CREATION_TIME_FIELD, operation.getStartTime())
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(query, "saveFrbWinOperation");
            LOG.debug("FrbWinOperation={} was saved successfully", operation);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public List<FRBWinOperation> getFrbWinOperations(long accountId) {
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(PENDING_DATA_ARCHIVE_TABLE, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(ACCOUNT_ID_FIELD, accountId))
                .and(eq(DATA_NAME_FIELD, FRB_WIN_DATA_NAME));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getFrbWinOperations");
        List<FRBWinOperation> result = new ArrayList<>(resultSet.getAvailableWithoutFetching());
        for (com.datastax.driver.core.Row row : resultSet) {
            String json = row.getString(JSON_COLUMN_NAME);
            FRBWinOperation operation = PENDING_DATA_ARCHIVE_TABLE.deserializeFromJson(json, FRBWinOperation.class);

            if (operation == null) {
                ByteBuffer buffer = row.getBytes(SERIALIZED_COLUMN_NAME);
                operation = PENDING_DATA_ARCHIVE_TABLE.deserializeFrom(buffer, FRBWinOperation.class);
            }

            result.add(operation);
        }
        return result;
    }
}
