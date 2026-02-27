package com.abs.casino.web.system.diagnosis.tasks;

import com.abs.casino.common.util.web.HttpClientConnection;

public class HttpClientConnectionCheckTask extends AbstractCheckTask {
    private static final double OVERLOAD_RATE = 0.9;

    @Override
    public boolean isOut(boolean strongValidation) {
        boolean failedTask = false;
        try {
            taskExecutionStartTime = getCurrentTime();
            int connectionsInPool = HttpClientConnection.getConnectionsInPool();
            failedTask = connectionsInPool >= HttpClientConnection.getMaxTotal() * OVERLOAD_RATE;
        } catch (Throwable e) {
            failedTask = true;
            getLog().error("An error has occurred during HTTP client connection checking: ", e);
        } finally {
            setTaskFailed(failedTask);
            taskExecutionEndTime = getCurrentTime();
        }

        return super.isOut(strongValidation);
    }
}
