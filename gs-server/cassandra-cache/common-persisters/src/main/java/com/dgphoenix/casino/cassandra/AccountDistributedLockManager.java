package com.dgphoenix.casino.cassandra;

import com.dgphoenix.casino.cassandra.persist.CassandraTransactionDataPersister;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by grien on 17.02.15.
 */
public class AccountDistributedLockManager extends AbstractLockManager {
    //ReadTimeouts under high load hint: alter table ACC_DLM_CF with speculative_retry = 'ALWAYS';
    private static final String LOCK_CF = "ACC_DLM_CF";
    private static final Logger LOG = LogManager.getLogger(AccountDistributedLockManager.class);

    private AccountDistributedLockManager() {
        super(2000, 2000, 10000, 8);
    }

    @SuppressWarnings("unused")
    private void setTransactionDataPersister(CassandraTransactionDataPersister transactionDataPersister) {
        registerListener(transactionDataPersister);
    }

    public String getMainColumnFamilyName() {
        return LOCK_CF;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        TableDefinition tableDefinition = super.getMainTableDefinition();
        tableDefinition.speculativeRetry(com.datastax.driver.core.schemabuilder.SchemaBuilder.always());
        return tableDefinition;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
