package com.abs.casino.cassandra;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;

public interface IConfigsInitializer {

    void initialize(CassandraPersistenceManager persistenceManager);
}
