package com.abs.casino.web.system.diagnosis.tasks;

import com.dgphoenix.casino.common.web.diagnostic.CheckTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public abstract class AbstractCheckTask extends CheckTask {
    private static final long TIME_FOR_TASK = TimeUnit.SECONDS.toMillis(5);
    private static final Logger LOG = LogManager.getLogger(AbstractCheckTask.class);
    private boolean taskFailed = true;
    protected long taskExecutionStartTime;
    protected long taskExecutionEndTime;

    public AbstractCheckTask() {
        super(null, true);
    }

    @Override
    public boolean isOut(boolean strongValidation) {
        return taskFailed || (taskExecutionEndTime - taskExecutionStartTime) > TIME_FOR_TASK;
    }

    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }

    protected static Logger getLog() {
        return LOG;
    }

    protected void setTaskFailed(boolean taskFailed) {
        this.taskFailed = taskFailed;
    }
}