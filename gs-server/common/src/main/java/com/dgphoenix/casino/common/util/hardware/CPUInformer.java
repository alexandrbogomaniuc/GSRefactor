package com.abs.casino.common.util.hardware;

import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.CommonExecutorService;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CPUInformer {
    private static CPUInformer instance = new CPUInformer();
    public static final long SLEEP_TIME = 20;
    private CPUUpdater updaterThread = new CPUUpdater(3);
    private ScheduledFuture<?> scheduledFuture;

    private CPUInformer() {
    }

    public void startup() {
        ScheduledExecutorService scheduler = ApplicationContextHelper.getBean(CommonExecutorService.class);
        scheduledFuture = scheduler.scheduleAtFixedRate(updaterThread, 1, SLEEP_TIME, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduledFuture.cancel(true);
    }

    public static CPUInformer getInstance() {
        return instance;
    }

    public double getAvrCpuPercent() {
        return updaterThread.getAvrCpuPercent();
    }

    class CPUUpdater implements Runnable {
        private double cpuPercentSumm;
        private double avrCpuPercent;
        private int maxCount;
        private int count;

        public CPUUpdater(int maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public void run() {
            //LOG.debug("Updating CPUInfo....");
            if (count > maxCount) {
                count = 1;
                cpuPercentSumm = 0;
            }
            cpuPercentSumm += HardwareConfigurationManager.getInstance().getCPUPercent();
            avrCpuPercent = calculateAvrCPUPercent(cpuPercentSumm, count);
            count++;
        }

        private double calculateAvrCPUPercent(double cpuPercentSumm, int count) {
            return cpuPercentSumm / count;
        }

        public double getAvrCpuPercent() {
            return avrCpuPercent;
        }
    }
}
