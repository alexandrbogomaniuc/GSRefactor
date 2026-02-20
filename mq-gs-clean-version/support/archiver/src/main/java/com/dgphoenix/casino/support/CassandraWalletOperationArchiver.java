package com.dgphoenix.casino.support;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraWalletOperationInfoPersister;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by grien on 06.02.15.
 */
public class CassandraWalletOperationArchiver extends AbstractStorageArchiver<WalletOperationInfo, Long> {

    private static final int DEFAULT_START_PERIOD = 2;
    private static final int DEFAULT_END_PERIOD = 1;
    private static final int REMOVE_BATCH_SIZE = 100;

    private final CassandraWalletOperationInfoPersister walletOperationInfoPersister;

    public CassandraWalletOperationArchiver() {
        super();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        walletOperationInfoPersister = persistenceManager.getPersister(CassandraWalletOperationInfoPersister.class);
    }

    @Override
    public String getName() {
        return "cassandra_wallet";
    }

    @Override
    protected String getColumnFamilyName() {
        return walletOperationInfoPersister.getMainColumnFamilyName();
    }

    @Override
    protected int getDefaultStartPeriod() {
        return DEFAULT_START_PERIOD;
    }

    @Override
    protected int getDefaultEndPeriod() {
        return DEFAULT_END_PERIOD;
    }

    @Override
    protected Iterable<WalletOperationInfo> getRecords(Date dayStartDate, Date dayEndDate) throws CommonException {
        return walletOperationInfoPersister.getRecordsByDay(dayStartDate);
    }

    @Override
    protected void remove(List<Long> needRemoveIdentifiers) {
        List<List<Long>> removeBatches = Lists.partition(needRemoveIdentifiers, REMOVE_BATCH_SIZE);
        for (List<Long> removeBatch : removeBatches) {
            long[] ids = ArrayUtils.toPrimitive(removeBatch.toArray(new Long[removeBatch.size()]));
            walletOperationInfoPersister.delete(ids);
        }
    }

    @Override
    protected void addNeedRemoveIdentifier(WalletOperationInfo record, List<Long> needRemoveIdentifiers) {
        needRemoveIdentifiers.add(record.getId());
    }
}
