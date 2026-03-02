package com.abs.casino.cassandra;

import com.abs.casino.cassandra.CassandraPersistenceManager;

public interface IConfigsInitializer {

    void initialize(CassandraPersistenceManager persistenceManager);
}
