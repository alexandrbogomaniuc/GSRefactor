package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.data.payment.PaymentMode;
import com.abs.casino.common.cache.data.payment.transfer.ExternalPaymentTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * User: flsh
 * Date: 13.11.13
 */
public class CassandraExternalTransactionPersister extends AbstractCassandraPersister<String, String> {
    //INTERNAL_ID_FIELD = PaymentMode.name+ExternalPaymentTransaction.internalOperationId
    public static final String INTERNAL_ID_FIELD = "InternalId";
    public static final String EXTERNAL_TRANSACTION_CF = "ExtTransactionCF";

    private static final Logger LOG = LogManager.getLogger(CassandraExternalTransactionPersister.class);
    private static final TableDefinition TABLE = new TableDefinition(EXTERNAL_TRANSACTION_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(INTERNAL_ID_FIELD, com.datastax.driver.core.DataType.text(), false, true, false),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ),
            KEY);

    private CassandraExternalTransactionPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    protected String getKey(long bankId, String extOperationId) {
        return bankId + ID_DELIMITER + extOperationId;
    }

    protected String getInternalId(PaymentMode mode, long internalOperationId) {
        return mode.name() + ID_DELIMITER + internalOperationId;
    }


    public ExternalPaymentTransaction get(long bankId, String extOperationId) {
        String key = getKey(bankId, extOperationId);
        return get(key, ExternalPaymentTransaction.class);
    }

    public void delete(long bankId, String extOperationId) {
        deleteWithCheck(getKey(bankId, extOperationId));
    }

    public ExternalPaymentTransaction getByInternalId(PaymentMode mode, long internalOperationId) {
        String key = getInternalId(mode, internalOperationId);
        com.datastax.driver.core.Statement query = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(INTERNAL_ID_FIELD, key));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByInternalId");
        com.datastax.driver.core.Row row = resultSet.one();
        if (row == null) {
            return null;
        }
        String json = row.getString(JSON_COLUMN_NAME);
        ExternalPaymentTransaction obj = TABLE.deserializeFromJson(json, ExternalPaymentTransaction.class);

        if (obj == null) {
            ByteBuffer bytes = row.getBytes(SERIALIZED_COLUMN_NAME);
            obj = TABLE.deserializeFrom(bytes, ExternalPaymentTransaction.class);
        }
        return obj;
    }

    public void persist(ExternalPaymentTransaction transaction) {

        getLog().debug("persist: ExternalPaymentTransaction={}, trace:{}",
                transaction, Thread.currentThread().getStackTrace());

        String key = getKey(transaction.getBankId(), transaction.getExtId());
        String json = TABLE.serializeToJson(transaction);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(transaction);
        try {
            com.datastax.driver.core.Statement query = transaction.getInternalOperationId() == null
                    ? getInsertQuery()
                            .value(KEY, key)
                            .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                            .value(JSON_COLUMN_NAME, json)
                    : getInsertQuery()
                            .value(KEY, key)
                            .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                            .value(JSON_COLUMN_NAME, json)
                            .value(INTERNAL_ID_FIELD, getInternalId(
                                    transaction.getPaymentMode(),
                                    transaction.getInternalOperationId()
                            ));
            execute(query, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }
    }
}
