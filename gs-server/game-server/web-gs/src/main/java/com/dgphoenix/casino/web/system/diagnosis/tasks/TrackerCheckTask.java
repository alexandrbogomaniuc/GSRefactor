package com.abs.casino.web.system.diagnosis.tasks;

import com.abs.casino.common.engine.tracker.AbstractCommonTracker;

import java.util.concurrent.TimeUnit;

public class TrackerCheckTask extends AbstractCheckTask {
    private static final long OUT_TIME = TimeUnit.MINUTES.toMillis(10);
    private final AbstractCommonTracker tracker;
    private long lastAsyncUpdate = System.currentTimeMillis();
    private long lastCompletedTaskCount = 0;
    private long lastTaskCount = 0;

    public TrackerCheckTask(AbstractCommonTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public boolean isOut(boolean strongValidation) {
        boolean taskFailed = true;
        try {
            taskExecutionStartTime = getCurrentTime();
            long taskCount = tracker.getTaskCount();
            long completedTaskCount = tracker.getCompletedTaskCount();
            if (isStateOk(taskCount, completedTaskCount)) {
                lastAsyncUpdate = getCurrentTime();
                taskFailed = false;
            }

            lastTaskCount = taskCount;
            lastCompletedTaskCount = completedTaskCount;
        } catch (Throwable e) {
            getLog().error("An error has occurred during {} checking: ", tracker.getClass().getSimpleName(), e);
            taskFailed = true;
        } finally {
            setTaskFailed(taskFailed);
            taskExecutionEndTime = getCurrentTime();
        }

        return super.isOut(strongValidation);
    }

    private boolean isStateOk(long taskCount, long completedTaskCount) {
        return (lastTaskCount == taskCount || completedTaskCount > lastCompletedTaskCount)
                || (getCurrentTime() - lastAsyncUpdate) < OUT_TIME;
    }
}
