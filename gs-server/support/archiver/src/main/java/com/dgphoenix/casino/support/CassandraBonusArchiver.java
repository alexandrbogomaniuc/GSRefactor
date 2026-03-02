package com.abs.casino.support;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraBonusArchivePersister;
import com.abs.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.util.ApplicationContextHelper;

import java.util.Date;
import java.util.List;

public class CassandraBonusArchiver extends AbstractStorageArchiver<Bonus, Long> {
    private static final int DEFAULT_PERIOD = 30;
    private static final String BONUS_ARCHIVER_NAME = "cassandra_bonusarchive";

    private final CassandraBonusArchivePersister bonusArchivePersister;

    CassandraBonusArchiver() {
        super();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean(CassandraPersistenceManager.class);
        bonusArchivePersister = persistenceManager.getPersister(CassandraBonusArchivePersister.class);
    }

    @Override
    public String getName() {
        return BONUS_ARCHIVER_NAME;
    }

    @Override
    protected String getColumnFamilyName() {
        return bonusArchivePersister.getMainColumnFamilyName();
    }

    @Override
    protected int getDefaultStartPeriod() {
        return DEFAULT_PERIOD;
    }

    @Override
    protected Iterable<Bonus> getRecords(Date dayStartDate, Date dayEndDate) {
        return bonusArchivePersister.getRecordsByDay(dayStartDate.getTime());
    }

    @Override
    protected void remove(List<Long> needRemoveBonusIds) {
        for (Long bonusId : needRemoveBonusIds) {
            bonusArchivePersister.delete(bonusId);
        }
    }

    @Override
    protected void addNeedRemoveIdentifier(Bonus bonus, List<Long> needRemoveBonusIds) {
        needRemoveBonusIds.add(bonus.getId());
    }
}