package com.dgphoenix.casino.cassandra;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 19.03.13
 */
public class DistributedLockManager extends AbstractLockManager {
    private static final String LOCK_CF = "DLM_CF";
    private static final Logger LOG = LogManager.getLogger(DistributedLockManager.class);

    private DistributedLockManager() {
        super(100, 100, 4000, 8);
    }

    @Override
    public String getMainColumnFamilyName() {
        return LOCK_CF;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
