package com.betsoft.casino.mp.data.persister;

import com.abs.casino.cassandra.AbstractLockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 10.04.18.
 */
public class LockPersister extends AbstractLockManager {
    public static final String LOCK_CF = "MP_DLM_CF";
    private static final Logger LOG = LogManager.getLogger(LockPersister.class);

    public LockPersister() {
        super(512, 100, 2000, 8);
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
