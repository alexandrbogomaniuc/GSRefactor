package com.dgphoenix.casino.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * User: plastical
 * Date: 20.05.2010
 */
public class NewThreadRejectionHandler implements RejectedExecutionHandler {
    private static final Logger LOG = LogManager.getLogger(NewThreadRejectionHandler.class);

    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        LOG.warn("<<<<<<<<<<<WARNING! Task:" + r + " was rejected for execution (starting this task in new thread)>>>>>>>>>>>>");
        Thread rejectedTask = new Thread(r);
        rejectedTask.setDaemon(true);
        rejectedTask.start();
    }
}
