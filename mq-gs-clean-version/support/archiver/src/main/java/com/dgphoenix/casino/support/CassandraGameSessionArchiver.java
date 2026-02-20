package com.dgphoenix.casino.support;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister.ShortGameSessionInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;

import java.util.Date;
import java.util.List;

/**
 * Created by grien on 06.02.15.
 */
public class CassandraGameSessionArchiver extends AbstractStorageArchiver<GameSession, ShortGameSessionInfo> {

    private static final int DEFAULT_PERIOD = 6;

    private final CassandraGameSessionPersister gameSessionPersister;

    public CassandraGameSessionArchiver() {
        super();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
    }

    @Override
    public String getName() {
        return "cassandra_gamesession";
    }

    @Override
    protected String getColumnFamilyName() {
        return gameSessionPersister.getMainColumnFamilyName();
    }

    @Override
    protected int getDefaultStartPeriod() {
        return DEFAULT_PERIOD;
    }

    @Override
    protected Iterable<GameSession> getRecords(Date dayStartDate, Date currentEndDate) {
        return gameSessionPersister.getRecordsByDay(dayStartDate);
    }

    @Override
    protected void remove(List<ShortGameSessionInfo> needRemoveIdentifiers) {
        for (ShortGameSessionInfo info : needRemoveIdentifiers) {
            boolean deleted = gameSessionPersister.delete(info);
            if (!deleted) {
                info("Can't delete: " + info);
            }
        }
    }

    @Override
    protected void addNeedRemoveIdentifier(GameSession record, List<ShortGameSessionInfo> needRemoveIdentifiers) {
        needRemoveIdentifiers.add(new ShortGameSessionInfo(record));
    }
}
