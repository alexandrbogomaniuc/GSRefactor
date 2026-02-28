package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.CassandraBetPersister;
import com.abs.casino.cassandra.persist.CassandraRoundGameSessionPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CassandraBigStorageBetPersister extends CassandraBetPersister {
    private static final Logger LOG = LogManager.getLogger(CassandraBigStorageBetPersister.class);

    protected CassandraBigStorageRoundGameSessionPersister roundGameSessionPersister;

    @Override
    public Logger getLog() {
        return LOG;
    }

    @SuppressWarnings("unused")
    private void setRoundGameSessionPersister(CassandraBigStorageRoundGameSessionPersister roundGameSessionPersister) {
        this.roundGameSessionPersister = roundGameSessionPersister;
    }

    @Override
    public CassandraRoundGameSessionPersister getRoundGameSessionPersister() {
        return roundGameSessionPersister;
    }
}
