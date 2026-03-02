package com.abs.casino.support;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraPlayerSessionHistoryPersister;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

/**
 * Created by grien on 06.02.15.
 */
public class CassandraPlayerSessionArchiver extends AbstractStorageArchiver<SessionInfo, String> {

    private static final int DEFAULT_PERIOD = 4;
    private static final int REMOVE_BATCH_SIZE = 100;

    private final CassandraPlayerSessionHistoryPersister playerSessionHistoryPersister;

    public CassandraPlayerSessionArchiver() {
        super();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        playerSessionHistoryPersister = persistenceManager.getPersister(CassandraPlayerSessionHistoryPersister.class);
    }

    @Override
    public String getName() {
        return "cassandra_playersession";
    }

    @Override
    protected String getColumnFamilyName() {
        return playerSessionHistoryPersister.getMainColumnFamilyName();
    }

    @Override
    protected int getDefaultStartPeriod() {
        return DEFAULT_PERIOD;
    }

    @Override
    protected Iterable<SessionInfo> getRecords(Date dayStartDate, Date dayEndDate) throws CommonException {
        return playerSessionHistoryPersister.getRecordsByDay(dayStartDate);
    }

    @Override
    protected void remove(List<String> needRemoveIdentifiers) {
        List<List<String>> removeBatches = Lists.partition(needRemoveIdentifiers, REMOVE_BATCH_SIZE);
        for (List<String> removeBatch : removeBatches) {
            String[] ids = removeBatch.toArray(new String[removeBatch.size()]);
            playerSessionHistoryPersister.delete(ids);
        }
    }

    @Override
    protected void addNeedRemoveIdentifier(SessionInfo record, List<String> needRemoveIdentifiers) {
        needRemoveIdentifiers.add(record.getSessionId());
    }
}
