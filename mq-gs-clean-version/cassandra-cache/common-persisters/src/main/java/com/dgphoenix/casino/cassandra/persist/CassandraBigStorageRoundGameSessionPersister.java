package com.dgphoenix.casino.cassandra.persist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CassandraBigStorageRoundGameSessionPersister extends CassandraRoundGameSessionPersister {
    private static final Logger LOG = LogManager.getLogger(CassandraBigStorageRoundGameSessionPersister.class);

    @Override
    public Logger getLog() {
        return LOG;
    }
}
