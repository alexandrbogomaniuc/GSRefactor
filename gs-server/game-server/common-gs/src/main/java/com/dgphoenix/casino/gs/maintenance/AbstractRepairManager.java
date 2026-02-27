package com.abs.casino.gs.maintenance;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: flsh
 * Date: 21.10.2009
 */
public abstract class AbstractRepairManager {
    protected final static int PAGE_SIZE = 100;
    protected final static int PAGE_FETCH_SLEEP_TIME = 50;
    protected AtomicBoolean running = new AtomicBoolean(false);

    public boolean isRunning() {
        return running.get();
    }
}
