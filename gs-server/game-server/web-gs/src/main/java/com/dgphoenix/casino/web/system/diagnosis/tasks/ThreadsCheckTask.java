package com.abs.casino.web.system.diagnosis.tasks;

import com.dgphoenix.casino.common.utils.MBeanUtils;

public class ThreadsCheckTask extends AbstractCheckTask {
    private static final double OVERLOAD_RATE = 0.9;

    @Override
    public boolean isOut(boolean strongValidation) {
        boolean taskFailed = false;
        taskExecutionStartTime = getCurrentTime();
        try {
            taskFailed = MBeanUtils.getCurrentThreadCount() > MBeanUtils.getMaxThreads() * OVERLOAD_RATE;
        } catch (Throwable e) {
            taskFailed = true;
            getLog().error("An error has occurred during threads checking: ", e);
        } finally {
            setTaskFailed(taskFailed);
            taskExecutionEndTime = getCurrentTime();
        }

        return super.isOut(strongValidation);
    }
}
