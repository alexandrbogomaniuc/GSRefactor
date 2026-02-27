package com.abs.casino.support;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraFrBonusArchivePersister;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;

import java.util.Date;
import java.util.List;

public class CassandraFRBonusArchiver extends AbstractStorageArchiver<FRBonus, Long> {
    private static final int DEFAULT_PERIOD = 30;
    private static final String FR_BONUS_ARCHIVER_NAME = "cassandra_frbonusarchive";

    private CassandraFrBonusArchivePersister frBonusArchivePersister;

    CassandraFRBonusArchiver() {
        super();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean(CassandraPersistenceManager.class);
        frBonusArchivePersister = persistenceManager.getPersister(CassandraFrBonusArchivePersister.class);
    }

    @Override
    public String getName() {
        return FR_BONUS_ARCHIVER_NAME;
    }

    @Override
    protected String getColumnFamilyName() {
        return frBonusArchivePersister.getMainColumnFamilyName();
    }

    @Override
    protected int getDefaultStartPeriod() {
        return DEFAULT_PERIOD;
    }

    @Override
    protected Iterable<FRBonus> getRecords(Date dayStartDate, Date dayEndDate) {
        return frBonusArchivePersister.getRecordsByDay(dayStartDate.getTime());
    }

    @Override
    protected void remove(List<Long> needRemoveFRBonusIds) {
        for (Long frBonusId : needRemoveFRBonusIds) {
            frBonusArchivePersister.delete(frBonusId);
        }
    }

    @Override
    protected void addNeedRemoveIdentifier(FRBonus bonus, List<Long> needRemoveFRBonusIds) {
        needRemoveFRBonusIds.add(bonus.getId());
    }
}