package com.abs.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.CassandraRoundGameSessionPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CassandraBigStorageRoundGameSessionPersister extends CassandraRoundGameSessionPersister {
    private static final Logger LOG = LogManager.getLogger(CassandraBigStorageRoundGameSessionPersister.class);

    @Override
    public Logger getLog() {
        return LOG;
    }
}
